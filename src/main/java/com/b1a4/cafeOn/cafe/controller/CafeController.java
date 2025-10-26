package com.b1a4.cafeOn.cafe.controller;

import com.b1a4.cafeOn.cafe.dto.CafeDTO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
                                            schema = @Schema(implementation = CafeDTO.class)
                                    ),
                                    examples = @ExampleObject(value = """
                                            [
                                              {
                                                "cafeId": 101,
                                                "name": "폴바셋 강남역점",
                                                "address": "서울 강남구 테헤란로 123",
                                                "latitude": 37.498,
                                                "longitude": 127.028,
                                                "phone": "02-123-4567",
                                                "avgRating": 4.35,
                                                "reviewsSummary": "커피가 진하고 분위기가 좋아요 ☕",
                                                "wishlistCount": 52
                                              },
                                              {
                                                "cafeId": null,
                                                "name": "스타벅스 강남2호점",
                                                "address": "서울 강남구 강남대로 420",
                                                "latitude": 37.5002,
                                                "longitude": 127.0274,
                                                "phone": "02-777-9999",
                                                "avgRating": 0.00,
                                                "reviewsSummary": null,
                                                "wishlistCount": 0
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
    public ResponseEntity<?> searchCafes(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tag
    ) {
        List<CafeDTO> cafes = cafeService.searchCafes(query, tag);    // required=false로 해당 파라미터가 아예 안 넘어와도 null로 처리해서 들어감
        return ResponseEntity.ok(cafes);
        
    }

//    2. 요즘 뜨고 있는 카페 순위별 조회 (hot10) todo: 찜+리뷰데이터 필요
//    최근 찜 + 리뷰 수 통계 SQL집계 (30일 기준)

//    3. 찜 많은 카페 순위별 조회 (wish10) todo: wishlists 테이블 필요

//    4. 랜덤 카페 (random10)

//    5. 사용자 맞춤 카페 순위별 조회 (related10) todo: 임시로 랜덤/지역기반 -> ai

//    6. 사용자 위치 기반 근처 카페 목록 조회 (latitude+longitude + Haversine 공식)
//    @GetMapping("/nearby")
//    public ResponseEntity<?> getNearbyCafes() {}

//    7. 카페 상세 정보 조회
//    @GetMapping("/{id}")
//    public ResponseEntity<?>  getDetailInfos() {}

//    8. 서울시 카페 전체 목록 조회
//    @GetMapping("/")
//    public ResponseEntity<?> getAllCafes() {}
}
