package com.b1a4.cafeOn.report.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.report.dto.ReportRequestDTO;
import com.b1a4.cafeOn.report.dto.ReportResponseDTO;
import com.b1a4.cafeOn.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    // 게시글 신고
    @PostMapping("/posts/{postId}/reports")
    public ResponseEntity<?> reportPost(@AuthenticationPrincipal String userId, @PathVariable Long postId,
                                        @Valid @RequestBody ReportRequestDTO reportRequestDTO) {

        try {
            ReportResponseDTO responseDTO = reportService.reportPost(userId, postId, reportRequestDTO);

            ApiResponse<ReportResponseDTO> response = ApiResponse.<ReportResponseDTO>builder()
                    .data(responseDTO)
                    .message("게시글 신고 성공")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("게시글 신고 실패 userId:{}, postId:{}", userId, postId, e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }

    }

    // 댓글 신고
    @PostMapping("/comments/{commentId}/reports")
    public ResponseEntity<?> reportComment(@AuthenticationPrincipal String userId, @PathVariable Long commentId,
                                           @Valid @RequestBody ReportRequestDTO reportRequestDTO) {
        try {
            ReportResponseDTO responseDTO = reportService.reportComment(userId, commentId, reportRequestDTO);

            ApiResponse<ReportResponseDTO> response = ApiResponse.<ReportResponseDTO>builder()
                    .data(responseDTO)
                    .message("댓글 신고 성공")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("댓글 신고 실패 userId:{}, commentId:{}", userId, commentId, e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }

    }


    // 리뷰 신고
    @PostMapping("/reviews/{reviewId}/reports")
    public ResponseEntity<?> reportReview(@AuthenticationPrincipal String userId, @PathVariable Long reviewId,
                                          @Valid @RequestBody ReportRequestDTO reportRequestDTO) {
        try {

            ReportResponseDTO responseDTO = reportService.reportReview(userId, reviewId, reportRequestDTO);

            ApiResponse<ReportResponseDTO> response = ApiResponse.<ReportResponseDTO>builder()
                    .data(responseDTO)
                    .message("리뷰 신고 성공")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("리뷰 신고 실패 userId:{}, reviewId:{}", userId, reviewId);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


}
