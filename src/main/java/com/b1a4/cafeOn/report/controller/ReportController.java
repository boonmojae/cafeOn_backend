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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "신고(게시글/댓글/리뷰) API")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "게시글 신고", description = "postId에 해당하는 게시글을 신고합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "신고 접수 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReportEnvelope.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageOnly.class))
            )
    })
    @PostMapping("/posts/{postId}/reports")
    public ResponseEntity<ApiResponse<ReportResponseDTO>> reportPost(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable Long postId,
            @Valid @RequestBody ReportRequestDTO reportRequestDTO
    ) {
        try {
            ReportResponseDTO dto = reportService.reportPost(userId, postId, reportRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<ReportResponseDTO>builder()
                            .data(dto)
                            .message("신고가 접수되었습니다.")
                            .build());
        } catch (Exception e) {
            log.error("게시글 신고 실패 userId:{}, postId:{}", userId, postId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<ReportResponseDTO>builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @Operation(summary = "댓글 신고", description = "commentId에 해당하는 댓글을 신고합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "신고 접수 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReportEnvelope.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageOnly.class))
            )
    })
    @PostMapping("/comments/{commentId}/reports")
    public ResponseEntity<ApiResponse<ReportResponseDTO>> reportComment(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable Long commentId,
            @Valid @RequestBody ReportRequestDTO reportRequestDTO
    ) {
        try {
            ReportResponseDTO dto = reportService.reportComment(userId, commentId, reportRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<ReportResponseDTO>builder()
                            .data(dto)
                            .message("신고가 접수되었습니다.")
                            .build());
        } catch (Exception e) {
            log.error("댓글 신고 실패 userId:{}, commentId:{}", userId, commentId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<ReportResponseDTO>builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @Operation(summary = "리뷰 신고", description = "reviewId에 해당하는 리뷰를 신고합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "신고 접수 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReportEnvelope.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageOnly.class))
            )
    })
    @PostMapping("/reviews/{reviewId}/reports")
    public ResponseEntity<ApiResponse<ReportResponseDTO>> reportReview(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReportRequestDTO reportRequestDTO
    ) {
        try {
            ReportResponseDTO dto = reportService.reportReview(userId, reviewId, reportRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<ReportResponseDTO>builder()
                            .data(dto)
                            .message("리뷰 신고 성공")
                            .build());
        } catch (Exception e) {
            log.error("리뷰 신고 실패 userId:{}, reviewId:{}", userId, reviewId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<ReportResponseDTO>builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @Schema(name = "ApiResponse<ReportResponseDTO>")
    static class ReportEnvelope {
        public String message;
        public ReportResponseDTO data;
    }

    @Schema(name = "ApiResponse<MessageOnly>")
    static class MessageOnly {
        public String message;
    }
}
