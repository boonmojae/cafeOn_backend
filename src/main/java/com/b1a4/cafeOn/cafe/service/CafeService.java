package com.b1a4.cafeOn.cafe.service;

import com.b1a4.cafeOn.cafe.dto.CafeDTO;
import com.b1a4.cafeOn.cafe.dto.CafeDetailResponse;
import com.b1a4.cafeOn.cafe.entity.CafeEntity;
import com.b1a4.cafeOn.cafe.enums.CafeSource;
import com.b1a4.cafeOn.cafe.exception.KakaoApiConnectionException;
import com.b1a4.cafeOn.cafe.exception.KakaoApiRequestException;
import com.b1a4.cafeOn.cafe.exception.KakaoApiServiceUnavailableException;
import com.b1a4.cafeOn.cafe.repository.CafeRepository;
import com.b1a4.cafeOn.common.exception.ClientErrorException;
import com.b1a4.cafeOn.review.dto.ReviewResponseDTO;
import com.b1a4.cafeOn.review.service.ReviewService;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// /api/cafes/**
// 카페 검색, 상세 조회, 리뷰 연결
@Slf4j
@Service
@EnableAsync
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class CafeService {
    private final CafeRepository cafeRepository;
    private final ReviewService reviewService;
    private final RestTemplate restTemplate;
    private static final int KAKAO_PAGE_SIZE = 15;
    private static final int KAKAO_MAX_PAGES = 45;

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    /**
     * 1. 키워드나 태그로 검색
     */
    public List<CafeDetailResponse> searchCafes(String keyword, String tag) {

        // 1-1. keyword가 있으면 [카카오맵 REST API(키워드로 장소검색) + DB병합] 로직 실행
        if (keyword != null && !keyword.isEmpty()) {
            // ✅ 카카오 API 검색 + DB 병합 결과 (CafeDTO 대신 CafeDetailResponse 반환하도록 변경)
            List<CafeDTO> kakaoMerged = searchAndMerge(keyword);

            // 🔁 CafeDTO → CafeDetailResponse로 변환 (N+1 문제 해결: 배치 조회 사용)
            // 카페 ID 리스트 추출 (null 제외)
            List<Long> cafeIds = kakaoMerged.stream()
                    .map(CafeDTO::getCafeId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 배치 조회: 카페, 태그, 리뷰 일괄 조회
            final Map<Long, CafeEntity> cafeMap = !cafeIds.isEmpty()
                    ? cafeRepository.findAllById(cafeIds).stream()
                    .collect(Collectors.toMap(CafeEntity::getCafeId, Function.identity()))
                    : new HashMap<>();

            final Map<Long, List<String>> tagsMap = new HashMap<>();
            if (!cafeIds.isEmpty()) {
                // 태그 일괄 조회
                try {
                    List<Object[]> tagResults = cafeRepository.findTagNamesByCafeIds(cafeIds);
                    log.info("🔍 [태그 조회] 요청 카페 수: {}, 조회된 태그 결과 수: {}", cafeIds.size(), tagResults != null ? tagResults.size() : 0);

                    if (tagResults != null && !tagResults.isEmpty()) {
                        for (Object[] row : tagResults) {
                            if (row == null || row.length < 2) {
                                continue;
                            }

                            Long cafeId = null;
                            try {
                                // cafe_id 파싱 (BigInteger, Long, Integer 등 처리)
                                Object idObj = row[0];
                                if (idObj == null) continue;

                                if (idObj instanceof Number) {
                                    cafeId = ((Number) idObj).longValue();
                                } else if (idObj instanceof String) {
                                    cafeId = Long.parseLong((String) idObj);
                                } else {
                                    log.warn("⚠️ cafe_id 예상치 못한 타입: {}", idObj.getClass().getName());
                                    continue;
                                }
                            } catch (Exception e) {
                                log.warn("⚠️ cafe_id 파싱 실패: row={}, error={}", Arrays.toString(row), e.getMessage());
                                continue;
                            }

                            // 태그명 파싱
                            String tagName = null;
                            try {
                                Object tagObj = row[1];
                                if (tagObj != null) {
                                    tagName = tagObj.toString();
                                }
                            } catch (Exception e) {
                                log.warn("⚠️ 태그명 파싱 실패: row={}, error={}", Arrays.toString(row), e.getMessage());
                                continue;
                            }

                            if (cafeId != null && tagName != null && !tagName.isEmpty()) {
                                tagsMap.computeIfAbsent(cafeId, k -> new ArrayList<>()).add(tagName);
                            }
                        }
                    }

                    log.info("📊 [태그 조회 완료] 총 {}개 카페 중 태그가 있는 카페: {}개", cafeIds.size(), tagsMap.size());
                } catch (Exception e) {
                    log.error("❌ 태그 일괄 조회 중 오류 발생: {}", e.getMessage(), e);
                }
            }

            // 검색 결과에서는 리뷰를 빈 배열로 반환 (목록 페이지에서 리뷰 불필요, 성능 최적화)
            final Map<Long, List<ReviewResponseDTO>> reviewsMap = new HashMap<>();
            for (Long cafeId : cafeIds) {
                reviewsMap.put(cafeId, Collections.emptyList());
                // 태그가 없는 카페는 빈 리스트로 초기화 (이미 조회한 결과가 있으면 유지)
                tagsMap.putIfAbsent(cafeId, Collections.emptyList());
            }

            // CafeDTO → CafeDetailResponse 변환
            return kakaoMerged.stream()
                    .map(dto -> {
                        // dto.getCafeId()가 null일 수도 있음 (신규 카페)
                        if (dto.getCafeId() == null) {
                            return CafeDetailResponse.builder()
                                    .name(dto.getName())
                                    .address(dto.getAddress())
                                    .latitude(dto.getLatitude())
                                    .longitude(dto.getLongitude())
                                    .phone(dto.getPhone())
                                    .rating(dto.getAvgRating() != null ? dto.getAvgRating().toString() : "0.00")
                                    .reviewsSummary(dto.getReviewsSummary())
                                    .photoUrl(dto.getPhotoUrl())
                                    .reviews(List.of())
                                    .tags(List.of())
                                    .build();
                        }

                        // DB 존재 카페는 배치 조회한 데이터 사용
                        CafeEntity entity = cafeMap.get(dto.getCafeId());
                        if (entity == null) {
                            return null;
                        }

                        List<String> tags = tagsMap.getOrDefault(entity.getCafeId(), Collections.emptyList());
                        List<ReviewResponseDTO> reviews = reviewsMap.getOrDefault(entity.getCafeId(), Collections.emptyList());

                        BigDecimal rating = entity.getAvgRating() != null
                                ? entity.getAvgRating()
                                : entity.getKakaoRating();

                        return CafeDetailResponse.builder()
                                .id(entity.getCafeId())
                                .name(entity.getName())
                                .address(entity.getAddress())
                                .phone(entity.getPhone())
                                .hours(entity.getOpenHours())
                                .rating(rating != null ? String.format("%.2f", rating) : "0.00")
                                .reviewsSummary(entity.getReviewsSummary())
                                .reviews(reviews)
                                .tags(tags)
                                .photoUrl(entity.getPhotoUrl())
                                .latitude(entity.getLatitude())
                                .longitude(entity.getLongitude())
                                .build();
                    })
                    .filter(Objects::nonNull)
                    // tag 파라미터가 있으면 태그로 필터링
                    .filter(response -> {
                        if (tag != null && !tag.isEmpty()) {
                            return response.getTags() != null && response.getTags().contains(tag);
                        }
                        return true;
                    })
                    .toList();
        }

        // 1-2. keyword가 없고 tag나 전체 조회일 경우 (DB만 조회)
        List<CafeEntity> cafes;
        if (tag != null && !tag.isEmpty()) {
            cafes = cafeRepository.findByTag(tag);
        } else {
            cafes = cafeRepository.findAll();
        }

        // 1-3. 각 카페별 찜 수 집계 (wishlists 테이블 기준)
        //       -> cafes테이블에 wishlist_count 컬럼이 없다면 이 맵으로 채움
        List<Long> cafeIds = cafes.stream()
                .map(CafeEntity::getCafeId)
                .toList();
        Map<Long, Integer> wishlistMap = buildWishCountMap(cafeIds);

        // 1-4. N+1 문제 해결: 태그와 리뷰 배치 조회
        final Map<Long, List<String>> tagsMap = new HashMap<>();
        if (!cafeIds.isEmpty()) {
            // 태그 일괄 조회
            List<Object[]> tagResults = cafeRepository.findTagNamesByCafeIds(cafeIds);
            for (Object[] row : tagResults) {
                Long cafeId = ((Number) row[0]).longValue();
                String tagName = (String) row[1];
                tagsMap.computeIfAbsent(cafeId, k -> new ArrayList<>()).add(tagName);
            }
        }

        // 검색 결과에서는 리뷰를 빈 배열로 반환 (목록 페이지에서 리뷰 불필요, 성능 최적화)
        final Map<Long, List<ReviewResponseDTO>> reviewsMap = new HashMap<>();
        for (Long cafeId : cafeIds) {
            reviewsMap.put(cafeId, Collections.emptyList());
            tagsMap.putIfAbsent(cafeId, Collections.emptyList());
        }

        // 1-5. Entity -> CafeDetailResponse 변환 + wishlistCount 주입 (배치 조회 데이터 사용)
        return cafes.stream()
                .map(entity -> {
                    List<String> tags = tagsMap.getOrDefault(entity.getCafeId(), Collections.emptyList());
                    List<ReviewResponseDTO> reviews = reviewsMap.getOrDefault(entity.getCafeId(), Collections.emptyList());

                    BigDecimal rating = entity.getAvgRating() != null
                            ? entity.getAvgRating()
                            : entity.getKakaoRating();

                    CafeDetailResponse response = CafeDetailResponse.builder()
                            .id(entity.getCafeId())
                            .name(entity.getName())
                            .address(entity.getAddress())
                            .phone(entity.getPhone())
                            .hours(entity.getOpenHours())
                            .rating(rating != null ? String.format("%.2f", rating) : "0.00")
                            .reviewsSummary(entity.getReviewsSummary())
                            .reviews(reviews)
                            .tags(tags)
                            .photoUrl(entity.getPhotoUrl())
                            .latitude(entity.getLatitude())
                            .longitude(entity.getLongitude())
                            .build();

                    // 찜 개수 직접 세팅 (CafeDetailResponse에 setter 있으면 가능)
                    response.setWishlistCount(wishlistMap.getOrDefault(entity.getCafeId(), 0));

                    return response;
                })
                .collect(Collectors.toList());
    }


    /**
     * 2. 핵심 로직 : 카카오 API 결과와 DB 데이터를 병합
     */
    private List<CafeDTO> searchAndMerge(String keyword) {
        List<Map<String, Object>> documents = fetchFromKakao(keyword);

//        2-1. 카카오 API 에서 결과 없는 경우 -> DB fallback (성능 최적화: LIMIT 추가)
        if (documents.isEmpty()) {
            log.info("⚠️ Kakao API returned no results. Falling back to DB search...");
            List<CafeEntity> dbFallback = cafeRepository.searchByQuery(keyword);
            // 결과 수 제한 (최대 50개)
            return dbFallback.stream()
                    .limit(50)
                    .map(CafeDTO::fromEntity)
                    .collect(Collectors.toList());
        }

//        2-2. 카카오 결과에서 '카페 이름' 목록 추출
        List<String> kakaoCafeNames = documents.stream()
                .map(doc -> (String) doc.get("place_name"))
                .distinct()
                .collect(Collectors.toList());

//        2-3. DB 검색: 이름 일치만 조회 (성능 최적화 - tagMatch 제거)
        List<CafeEntity> dbCafes = cafeRepository.findByNameIn(kakaoCafeNames);

        Map<String, CafeEntity> dbCafesMap = dbCafes.stream()
                .collect(Collectors.toMap(CafeEntity::getName, Function.identity(), (a, b) -> a));

        List<CafeDTO> mergedResults = new ArrayList<>();
        final int MAX_RESULTS = 50; // 결과 수 제한

        for (Map<String, Object> doc : documents) {
            if (mergedResults.size() >= MAX_RESULTS) {
                break; // 최대 개수 제한
            }

            String kakaoName = (String) doc.get("place_name");
            String kakaoId = (String) doc.get("id");

            CafeEntity dbCafe = dbCafesMap.get(kakaoName);
            if (dbCafe != null) {
                mergedResults.add(CafeDTO.fromEntity(dbCafe));
            } else {
                mergedResults.add(parseKakaoDocToDTO(doc));
                synchronizeCafe(doc, kakaoId);
            }
        }

        return mergedResults;
    }

    /**
     * 키워드 기반 카카오 장소검색: 성능 최적화 - 첫 페이지만 조회
     * - 검색 결과 목록에서는 첫 페이지(15개)만으로 충분
     * - UTF-8 인코딩 보장
     */
    private List<Map<String, Object>> fetchFromKakao(String keyword) {
        List<Map<String, Object>> allDocuments = new ArrayList<>();

        try {
            String url = UriComponentsBuilder
                    .fromUriString("https://dapi.kakao.com/v2/local/search/keyword.json")
                    .queryParam("query", keyword)
                    .queryParam("size", 15)
                    .queryParam("page", 1)
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();

            log.info("🚀 Kakao API Request URL: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "KakaoAK " + kakaoApiKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {
                    }
            );

            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("documents")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> documents = (List<Map<String, Object>>) body.get("documents");

                log.info("📡 Kakao API Response: {} results", documents.size());
                allDocuments.addAll(documents);
            }

        } catch (HttpClientErrorException e) {
            log.error("❌ Kakao API 4xx error: status {}", e.getStatusCode());
            throw new KakaoApiRequestException();

        } catch (HttpServerErrorException e) {
            log.error("❌ Kakao API 5xx error: status {}", e.getStatusCode());
            throw new KakaoApiServiceUnavailableException();

        } catch (ResourceAccessException e) {
            log.error("❌ Kakao API connection error (Network/Timeout)", e);
            throw new KakaoApiConnectionException();

        } catch (Exception e) {
            log.error("❌ Kakao API unexpected error", e);
            throw new ClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 API 호출 중 예상치 못한 오류가 발생했습니다.");
        }

        log.info("✅ Kakao fetch done. total documents: {}", allDocuments.size());
        return allDocuments;
    }


    /**
     * @Async: 카카오 검색 결과를 DB에 비동기 저장 (신규 카페만)
     * 이 메서드는 public 이어야 프록시가 생성되어 비동기(@Async)로 동작합니다.
     */
    @Async
    @Transactional
    public void synchronizeCafe(Map<String, Object> doc, String kakaoId) {
//        DB에 kakaoId가 이미 있는지 확인 (더 정확함)
        if (!cafeRepository.existsByKakaoId(kakaoId)) {
            try {
                CafeEntity newCafe = parseKakaoDocToEntity(doc);
                cafeRepository.save(newCafe);
                log.info("✅ Async: New cafe synchronized [{}], Kakao ID [{}]", newCafe.getName(), kakaoId);
            } catch (Exception e) {
//                DB 제약조건 위반(e.g., PK, Unique 중복) 등 예외 처리
                log.warn("⚠️ Async: Failed to synchronize cafe. Kakao ID [{}]. Error: {}", kakaoId, e.getMessage());
            }
        } else {
            log.info("ℹ️ Async: Cafe already exists. Kakao ID [{}]", kakaoId);
//            (선택) 기존 데이터 업데이트 로직 추가 가능
//            예: cafeRepository.findByKakaoId(kakaoId).ifPresent(cafe -> { ... update ... });
        }
    }

    /**
     * 1-4. Helper: 카카오 API(doc) -> CafeDTO (즉시 응답용)
     * (DB에 없는 신규 카페용. avg_rating 등 내부 데이터는 0 또는 null)
     */
    private CafeDTO parseKakaoDocToDTO(Map<String, Object> doc) {
        String road = (String) doc.getOrDefault("road_address_name", "");
        String jibun = (String) doc.getOrDefault("address_name", "");

        return CafeDTO.builder()
                .cafeId(null)   // DTO는 전송용이니 프론트로 보내야하는 값이라 null처리 해서 보낸다
                .name((String) doc.get("place_name"))
                .address(!road.isEmpty() ? road : jibun)
                .latitude(new BigDecimal((String) doc.get("y")))
                .longitude(new BigDecimal((String) doc.get("x")))
                .phone((String) doc.get("phone"))
//                나머지 데이터는 없어서 못보냄
                .build();
    }

    /**
     * 1-5. Helper: 카카오 API(doc) -> CafeEntity (DB 저장용)
     */
    private CafeEntity parseKakaoDocToEntity(Map<String, Object> doc) {
        String road = (String) doc.getOrDefault("road_address_name", "");
        String jibun = (String) doc.getOrDefault("address_name", "");
        String kakaoId = (String) doc.get("id");

//        CafeEntity의 @Id가 Auto-increment라고 가정하고 cafeId는 설정하지 않음
        return CafeEntity.builder()
                .kakaoId(kakaoId)
                .name((String) doc.get("place_name"))
                .address(!road.isEmpty() ? road : jibun)
                .latitude(new BigDecimal((String) doc.get("y")))
                .longitude(new BigDecimal((String) doc.get("x")))
                .phone((String) doc.get("phone"))
                .kakaoUrl((String) doc.get("place_url"))
                .source(CafeSource.KAKAO)   // EnumType import 필요
                .createdAt(LocalDateTime.now())
                .build();
    }



    // 조회수, 최종 시간 갱신
    @Transactional
    public void incrementViewCount(Long id) {
        LocalDateTime now = LocalDateTime.now();
        cafeRepository.incrementViewCountAndSetLastViewedAt(id, now);
    }

    // 카페 상세 정보 조회
    @Transactional(readOnly = true)
    public CafeDetailResponse getCafeDetail(Long id) {

        CafeEntity entity = cafeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 카페를 찾을 수 없습니다. id=" + id));

        incrementViewCount(id);

        List<ReviewResponseDTO> reviews = reviewService.getReviewsByCafeId(id);
        log.info("✅ [CafeService] cafeId={} -> reviews.size={}", id, reviews.size());

        List<String> tagNames = cafeRepository.findTagNamesByCafeId(id);
        log.info(tagNames.toString());

        return CafeDetailResponse.builder()
                .id(entity.getCafeId())
                .name(entity.getName())
                .address(entity.getAddress())
                .phone(entity.getPhone())
                .hours(entity.getOpenHours())
                .rating(String.valueOf(entity.getKakaoRating()))
                .reviewsSummary(entity.getReviewsSummary())
                .reviews(reviews)
                .tags(tagNames)
                .photoUrl(entity.getPhotoUrl())
                .build();
    }


    /**
     * 3. 사용자 위치 기반 근처 카페 조회
     */
    public List<CafeDetailResponse> getNearbyCafes(double latitude, double longitude, int radius) {

        // 1️⃣ DB 기반 조회
        List<CafeEntity> nearbyFromDB = cafeRepository.findNearbyCafes(latitude, longitude, radius);
        log.info("📍 [DB] 반경 {}m 이내 카페 {}개 조회 (lat={}, lon={})",
                radius, nearbyFromDB.size(), latitude, longitude);

        // 2️⃣ Entity → CafeDetailResponse
        List<CafeDetailResponse> cafeDetails = nearbyFromDB.stream()
                .map(this::toDetailResponse)
                .collect(Collectors.toCollection(ArrayList::new));

        // 3️⃣ Kakao API 보조 호출 (DB 결과 부족할 때)
        if (cafeDetails.size() < 10) {
            log.info("⚠️ DB 결과 부족 ({}개) → Kakao API로 보조 조회 시작", cafeDetails.size());

            List<CafeDTO> kakaoCafes = fetchNearbyFromKakao(latitude, longitude, radius);

            Set<String> existingNames = cafeDetails.stream()
                    .map(CafeDetailResponse::getName)
                    .collect(Collectors.toSet());

            List<CafeDetailResponse> newCafes = kakaoCafes.stream()
                    .filter(c -> !existingNames.contains(c.getName()))
                    .map(dto -> CafeDetailResponse.builder()
                            .name(dto.getName())
                            .address(dto.getAddress())
                            .latitude(dto.getLatitude())
                            .longitude(dto.getLongitude())
                            .phone(dto.getPhone())
                            .rating("0.00")
                            .reviewsSummary(null)
                            .reviews(List.of())
                            .tags(List.of())
                            .photoUrl(null)
                            .build())
                    .toList();

            cafeDetails.addAll(newCafes);
            log.info("🟢 [Kakao] 새로 추가된 카페 {}개 (중복 제거 후 총 {}개)", newCafes.size(), cafeDetails.size());
        } else {
            log.info("✅ Kakao API 호출 불필요 — DB 결과 충분 ({}개)", cafeDetails.size());
        }

        log.info("✅ [Final] 총 {}개 카페 반환 (DB + Kakao)", cafeDetails.size());

        return cafeDetails;
    }

    /**
     * 3-1. kakao API 보조 호출
     */
    private List<CafeDTO> fetchNearbyFromKakao(double latitude, double longitude, int radius) {
        List<CafeDTO> cafes = new ArrayList<>();
        try {
            String url = UriComponentsBuilder.newInstance()
                    .scheme("https")
                    .host("dapi.kakao.com")
                    .path("/v2/local/search/category.json")
                    .queryParam("category_group_code", "CE7")
                    .queryParam("x", longitude)
                    .queryParam("y", latitude)
                    .queryParam("radius", radius)
                    .build()
                    .toUriString();

            log.info("🚀 [Kakao Nearby] Request URL: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
                    }
            );

            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("documents")) {
                log.warn("⚠️ [Kakao Nearby] Response body is null or missing 'documents' key");
                return cafes;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> documents = (List<Map<String, Object>>) body.get("documents");
            if (documents == null || documents.isEmpty()) {
                log.info("ℹ️ [Kakao Nearby] No cafes found from Kakao API");
                return cafes;
            }

            for (Map<String, Object> doc : documents) {
                try {
                    String name = (String) doc.getOrDefault("place_name", "");
                    String address = (String) doc.getOrDefault("road_address_name",
                            doc.getOrDefault("address_name", ""));
                    String yStr = (String) doc.get("y");
                    String xStr = (String) doc.get("x");

                    BigDecimal lat = null;
                    BigDecimal lon = null;

                    if (yStr != null && !yStr.isBlank() && xStr != null && !xStr.isBlank()) {
                        lat = BigDecimal.valueOf(Double.parseDouble(yStr));
                        lon = BigDecimal.valueOf(Double.parseDouble(xStr));
                    } else {
                        log.warn("⚠️ [Kakao Nearby] Invalid coordinates for '{}'", name);
                    }

                    cafes.add(CafeDTO.builder()
                            .name(name)
                            .address(address)
                            .latitude(lat != null ? lat : BigDecimal.ZERO)
                            .longitude(lon != null ? lon : BigDecimal.ZERO)
                            .build());
                } catch (Exception inner) {
                    log.warn("⚠️ [Kakao Nearby] Skipping invalid cafe entry: {}", inner.getMessage());
                }

                log.info("✅ [Kakao Nearby] {} cafes fetched successfully", cafes.size());
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("❌ [Kakao Nearby] API HTTP error: {}", e.getStatusCode());
        } catch (ResourceAccessException e) {
            log.error("❌ [Kakao Nearby] Network access error: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ [Kakao Nearby] Unexpected error: {}", e.getMessage(), e);
        }

        return cafes;
    }


    /**
     * 4. 랜덤 카페 10개 조회
     */
    @Transactional(readOnly = true)
    public List<CafeDetailResponse> getRandomCafes() {
        List<CafeEntity> cafes = cafeRepository.findRandom10();
        log.info("🎲 랜덤으로 선택된 카페 개수: {}", cafes.size());

        return cafes.stream()
                .map(this::toDetailResponse)   // ✅ 공통 변환 메서드 (이미 CafeService에 있음)
                .toList();
    }

    /**
     * 5. 종합 인기점수 기반 요즘 뜨는 카페 10개 조회
     */
    @Transactional(readOnly = true)
    public List<CafeDetailResponse> getHotCafesWeighted(double w7d, double wAll, double wRate, double wRev) {
        List<CafeEntity> hotCafes = cafeRepository.findHotWeightedNative(w7d, wAll, wRate, wRev);

        return hotCafes.stream().map(cafe -> {
            // ✅ 태그명 리스트
            List<String> tags = cafeRepository.findTagNamesByCafeId(cafe.getCafeId());

            // ✅ 리뷰는 ReviewService가 알아서 ReviewResponseDTO로 변환
            List<ReviewResponseDTO> reviews = reviewService.getReviewsByCafeId(cafe.getCafeId());
            return CafeDetailResponse.builder()
                    .id(cafe.getCafeId())
                    .name(cafe.getName())
                    .address(cafe.getAddress())
                    .phone(cafe.getPhone())
                    .rating(String.valueOf(cafe.getKakaoRating()))
                    .hours(cafe.getOpenHours())
                    .reviewsSummary(cafe.getReviewsSummary())
                    .reviews(reviews)
                    .tags(tags)
                    .photoUrl(cafe.getPhotoUrl())
                    .build();
        }).toList();
    }

    /**
     * 6. 찜 많은 카페 10개 조회
     */
    @Transactional(readOnly = true)
    public List<CafeDetailResponse> getTopWishlistedCafes(int limit) {
        List<CafeEntity> cafes = cafeRepository.findTopWishlistedCafesFull(limit);

        return cafes.stream()
                .map(cafe -> {
                    // ✅ 리뷰는 ReviewService가 알아서 ReviewResponseDTO로 변환
                    List<ReviewResponseDTO> reviews = reviewService.getReviewsByCafeId(cafe.getCafeId());

                    // ✅ 태그명 리스트
                    List<String> tagNames = cafeRepository.findTagNamesByCafeId(cafe.getCafeId());

                    return CafeDetailResponse.builder()
                            .id(cafe.getCafeId())
                            .name(cafe.getName())
                            .address(cafe.getAddress())
                            .phone(cafe.getPhone())
                            .rating(cafe.getKakaoRating() != null
                                    ? String.format("%.2f", cafe.getKakaoRating())
                                    : "0.00")
                            .hours(cafe.getOpenHours())
                            .reviewsSummary(cafe.getReviewsSummary())
                            .reviews(reviews)   // ✅ ReviewResponseDTO 그대로 전달
                            .tags(tagNames)
                            .photoUrl(cafe.getPhotoUrl())
                            .build();
                })
                .toList();
    }

    /**
     * 7. 리뷰 조회 메서드
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCafeReviews(Long cafeId) {
        // 1️⃣ 카페 존재 여부 검증
        if (!cafeRepository.existsById(cafeId)) {
            throw new IllegalArgumentException("해당 ID의 카페를 찾을 수 없습니다. id=" + cafeId);
        }

        // 2️⃣ 리뷰 전체 조회 (이미 ReviewService에 getReviewsByCafeId 있음)
        List<ReviewResponseDTO> reviews = reviewService.getReviewsByCafeId(cafeId);

        // 3️⃣ Map 형태로 반환 (reviews, count)
        Map<String, Object> result = new HashMap<>();
        result.put("reviews", reviews);
        result.put("count", reviews.size());

        log.info("💬 [CafeService] cafeId={} 리뷰 {}개 반환", cafeId, reviews.size());
        return result;
    }


    /**
     * ✅ 공통 변환 메서드: CafeEntity → CafeDetailResponse
     */
    private CafeDetailResponse toDetailResponse(CafeEntity entity) {
        List<String> tags = cafeRepository.findTagNamesByCafeId(entity.getCafeId());
        List<ReviewResponseDTO> reviews = reviewService.getReviewsByCafeId(entity.getCafeId());

        BigDecimal rating = entity.getAvgRating() != null
                ? entity.getAvgRating()
                : entity.getKakaoRating();

        return CafeDetailResponse.builder()
                .id(entity.getCafeId())
                .name(entity.getName())
                .address(entity.getAddress())
                .phone(entity.getPhone())
                .hours(entity.getOpenHours())
                .rating(rating != null ? String.format("%.2f", rating) : "0.00")
                .reviewsSummary(entity.getReviewsSummary())
                .reviews(reviews)
                .tags(tags)
                .photoUrl(entity.getPhotoUrl())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .build();
    }

    /**
     * cafe_id 리스트를 받아 각 카페별 찜 개수를 Map으로 반환
     * ex) {1=52, 2=31, 3=0, ...}
     */
    private Map<Long, Integer> buildWishCountMap(List<Long> cafeIds) {
        if (cafeIds == null || cafeIds.isEmpty()) return Collections.emptyMap();

        List<Object[]> rows = cafeRepository.countWishByCafeIds(cafeIds);
        Map<Long, Integer> result = new HashMap<>();
        for (Object[] row : rows) {
            Long id = ((Number) row[0]).longValue();
            Integer count = ((Number) row[1]).intValue();
            result.put(id, count);
        }
        return result;
    }

}