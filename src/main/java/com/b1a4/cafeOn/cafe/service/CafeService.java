package com.b1a4.cafeOn.cafe.service;

import com.b1a4.cafeOn.cafe.dto.CafeDTO;
import com.b1a4.cafeOn.cafe.entity.CafeEntity;
import com.b1a4.cafeOn.cafe.repository.CafeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// /api/cafes/**
// 카페 검색, 상세 조회, 리뷰 연결
@Slf4j
@Service
public class CafeService {
    @Autowired private CafeRepository cafeRepository;

//    1. 카페 검색 (검색어/정렬기준/태그 필터)
    public List<CafeDTO> searchCafes(String query, String sort, String tag) {
        List<CafeEntity> cafes;

//        1-1. 태그만 선택된 경우
        if (query == null && sort == null && tag != null) {
            cafes = cafeRepository.findByTag(tag);
        }
//        1-2. 검색어만 들어온 경우
        else if (query != null && sort == null && tag == null) {
            cafes = cafeRepository.searchByQuery(query);
        }
//        1-3. 정렬기준만 들어온 경우
        else if (query == null && sort != null && tag == null) {
            cafes = switch (sort.toLowerCase()) {
                case "rating" -> cafeRepository.findAllOrderByRating(); // 평점순
                case "wish" -> cafeRepository.findAllOrderByWishCount();    // 찜 많은 순
                case "random" -> cafeRepository.findRandom(); // 랜덤
                default -> cafeRepository.findAll();
            };
        }
//        1-4. 검색어 + 정렬
        else if (query != null && sort != null && tag == null) {
            cafes = cafeRepository.searchByQueryAndSort(query, sort);
        }
//        1-5. 검색어 + 태그
        else if (query != null && tag != null && sort == null) {
            cafes = cafeRepository.searchByQueryAndTag(query, tag);
        }
//        1-6. 태그 + 정렬
        else if (tag != null && sort != null && query == null) {
            cafes = cafeRepository.searchByTagAndSort(tag, sort);
        }
//        1-7. 검색어 + 정렬 + 태그 전부 들어온 경우
        else if (query != null && sort != null && tag != null) {
            cafes = cafeRepository.searchByAllFilters(query, sort, tag);
        }
//        1-8. 아무것도 없을 때 (전체 조회)
        else {
            cafes = cafeRepository.findAll();
        }

        return cafes.stream().map(CafeDTO::fromEntity).toList();
    }
}