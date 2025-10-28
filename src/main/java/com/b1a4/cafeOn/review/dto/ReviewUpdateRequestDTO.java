package com.b1a4.cafeOn.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewUpdateRequestDTO {

    private Integer rating;
    private String content;

    @Builder.Default
    private List<Long> existingImageIds = new ArrayList<>();
}
