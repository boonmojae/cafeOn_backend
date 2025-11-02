package com.b1a4.cafeOn.review.dto;

import com.b1a4.cafeOn.image.dto.ImageResponseDTO;
import com.b1a4.cafeOn.review.entity.ReviewEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ReviewResponseDTO", description = "카페 리뷰 상세/목록 아이템 DTO")
public class ReviewResponseDTO {

    @Schema(description = "리뷰 ID", example = "101")
    private Long reviewId;

    @Schema(description = "평점(1~5)", example = "5")
    private int rating;

    @Schema(description = "리뷰 본문", example = "몽블랑 빙수 정말 맛있어요!")
    private String content;

    @Schema(description = "작성 시각(ISO-8601)", example = "2025-11-02T14:20:00")
    private LocalDateTime createdAt;

    @Schema(description = "카페 ID", example = "12")
    private Long cafeId;

    @Schema(description = "카페 이름", example = "카페 모노")
    private String cafeName;

    @Schema(description = "작성자 ID", example = "userA")
    private String reviewerId;

    @Schema(description = "작성자 닉네임", example = "모짜")
    private String reviewerNickname;

    @Schema(description = "작성자 프로필 이미지 URL", example = "https://example.com/u/mozart.png", nullable = true)
    private String reviewerProfileImageUrl;

    @Schema(description = "첨부 이미지 목록", nullable = true)
    private List<ImageResponseDTO> images;

    public static ReviewResponseDTO fromEntity(ReviewEntity reviewEntity) {
        List<ImageResponseDTO> imageDTOs = reviewEntity.getImages().stream()
                .map(ImageResponseDTO::from)
                .collect(Collectors.toList());

        return ReviewResponseDTO.builder()
                .reviewId(reviewEntity.getReviewId())
                .rating(reviewEntity.getRating())
                .content(reviewEntity.getContent())
                .createdAt(reviewEntity.getCreatedAt())
                .cafeId(reviewEntity.getCafe().getCafeId())
                .cafeName(reviewEntity.getCafe().getName())
                .reviewerId(reviewEntity.getUser().getUserId())
                .reviewerNickname(reviewEntity.getUser().getNickname())
                .reviewerProfileImageUrl(reviewEntity.getUser().getProfileImage())
                .images(imageDTOs)
                .build();
    }

    public static List<ReviewResponseDTO> fromEntities(List<ReviewEntity> entities) {
        return entities.stream()
                .map(ReviewResponseDTO::fromEntity)
                .toList();
    }
}
