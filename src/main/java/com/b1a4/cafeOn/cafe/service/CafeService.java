package com.b1a4.cafeOn.cafe.service;

import com.b1a4.cafeOn.cafe.dto.CafeDTO;
import com.b1a4.cafeOn.cafe.dto.CafeDetailResponse;
import com.b1a4.cafeOn.cafe.dto.CafeNearbyResponse;
import com.b1a4.cafeOn.cafe.entity.CafeEntity;
import com.b1a4.cafeOn.cafe.enums.CafeSource;
import com.b1a4.cafeOn.cafe.repository.CafeRepository;
import com.b1a4.cafeOn.image.entity.ImageEntity;
import com.b1a4.cafeOn.review.dto.ReviewResponseDTO;
import com.b1a4.cafeOn.review.entity.ReviewEntity;
import com.b1a4.cafeOn.review.repository.ReviewRepository;
import com.b1a4.cafeOn.review.service.ReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// /api/cafes/**
// 카페 검색, 상세 조회, 리뷰 연결
@Slf4j
@Service
@EnableAsync    // ✅ Async 동기화를 위한 활성화
@RequiredArgsConstructor    // ✅ @Autowired 대신 생성자 주입 방식 사용
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class CafeService {
    private final CafeRepository cafeRepository;    // ✅ final + RequiredArgsConstructor
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
//    RestTemplate은 Bean으로 등록하고 주입받는 것이 좋으나, 기존 코드를 유지합니다.
    private final RestTemplate restTemplate = new RestTemplate();
    private static final int KAKAO_PAGE_SIZE = 15;
    private static final int KAKAO_MAX_PAGES = 45;

    @Value("${kakao.api.key}")
    private String kakaoApiKey;  // ✅ application.properties 에 .env에서 불러온 "KAKAO_REST_API_KEY=..." 저장해둠
//    private final KakaoMapService kakaoMapService;

    /**
     * 1. 키워드나 태그로 검색
     */
    public List<CafeDTO> searchCafes(String keyword, String tag) {
//        1-1. keyword가 있으면 [카카오맵 REST API(키워드로 장소검색) + DB병합] 로직 실행
        if (keyword != null && !keyword.isEmpty()) {
            return searchAndMerge(keyword);
        }

//        1-2. keyword가 없고 tag나 전체 조회일 경우 (DB만 조회)
        List<CafeEntity> cafes;
        if (tag !=null && !tag.isEmpty()) {
            cafes = cafeRepository.findByTag(tag);
        } else {
            cafes = cafeRepository.findAll();
        }

//        Entity -> DTO 변환 (CafeDTO의 fromEntity static-method 활용)
        return cafes.stream()
                .map(CafeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     *  2. 핵심 로직 : 카카오 API 결과와 DB 데이터를 병합
     */
    private  List<CafeDTO> searchAndMerge(String keyword) {
//        2-1. 카카오 API 호출
        List<Map<String, Object>> documents = fetchFromKakao(keyword);
        if (documents.isEmpty()) {
            return List.of();
        }

//        2-2. 카카오 결과에서 '카페 이름' 목록 추출
        List<String> kakaoCafeNames = documents.stream()
                .map(doc -> (String) doc.get("place_name"))
                .distinct()
                .collect(Collectors.toList());

//        2-3. '이름' 목록으로 DB 일괄 조회 (IN 쿼리 + name 인덱스 활용)
        List<CafeEntity> dbCafes = cafeRepository.findByNameIn(kakaoCafeNames);

//        2-4. 병합 성능 최적화를 위해 DB결과를 Map으로 변환 (Key: 카페이름, Value: CafeEntity)
//        O(N) -> O(1) 조회 속도 향상
        Map<String, CafeEntity> dbCafesMap = dbCafes.stream()
                .collect(Collectors.toMap(CafeEntity::getName, Function.identity(), (db1, db2) -> db1));    // 이름 중복 시 첫 번째 데이터 사용

        List<CafeDTO> mergedResults = new ArrayList<>();

//        2-5. 카카오 API결과(documents)를 순회하며 DB데이터와 병합 (카카오 정렬 순서 유지)
        for (Map<String, Object> doc : documents) {
            String kakaoName = (String) doc.get("place_name");
            String kakaoId = (String) doc.get("id");

            CafeEntity dbCafe = dbCafesMap.get(kakaoName);

            if (dbCafe != null) {
//                2-5-1. DB에 데이터가 있는 경우: DB데이터(avg_rating 등)로 DTO 생성
                mergedResults.add(CafeDTO.fromEntity(dbCafe));
            } else {
//                2-5-2. DB에 데이터가 없는 경우: 카카오 데이터로 DTO 생성 (즉시 응답용)
                mergedResults.add(parseKakaoDocToDTO(doc));

//                2-5-3. @Async: DB에 없는 카페 정보 비동기 저장
                synchronizeCafe(doc, kakaoId);
            }
        }
        return mergedResults;
    }

    /**
     * 키워드 기반 카카오 장소검색: 최대 45페이지(675건)까지 긁어오기
     * - query만 사용 (category_group_code는 keyword.json에 함께 쓰면 케이스에 따라 필터 꼬일 수 있어 제외)
     * - UTF-8 인코딩 보장
     * - meta.is_end == true 시 조기 종료
     */
    private List<Map<String, Object>> fetchFromKakao(String keyword) {
        List<Map<String, Object>> allDocuments = new ArrayList<>();

        try {
            for (int page = 1; page <= 45; page++) {

                // ✅ UriComponentsBuilder에 맡기면 이중 인코딩 없음
                String url = UriComponentsBuilder
                        .fromUriString("https://dapi.kakao.com/v2/local/search/keyword.json")
                        .queryParam("query", keyword)               // 예: "강남 카페"
//                        .queryParam("category_group_code", "CE7")
                        .queryParam("size", 15)
                        .queryParam("page", page)
                        .encode(StandardCharsets.UTF_8)             // ✅ 한글 안전하게 인코딩
                        .toUriString();


                log.info("🚀 Kakao API Request URL: {}", url);

                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", "KakaoAK " + kakaoApiKey);
                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<>() {}
                );

                Map<String, Object> body = response.getBody();
                if (body == null || !body.containsKey("documents")) break;

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> documents = (List<Map<String, Object>>) body.get("documents");

                log.info("📡 Kakao API Response (page {}): {} results", page, documents.size());

                if (documents.isEmpty()) break;
                allDocuments.addAll(documents);

                @SuppressWarnings("unchecked")
                Map<String, Object> meta = (Map<String, Object>) body.get("meta");
                if (meta != null && Boolean.TRUE.equals(meta.get("is_end"))) {
                    log.info("✅ Kakao API last page reached at page {}", page);
                    break;
                }

                if (page >= 5) { // 성능 제한
                    log.info("ℹ️ Page limit reached {}, stop", page);
                    break;
                }
            }

        } catch (Exception e) {
            log.error("❌ Kakao API request failed: {}", e.getMessage(), e);
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
//                todo: open_hours, reviews_summary, kakao_rating 등은 크롤링해야 하는 작업임
                .build();
    }


    /**
     * 2. 카페 상세 정보 조회
     */
    public CafeDetailResponse getCafeDetail(Long id) {
        CafeEntity entity = cafeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 카페를 찾을 수 없습니다. id=" + id));

        // 2-1. 조회수 증가
        entity.setViewCount(entity.getViewCount() + 1);
        entity.setLastViewedAt(LocalDateTime.now());
        cafeRepository.save(entity);

        // 2-2. 리뷰 불러오기 (이미 이미지까지 포함된 DTO 반환됨)
        List<ReviewResponseDTO> reviews = reviewService.getReviewsByCafeId(id);
        log.info("✅ [CafeService] cafeId={} -> reviews.size={}", id, reviews.size());

//        2-3. 태그명 리스트 가져오기
        List<String> tagNames = cafeRepository.findTagNamesByCafeId(id);
        log.info(tagNames.toString());

        return CafeDetailResponse.builder()
                .id(entity.getCafeId())
                .name(entity.getName())
                .address(entity.getAddress())
                .phone(entity.getPhone())
                .hours(entity.getOpenHours())
                .rating(String.valueOf(entity.getKakaoRating()))    //  todo : 우선 리뷰데이터 업어서 전부 걍 카카오크롤링한 별점 때리기
                .reviewsSummary(entity.getReviewsSummary())
                .reviews(reviews)   // ✅ ← CafeDetailResponse.reviews 타입이 List<ReviewResponseDTO> 인지 확인!
                .tags(tagNames)
                .build();
    }


    /**
     * 3. 사용자 위치 기반 근처 카페 조회
     */
    public CafeNearbyResponse getNearbyCafes(double latitude, double longitude, int radius) {

        // 1️⃣ DB 기반 조회
        List<CafeEntity> nearbyFromDB = cafeRepository.findNearbyCafes(latitude, longitude, radius);
        log.info("📍 [DB] 반경 {}m 이내 카페 {}개 조회 (lat={}, lon={})",
                radius, nearbyFromDB.size(), latitude, longitude);

        // 2️⃣ Entity → DTO
        List<CafeDTO> cafeDTOs = nearbyFromDB.stream()
                .map(CafeDTO::fromEntity)
                .toList();

        // 3️⃣ Kakao API 보조 호출 (DB 결과 부족할 때)
        if (cafeDTOs.size() < 10) {
            log.info("⚠️ DB 결과 부족 ({}개) → Kakao API로 보조 조회 시작", cafeDTOs.size());

            List<CafeDTO> kakaoCafes = fetchNearbyFromKakao(latitude, longitude, radius);

            Set<String> existingNames = cafeDTOs.stream()
                    .map(CafeDTO::getName)
                    .collect(Collectors.toSet());

            List<CafeDTO> newCafes = kakaoCafes.stream()
                    .filter(c -> !existingNames.contains(c.getName()))
                    .toList();

            cafeDTOs.addAll(newCafes);
            log.info("🟢 [Kakao] 새로 추가된 카페 {}개 (중복 제거 후 총 {}개)", newCafes.size(), cafeDTOs.size());
        } else {
            log.info("✅ Kakao API 호출 불필요 — DB 결과 충분 ({}개)", cafeDTOs.size());
        }

        // 4️⃣ 최종 반환 로그
        log.info("✅ [Final] 총 {}개 카페 반환 (DB + Kakao)", cafeDTOs.size());

        return CafeNearbyResponse.builder()
                .cafes(cafeDTOs)
                .build();
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

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {}
            );

            Object docsObj = Objects.requireNonNull(Objects.requireNonNull(response.getBody()).get("documents"));
            if (docsObj instanceof List<?>) {
                for (Object doc : (List<?>) docsObj) {
                    if (doc instanceof Map<?, ?>) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> mapDoc = (Map<String, Object>) doc;
                        cafes.add(CafeDTO.builder()
                                .name((String) mapDoc.get("place_name"))
                                .address((String) mapDoc.get("address_name"))
                                .latitude(BigDecimal.valueOf(Double.parseDouble((String) mapDoc.get("y"))))
                                .longitude(BigDecimal.valueOf(Double.parseDouble((String) mapDoc.get("x"))))
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            log.error("❌ Kakao API 호출 실패: {}", e.getMessage());
        }
        return cafes;
    }

    /**
     * 4. 랜덤 카페 10개 조회
     */
    public List<CafeDTO> getRandomCafes() {
        List<CafeEntity> cafes = cafeRepository.findRandom10();
        log.info("🎲 랜덤으로 선택된 카페 개수: {}", cafes.size());
        return cafes.stream()
                .map(cafe -> CafeDTO.builder()
                        .cafeId(cafe.getCafeId())
                        .name(cafe.getName())
                        .address(cafe.getAddress())
                        .latitude(cafe.getLatitude())
                        .longitude(cafe.getLongitude())
                        .phone(cafe.getPhone())
                        .avgRating(cafe.getKakaoRating())   // todo : 아직은 avg_rating 없어서 카카오별점으로 설정
                        .openHours(cafe.getOpenHours())
                        .reviewsSummary(cafe.getReviewsSummary())
                        .build())
                .toList();
    }

}