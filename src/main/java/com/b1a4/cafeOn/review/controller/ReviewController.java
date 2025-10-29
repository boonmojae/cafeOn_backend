package com.b1a4.cafeOn.review.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.image.enums.ImageCategory;
import com.b1a4.cafeOn.image.service.S3Service;
import com.b1a4.cafeOn.review.dto.ReviewRequestDTO;
import com.b1a4.cafeOn.review.dto.ReviewResponseDTO;
import com.b1a4.cafeOn.review.dto.ReviewUpdateRequestDTO;
import com.b1a4.cafeOn.review.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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


        ReviewRequestDTO reviewDTO;
        try {
            reviewDTO = objectMapper.readValue(reviewJson, ReviewRequestDTO.class);
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

        ReviewResponseDTO saved = reviewService.createReview(
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


    @PutMapping(
            value = "/reviews/{reviewId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateReview(@AuthenticationPrincipal String userId,
                                          @PathVariable Long reviewId,
                                          @RequestParam(value = "review", required = true) String reviewJson,
                                          @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        ReviewUpdateRequestDTO reviewDTO;
        try {
            reviewDTO = objectMapper.readValue(reviewJson, ReviewUpdateRequestDTO.class);
        } catch (Exception e) {
            log.warn("review 파트 파싱 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .message("review 파트(JSON) 파싱 실패")
                            .build()
            );
        }

        List<S3Service.UploadedImageInfo> newlyUploadedInfos = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                S3Service.UploadedImageInfo info =
                        s3Service.uploadImage(file, ImageCategory.REVIEW);
                newlyUploadedInfos.add(info);
            }
        }

        try {
            ReviewResponseDTO updated = reviewService.updateReview(
                    userId,
                    reviewId,
                    reviewDTO,
                    newlyUploadedInfos
            );

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .message("리뷰가 수정되었습니다.")
                            .data(updated)
                            .build()
            );

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.builder()
                            .message("이 리뷰를 수정할 권한이 없습니다.")
                            .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .message(e.getMessage())
                            .build());
        }

    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(@AuthenticationPrincipal String userId, @PathVariable Long reviewId) {
        reviewService.deleteReview(userId, reviewId);
        return ResponseEntity.noContent().build();
    }

}
