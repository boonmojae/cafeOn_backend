package com.b1a4.cafeOn.review.dto;

import com.b1a4.cafeOn.image.dto.ImageResponseDTO;
import com.b1a4.cafeOn.review.entity.ReviewEntity;
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
public class ReviewResponseDTO {

    private Long reviewId;
    private int rating;
    private String content;
    private LocalDateTime createdAt;

    private Long cafeId;
    private String cafeName;

    private String reviewerId;
    private String reviewerNickname;
    private String reviewerProfileImageUrl;

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
