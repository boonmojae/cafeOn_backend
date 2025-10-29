package com.b1a4.cafeOn.cafe.dto;

import com.b1a4.cafeOn.review.dto.ReviewResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "카페 상세 응답 DTO")
public class CafeDetailResponse {

    @Schema(description = "카페 ID", example = "123")
    private Long id;

    @Schema(description = "카페 이름", example = "카페온 2025")
    private String name;

    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
    private String address;

    @Schema(description = "전화번호", example = "02-123-4567")
    private String phone;

    @Schema(description = "평균 별점", example = "3.90")
    private String rating;

    @Schema(description = "카페 사진 URL 리스트")
    private List<String> photos;

    @Schema(description = "영업시간", example = "월 10:00~20:00")
    private String hours;

    @Schema(description = "현재 영업 여부", example = "true")
    private boolean isOpen;

    @Schema(description = "후기 요약", example = "조용하고 감성적인 분위기의 브런치 카페입니다.")
    private String reviewsSummary;

    @Schema(description = "후기 목록")
    private List<ReviewResponseDTO> reviews;    // todo: 민재가 구현할 reviewDTO던 내가 만들던? 해야함 민재코드 보자

    @Schema(description = "태그 목록")
    private  List<String> tags;

    @Getter
    @AllArgsConstructor
    @Schema(description = "후기 정보 DTO")
    public static class ReviewDTO {
        @Schema(example = "김도이")
        private String author;

        @Schema(example = "4.8")
        private double rating;

        @Schema(example = "분위기 좋고 커피 맛있어요!")
        private String content;

        @Schema(example = "2025-10-25T14:32:00")
        private LocalDateTime createdAt;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "연관 카페 정보 DTO")
    public static class RelatedCafeDTO {
        @Schema(example = "456")
        private Long id;

        @Schema(example = "스타벅스 강남점")
        private String name;

        @Schema(example = "htpps://cdn.cafeon.kr/photos/456-thumb.jpg")
        private String thumbnail;
    }
}
