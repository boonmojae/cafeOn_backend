package com.b1a4.cafeOn.review.dto;

import com.b1a4.cafeOn.cafe.entity.CafeEntity;
import com.b1a4.cafeOn.review.entity.ReviewEntity;
import com.b1a4.cafeOn.user.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDTO {
    private int rating;
    private String content;

    public ReviewEntity toEntity(UserEntity user, CafeEntity cafe) {
        return ReviewEntity.builder()
                .rating(this.rating)
                .content(this.content)
                .user(user)
                .cafe(cafe)
                .build();
    }
}
