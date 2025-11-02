package com.b1a4.cafeOn.cafe.dto;

import com.b1a4.cafeOn.cafe.entity.CafeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "CafeDTO", description = "카페 요약 정보")
public class CafeDTO {
    @Schema(description = "카페 고유 ID", example = "101")
    private Long cafeId;

    @Schema(description = "카페명", example = "폴바셋 강남역점")
    private String name;

    @Schema(description = "주소", example = "서울 강남구 테헤란로 123")
    private String address;

    @Schema(description = "위도", example = "37.498023")
    private BigDecimal latitude;

    @Schema(description = "경도", example = "127.027579")
    private BigDecimal longitude;

    @Schema(description = "전화번호", example = "02-123-4567")
    private String phone;


//    private String photos;    // todo: 대표사진 크롤링해와서 이미지링크 URL로 저장할 photos 컬럼 추가해야함

    @Schema(description = "영업시간", example = "월 9:00 ~ 20:00 ...")
    private String openHours;

    @Schema(description = "평균 평점(없으면 kakaoRating 대체 가능)", example = "4.35")
    private BigDecimal avgRating;

    @Schema(description = "후기 요약", example = "진한 에스프레소, 좌석 넓음, 콘센트 많음")
    private String reviewsSummary;

    @Schema(description = "대표 사진 URL", example = "https://img1.kakaocdn.net/...jpg")
    private String photoUrl;

    @Schema(description = "찜 수", example = "52")
    private Integer wishlistCount;
//    private String relatedCafes;    // todo

//    Cafe API는 읽기(Read) 중심 : DB데이터를 프론트에 맞게 내려줘야 해서 Entity -> DTO 변환이 필요
    public static CafeDTO fromEntity(CafeEntity entity) {
        BigDecimal rating = entity.getAvgRating();

//        ✅ 1. 평점 처리: avgRating > kakaoRating > 0.00
        if (rating == null && entity.getKakaoRating() != null) {
            rating = entity.getKakaoRating();
        }

//        ✅ 2. 리뷰요약 처리: null → 빈 문자열
        String summary = (entity.getReviewsSummary() != null)
                ? entity.getReviewsSummary()
                : "";

        return CafeDTO.builder()
                .cafeId(entity.getCafeId())
                .name(entity.getName())
                .avgRating(rating != null ? rating : BigDecimal.valueOf(0.00))
                .address(entity.getAddress())
                .phone(entity.getPhone())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .openHours(entity.getOpenHours())
                .reviewsSummary(summary)
                .photoUrl(entity.getPhotoUrl())
                .wishlistCount(0)   // Entity에 없으므로 0으로 초기화. Service에서 다시 set됨
                .build();
    }
}