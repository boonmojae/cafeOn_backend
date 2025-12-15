package com.b1a4.cafeOn.cafe.controller;

import com.b1a4.cafeOn.cafe.dto.CafeDTO;
import com.b1a4.cafeOn.cafe.dto.CafeDetailResponse;
import com.b1a4.cafeOn.cafe.dto.CafeNearbyResponse;
import com.b1a4.cafeOn.cafe.service.CafeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;
import java.util.Map;

// 카페 검색, 관련 카페 추천 등 읽기 전용 도메인 (모든 사용자 접근 가능 - permitAll)
@Slf4j
@RestController
@RequestMapping("/api/cafes")
@Tag(name = "Cafe", description = "☕ 카페 API: 카페 검색, 목록 조회, 상세정보 조회, 추천, 근처카페 등")
public class CafeController {
    @Autowired private CafeService cafeService;

    /**
     * 1. 카페 검색 및 목록 조회 (query/tags 요청바디 기반 검색 + query는 KAKAO REST API 키워드로 장소검색)
     * @param query
     * @param tag
     * @return ResponseEntity.ok(cafes)
     */
    @GetMapping("/search")
    @Operation(
            summary = "🔍 카페 검색 및 목록 조회",
            description = """
                query(검색어) 또는 tag(태그)로 카페 목록을 조회합니다.
                - query가 있으면: Kakao REST API의 카페 검색 결과와 DB 데이터를 병합해서 반환합니다.
                - tag만 있으면: DB 내 해당 태그의 카페를 조회합니다.
                - query/tag 둘 다 없으면: 전체 카페 목록을 반환합니다.
                """,
            parameters = {
                    @Parameter(
                            name = "query",
                            description = "검색 키워드 (예: '강남 카페') — Kakao Map API에서 장소검색",
                            example = "강남 카페"
                    ),
                    @Parameter(
                            name = "tag",
                            description = "필터링 태그 (예: '감성적인', '조용한', '데이트하기좋은' 등)",
                            example = "조용한"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "✅ 성공: 카페 목록 조회 완료",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = CafeDetailResponse.class)
                                    ),
                                    examples = @ExampleObject(value = """
                                        [
                                          {
                                            "id": 1144,
                                            "name": "로찌커피 논현점",
                                            "address": "서울 강남구 강남대로128길 4",
                                            "phone": "02-543-1009",
                                            "latitude": 37.5096807246915,
                                            "longitude": 127.022634417315,
                                            "rating": "3.8",
                                            "hours": "월 10:30 ~ 23:00\\n화 10:30 ~ 23:00\\n수 10:30 ~ 23:00...",
                                            "reviewsSummary": "분위기도 고급스럽고 예쁘며, 디저트도 맛있어요.",
                                            "reviews": [],
                                            "tags": ["분위기있는", "감성적인", "프라이빗한", "카페거리위치", "고급스러운"],
                                            "photoUrl": "https://img1.kakaocdn.net/...jpg",
                                            "wishlistCount": 0
                                          },
                                          {
                                            "id": 18988,
                                            "name": "까사넬로",
                                            "address": "서울 강남구 봉은사로29길 10",
                                            "phone": "0507-1333-2984",
                                            "latitude": 37.5084596371926,
                                            "longitude": 127.03470320344,
                                            "rating": "3.8",
                                            "hours": "월 12:00 ~ 20:00...",
                                            "reviewsSummary": "맛있는 케이크와 커피까지 즐길 수 있는 곳입니다.",
                                            "reviews": [],
                                            "tags": ["감성적인", "분위기있는", "디저트맛집"],
                                            "photoUrl": "https://img1.kakaocdn.net/...jpg",
                                            "wishlistCount": 3
                                          }
                                        ]
                                        """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "❌ 잘못된 요청 (파라미터 누락 또는 형식 오류)",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                        {
                                          "error": "Invalid parameter",
                                          "message": "query 또는 tag 중 하나는 필수입니다."
                                        }
                                        """))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "❗ 서버 내부 오류",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                        {
                                          "error": "Internal Server Error",
                                          "message": "Kakao API 호출 중 오류가 발생했습니다."
                                        }
                                        """))
                    )
            }
    )
    public ResponseEntity<List<CafeDetailResponse>> searchCafes(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) List<String> tags) {
        // required=false로 해당 파라미터가 아예 안 넘어와도 null로 처리되어 들어감
        // tags 배열이 있으면 첫 번째 태그 사용 (호환성 유지)
        String tagParam = (tags != null && !tags.isEmpty()) ? tags.get(0) : tag;
        List<CafeDetailResponse> cafes = cafeService.searchCafes(query, tagParam);
        return ResponseEntity.ok(cafes);
    }


    /**
     * 2. 카페 상세 정보 조회
     */
    @Operation(
            summary = "카페 상세 정보 조회",
            description = "특정 카페의 id를 기반으로 상세 정보를 조회합니다.",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "조회할 카페의 고유 ID",
                            required = true,
                            example = "1001"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "카페 상세 정보 조회 성공",
                            content = @Content(schema = @Schema(implementation = CafeDTO.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "해당 ID의 카페를 찾을 수 없습니다."),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<CafeDetailResponse> getCafeDetail(@PathVariable Long id) {
        CafeDetailResponse response = cafeService.getCafeDetail(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 3. 사용자 위치 기반 근처 카페 조회
     */
    @GetMapping("/nearby")
    @Operation(
            summary = "📍 사용자 위치 기반 근처 카페 조회",
            description = """
            사용자의 현재 위치(latitude, longitude)를 기반으로
            지정 반경(radius, 단위: m) 내의 카페 목록을 DB에서 조회합니다.
            만약 결과가 적으면 Kakao Map API를 통해 추가 카페를 보강합니다.
            """,
            parameters = {
                    @Parameter(name = "latitude", example = "37.4979"),
                    @Parameter(name = "longitude", example = "127.0276"),
                    @Parameter(name = "radius", example = "1000")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "✅ 성공: 근처 카페 목록 조회 완료",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = CafeDetailResponse.class))
                            )
                    )
            }
    )
    public ResponseEntity<List<CafeDetailResponse>> getNearbyCafes(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "20000") int radius
    ) {
        List<CafeDetailResponse> cafes = cafeService.getNearbyCafes(latitude, longitude, radius);
        return ResponseEntity.ok(cafes);
    }


    /**
     * 4. 랜덤 카페 10개 조회
     */
    @GetMapping("/random10")
    @Operation(
            summary = "🎲 랜덤 카페 10개 조회",
            description = """
            전체 카페 데이터 중 무작위로 10개를 반환합니다.
            - 성능 참고: 데이터가 매우 큰 경우 ORDER BY RAND() 대신 샘플링/캐시 전략을 고려하세요.
            """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "✅ 성공: 랜덤 카페 10개 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = CafeDTO.class)),
                                    examples = @ExampleObject(name = "success_example", value = """
                                        [
                                          {
                                            "cafeId": 101,
                                            "name": "폴바셋 강남역점",
                                            "address": "서울 강남구 테헤란로 123",
                                            "latitude": 37.498023,
                                            "longitude": 127.027579,
                                            "phone": "02-123-4567",
                                            "avgRating": 4.2,
                                            "reviewsSummary": "진한 에스프레소, 넓은 좌석, 콘센트 많음",
                                            "wishlistCount": 52
                                          },
                                          {
                                            "cafeId": 207,
                                            "name": "어글리베이커리 성수점",
                                            "address": "서울 성동구 아차산로 89",
                                            "latitude": 37.54321,
                                            "longitude": 127.05567,
                                            "phone": "02-345-6789",
                                            "avgRating": 4.6,
                                            "reviewsSummary": "빵이 특히 맛있고 직원이 친절함",
                                            "wishlistCount": 31
                                          }
                                        ]
                                        """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "❗ 서버 내부 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(name = "error_example", value = """
                                        {
                                          "error": "Internal Server Error",
                                          "message": "랜덤 조회 중 예기치 못한 오류가 발생했습니다."
                                        }
                                        """)
                            )
                    )
            }
    )
    public ResponseEntity<List<CafeDetailResponse>> getRandomCafes() {
        List<CafeDetailResponse> randomCafes = cafeService.getRandomCafes();
        return ResponseEntity.ok(randomCafes);
    }

    /**
     * 5. 종합 인기지수 기반 요즘 뜨는 카페 10개 조회
     */
    @GetMapping("/hot10")
    @Operation(
            summary = "🔥 종합 인기 지수 기반 요즘 뜨는 카페 Top 10",
            description = """
    최근 7일 조회수, 누적 조회수, 평균 평점, 리뷰 수를 가중합으로 계산해 상위 10개를 반환합니다.
    가중치는 쿼리 파라미터로 조절할 수 있습니다.
    hot_score = views_last7d*w7d + view_count*wAll + avg_rating*50*wRate + review_count*5*wRev
    """,
            parameters = {
                    @Parameter(name = "w7d",  description = "최근 7일 조회수 가중치", example = "0.4"),
                    @Parameter(name = "wAll", description = "누적 조회수 가중치",   example = "0.2"),
                    @Parameter(name = "wRate",description = "평균 평점 가중치",   example = "0.2"),
                    @Parameter(name = "wRev", description = "리뷰 수 가중치",     example = "0.2")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "✅ 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = CafeDTO.class)),
                                    examples = @ExampleObject(value = """
                [
                  {
                    "cafeId": 637639,
                    "name": "미묘",
                    "address": "서울 서대문구 연희로11길 41",
                    "latitude": 37.57,
                    "longitude": 126.93,
                    "phone": "",
                    "openHours": "월 13:00 ~ 19:00 ...",
                    "avgRating": 4.3,
                    "reviewsSummary": "치즈케이크 맛있고 사진 스팟 많음"
                  }
                ]
                """)
                            )
                    )
            }
    )
    public ResponseEntity<List<CafeDetailResponse>> getHotCafesWeighted(
            @RequestParam(defaultValue = "0.4") double w7d,
            @RequestParam(defaultValue = "0.2") double wAll,
            @RequestParam(defaultValue = "0.2") double wRate,
            @RequestParam(defaultValue = "0.2") double wRev
    ) {
        List<CafeDetailResponse> hotCafes = cafeService.getHotCafesWeighted(w7d, wAll, wRate, wRev);
        return ResponseEntity.ok(hotCafes);
    }

    /**
     * 6. 찜 많은 카페 Top 10 조회
     */
    @GetMapping("/wish10")
    @Operation(
            summary = "💖 찜 많은 카페 Top 10 조회",
            description = """
                    wishlists 테이블을 기준으로 카페별 찜 개수를 집계하여 상위 10개를 반환합니다.<br><br>
                    - `limit` 파라미터로 원하는 개수를 지정할 수 있습니다. (기본 10개)<br>
                    - 각 카페에 대해 별점, 후기, 태그, 리뷰 요약이 모두 포함됩니다.<br><br>
                    예를 들어 `limit=5`로 호출 시 상위 5개의 인기 카페 정보를 내려줍니다.
                    """,
            parameters = {
                    @Parameter(
                            name = "limit",
                            description = "가져올 카페 개수 (기본값: 10)",
                            example = "10"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "✅ 성공: 찜 많은 카페 목록 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = CafeDetailResponse.class)),
                                    examples = @ExampleObject(value = """
                                            [
                                              {
                                                "id": 1,
                                                "name": "카페온 강남점",
                                                "address": "서울특별시 강남구 테헤란로 123",
                                                "phone": "02-1234-5678",
                                                "rating": "4.85",
                                                "photos": [
                                                  "https://cdn.cafeon.kr/images/reviews/123-1.jpg",
                                                  "https://cdn.cafeon.kr/images/reviews/456-1.jpg"
                                                ],
                                                "hours": "월~금 10:00~21:00 / 주말 11:00~20:00",
                                                "reviewsSummary": "조용하고 감성적인 분위기의 브런치 카페입니다.",
                                                "reviews": [
                                                  {
                                                    "author": "홍길동",
                                                    "rating": 5,
                                                    "content": "분위기 좋고 커피 맛있어요!",
                                                    "createdAt": "2025-10-25T14:32:00"
                                                  },
                                                  {
                                                    "author": "홍길동",
                                                    "rating": 4,
                                                    "content": "좌석 간격이 넓고 조용해서 작업하기 좋았습니다.",
                                                    "createdAt": "2025-10-27T09:45:10"
                                                  }
                                                ],
                                                "tags": ["조용한", "감성적인", "브런치맛집"]
                                              },
                                              {
                                                "id": 2,
                                                "name": "앤드테일 압구정점",
                                                "address": "서울특별시 강남구 압구정로 11길 7",
                                                "phone": "02-555-7890",
                                                "rating": "4.72",
                                                "photos": [
                                                  "https://cdn.cafeon.kr/images/reviews/789-1.jpg"
                                                ],
                                                "hours": "월~일 11:00~22:00",
                                                "reviewsSummary": "인테리어가 세련되고 조용한 분위기",
                                                "reviews": [],
                                                "tags": ["모던한", "데이트하기좋은"]
                                              }
                                            ]
                                            """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "❌ 잘못된 파라미터 (limit 음수 또는 0 등)",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                            {
                                              "error": "Invalid parameter",
                                              "message": "limit은 1 이상이어야 합니다."
                                            }
                                            """))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "❗ 서버 내부 오류",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                            {
                                              "error": "Internal Server Error",
                                              "message": "찜 많은 카페 조회 중 오류가 발생했습니다."
                                            }
                                            """))
                    )
            }
    )
    public ResponseEntity<List<CafeDetailResponse>> getTopWishlistedCafes(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<CafeDetailResponse> topCafes = cafeService.getTopWishlistedCafes(limit);
        log.info("💖 [TopWishlisted] {}개 카페 반환됨", topCafes.size());
        return ResponseEntity.ok(topCafes);
    }


    /**
     * 7. 특정 카페 리뷰 목록 조회
     */
    @GetMapping("/{id}/reviews")
    @Operation(
            summary = "💬 특정 카페 리뷰 목록 조회",
            description = """
                특정 카페의 리뷰 전체 목록을 반환합니다.<br>
                별도의 sort나 page 파라미터는 사용하지 않으며, 
                단순히 cafe_id 기준으로 모든 리뷰와 총 개수를 내려줍니다.
                """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "조회할 카페의 고유 ID",
                            required = true,
                            example = "1001"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "✅ 성공: 리뷰 목록 및 총 개수 반환",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                        {
                                          "reviews": [
                                            {
                                              "reviewId": 12,
                                              "nickname": "coffee_lover",
                                              "rating": 5,
                                              "content": "분위기 좋고 조용한 카페예요 ☕",
                                              "images": [
                                                "https://cdn.cafeon.kr/reviews/12-1.jpg"
                                              ],
                                              "createdAt": "2025-10-30T14:23:11"
                                            },
                                            {
                                              "reviewId": 13,
                                              "nickname": "latte_holic",
                                              "rating": 4,
                                              "content": "라떼 맛집 인정!",
                                              "images": [],
                                              "createdAt": "2025-10-29T10:02:40"
                                            }
                                          ],
                                          "count": 2
                                        }
                                        """))
                    ),
                    @ApiResponse(responseCode = "404", description = "❌ 해당 카페 또는 리뷰를 찾을 수 없습니다."),
                    @ApiResponse(responseCode = "500", description = "❗ 서버 내부 오류")
            }
    )
    public ResponseEntity<Map<String, Object>> getCafeReviews(@PathVariable Long id) {
        Map<String, Object> response = cafeService.getCafeReviews(id);
        return ResponseEntity.ok(response);
    }


}
