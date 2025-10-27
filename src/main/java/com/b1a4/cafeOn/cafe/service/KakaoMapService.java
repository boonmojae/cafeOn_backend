package com.b1a4.cafeOn.cafe.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoMapService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // 1. 키워드로 중심 좌표 구하기 (ex. "강남역")
    public double[] getCoordinates(String query) {
        String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + query;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        List<Map<String, Object>> documents = (List<Map<String, Object>>) response.getBody().get("documents");

        if (documents.isEmpty()) throw new RuntimeException("검색 결과 없음");

        double lng = Double.parseDouble((String) documents.get(0).get("x"));
        double lat = Double.parseDouble((String) documents.get(0).get("y"));

        return new double[]{lat, lng};
    }

    // 2. 중심 좌표 기준으로 주변 카페 이름 리스트 가져오기
    public List<String> searchNearbyPlaceNames(double lat, double lng, String keyword) {
        String url = String.format(
                "https://dapi.kakao.com/v2/local/search/keyword.json?y=%f&x=%f&radius=1000&query=%s",
                lat, lng, keyword
        );
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        List<Map<String, Object>> docs = (List<Map<String, Object>>) response.getBody().get("documents");

        return docs.stream()
                .map(doc -> (String) doc.get("place_name"))
                .toList();
    }
}