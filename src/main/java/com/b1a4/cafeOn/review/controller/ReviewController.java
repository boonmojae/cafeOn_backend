package com.b1a4.cafeOn.review.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.community.post.dto.PostDetailResponseDTO;
import com.b1a4.cafeOn.image.enums.ImageCategory;
import com.b1a4.cafeOn.image.service.S3Service;
import com.b1a4.cafeOn.review.dto.ReviewDTO;
import com.b1a4.cafeOn.review.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(
            value = "/cafes/{cafeId}/reviews",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> createReview(
            @AuthenticationPrincipal String userId,
            @PathVariable Long cafeId,
            @RequestParam(value = "review", required = true) String reviewJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {


        ReviewDTO reviewDTO;
        try {
            reviewDTO = objectMapper.readValue(reviewJson, ReviewDTO.class);
        } catch (Exception e) {
            log.warn("review 파트 파싱 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .message("review 파트(JSON) 파싱 실패")
                            .build()
            );
        }

        List<S3Service.UploadedImageInfo> uploadedInfos = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                S3Service.UploadedImageInfo info =
                        s3Service.uploadImage(file, ImageCategory.REVIEW);
                uploadedInfos.add(info);
            }
        }

        ReviewDTO saved = reviewService.createReview(
                userId,
                cafeId,
                reviewDTO,
                uploadedInfos
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.builder()
                        .data(saved)
                        .message("리뷰가 작성되었습니다.")
                        .build());
    }


    // @PutMapping("/reviews/{reviewId}")

    // @DeleteMapping("/reviews/{reviewId}")


    // 내가 작성한 리뷰 조회 fixme: mypageController에 작성
    // @GetMappint("/my/reviews")


}