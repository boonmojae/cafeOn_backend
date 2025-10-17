package com.b1a4.cafeOn.cafe.dto;

import com.b1a4.cafeOn.cafe.entity.CafeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// API 요청/응답 맞춤용 => API(클라이언트) 중심의 역할
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CafeDTO {
    private Long cafeId;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phone;
//    private String photos;    // todo: photos 컬럼 추가해야함
    private String openHours;
    private BigDecimal avgRating;
//    private String description; // todo: 컬럼 추가해야함
//    private String reviewsSummary;  // todo: 컬럼 추가 후, AI API 등으로 리뷰요약 해야함
//    private String relatedCafes;    // todo

//    Cafe API는 읽기(Read) 중심 : DB데이터를 프론트에 맞게 내려줘야 해서 Entity -> DTO 변환이 필요
    public static CafeDTO fromEntity(CafeEntity entity) {
        return CafeDTO.builder()
                .cafeId(entity.getCafeId())
                .name(entity.getName())
                .avgRating(BigDecimal.valueOf(entity.getAvgRating() != null ? entity.getAvgRating().doubleValue() : 0.00))
                .address(entity.getAddress())
                .phone(entity.getPhone())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .openHours(entity.getOpen_hours())
                .build();
    }
}