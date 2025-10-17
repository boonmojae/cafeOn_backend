package com.b1a4.cafeOn.cafe.service;

import com.b1a4.cafeOn.cafe.dto.CafeDTO;
import com.b1a4.cafeOn.cafe.entity.CafeEntity;
import com.b1a4.cafeOn.cafe.repository.CafeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

// /api/cafes/**
// 카페 검색, 상세 조회, 리뷰 연결
@Slf4j
@Service
public class CafeService {
    @Autowired private CafeRepository cafeRepository;
    private final KakaoMapService kakaoMapService;

//    1. 태그로 검색
    public List<CafeDTO> searchCafes(String query, String tag) {
        List<CafeEntity> cafes;

//        1-1. 태그만 선택된 경우
        if (query == null && tag != null) {
            cafes = cafeRepository.findByTag(tag);
        }
//        1-2. 검색어만 들어온 경우
        else if (query != null && tag == null) {
//            cafes = cafeRepository.searchByQuery(query);
            cafes = 카카오맵RESTAPI GET요청;
        }
//        1-3. 검색어 + 태그
        else if (query != null && tag != null) {
            cafes = cafeRepository.searchByQueryAndTag(query, tag);
        }
//        1-4. 아무것도 없을 때 (전체 조회)
        else {
            cafes = cafeRepository.findAll();
        }

        return cafes.stream().map(CafeDTO::fromEntity).toList();
    }
}