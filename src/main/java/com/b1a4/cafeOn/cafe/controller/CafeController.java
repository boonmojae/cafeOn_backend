package com.b1a4.cafeOn.cafe.controller;

import com.b1a4.cafeOn.cafe.dto.CafeDTO;
import com.b1a4.cafeOn.cafe.service.CafeService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Cafe", description = "카페 API")
public class CafeController {
    @Autowired private CafeService cafeService;

//    1. 카페 검색 및 목록 조회 (query/sort/tags 요청바디 기반 검색)
    @GetMapping("/search")
    @Operation(
            summary = "카페 검색 및 목록 조회",
            description = "query, sort, tag(옵션)으로 카페 목록 조회. query 없으면 전체 목록 반환."
    )
    public ResponseEntity<?> searchCafes(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String tag
    ) {
        List<CafeDTO> cafes = cafeService.searchCafes(query, sort, tag);    // required=false로 해당 파라미터가 아예 안 넘어와도 null로 처리해서 들어감
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
