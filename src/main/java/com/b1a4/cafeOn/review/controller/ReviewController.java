package com.b1a4.cafeOn.review.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.review.dto.ReviewDTO;
import com.b1a4.cafeOn.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성(JSON)
    @PostMapping("/cafes/{cafeId}/reviews")
    public ResponseEntity<?> createReview(@AuthenticationPrincipal String userId, @PathVariable Long cafeId,
                                          @RequestBody ReviewDTO reviewDTO) {
        try {
            ReviewDTO review = reviewService.createReview(reviewDTO, userId, cafeId);

            ApiResponse<ReviewDTO> response = ApiResponse.<ReviewDTO>builder()
                    .data(review)
                    .message("리뷰가 작성되었습니다.")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("JSON 리뷰 작성 실패 userId:{}, cafeId:{}", userId, cafeId, e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // todo
    // 리뷰 작성(JSON+멀티파트)
    // @PostMapping("/cafes/{cafeId}/reviews")


    // 리뷰 수정
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<?> updateReview(@AuthenticationPrincipal String userId,
                                          @PathVariable Long reviewId, @RequestBody ReviewDTO reviewDTO) {
        try {
            ReviewDTO review = reviewService.updateReview(reviewDTO, userId, reviewId);

            ApiResponse<ReviewDTO> response = ApiResponse.<ReviewDTO>builder()
                    .data(review)
                    .build();

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            log.error("JSON 리뷰 수정 실패 userId:{}, reviewId:{}", userId, reviewId, e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // todo
    // 리뷰 수정(JSON+멀티파트)
    // @PutMapping("/reviews/{reviewId}")


    // 리뷰 삭제
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@AuthenticationPrincipal String userId, @PathVariable Long reviewId) {
        try {
            reviewService.deleteReview(userId, reviewId);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("리뷰 삭제 실패", e);

            return ResponseEntity.noContent().build();
        }
    }


    // 내가 작성한 리뷰 조회 fixme: mypageController에 작성


    // @PostMapping("/reviews/{reviewId}/reports")
    // 리뷰 신고 fixme:report 도메인에 로직 추가

}
