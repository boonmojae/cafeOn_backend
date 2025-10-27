package com.b1a4.cafeOn.review.dto;

import com.b1a4.cafeOn.cafe.entity.CafeEntity;
import com.b1a4.cafeOn.image.dto.ImageResponseDTO;
import com.b1a4.cafeOn.image.entity.ImageEntity;
import com.b1a4.cafeOn.review.entity.ReviewEntity;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewDTO {
    private Long reviewId;
    private int rating;
    private String content;
    private LocalDateTime createdAt;
    private boolean reported;

    private Long cafeId;
    private String cafeName;
    private String reviewerId;
    private String reviewerNickname;
    private String reviewerProfileImageUrl;

    private List<ImageResponseDTO> images;

    @Builder.Default
    private List<Long> existingImageIds = new ArrayList<>();

    public static ReviewEntity toEntity(ReviewDTO reviewDTO, UserEntity user, CafeEntity cafe) {
        return ReviewEntity.builder()
                .rating(reviewDTO.getRating())
                .content(reviewDTO.getContent())
                .user(user)
                .cafe(cafe)
                .build();
    }

    public static ReviewDTO fromEntity(ReviewEntity reviewEntity) {

        List<ImageResponseDTO> imageResponseDTOS = reviewEntity.getImages().stream()
                .map(ImageResponseDTO::from)
                .collect(Collectors.toList());


        return ReviewDTO.builder()
                .reviewId(reviewEntity.getReviewId())
                .rating(reviewEntity.getRating())
                .content(reviewEntity.getContent())
                .createdAt(reviewEntity.getCreatedAt())
                .reported(reviewEntity.isReported())
                .cafeId(reviewEntity.getCafe().getCafeId())
                .cafeName(reviewEntity.getCafe().getName())
                .reviewerId(reviewEntity.getUser().getUserId())
                .reviewerNickname(reviewEntity.getUser().getNickname())
                .reviewerProfileImageUrl(reviewEntity.getUser().getProfileImage())
                .images(imageResponseDTOS)
                .build();
    }

}