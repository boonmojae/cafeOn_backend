package com.b1a4.cafeOn.review.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.image.enums.ImageCategory;
import com.b1a4.cafeOn.image.service.S3Service;
import com.b1a4.cafeOn.image.service.UploadedImageInfo;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "리뷰 생성/조회/수정/삭제 API")
@SecurityRequirement(name = "Bearer Authentication")
public class ReviewController {

    private final ReviewService reviewService;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(
            summary = "리뷰 작성",
            description = "multipart/form-data로 리뷰(JSON 문자열)와 이미지들을 업로드하여 새 리뷰를 생성합니다. `review` 파트에는 ReviewRequestDTO JSON 문자열을 넣습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "리뷰 생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReviewEnvelope.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 형식 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageOnly.class))
            )
    })
    @PostMapping(
            value = "/cafes/{cafeId}/reviews",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> createReview(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable Long cafeId,
            @RequestPart(value = "review", required = true) String reviewJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        ReviewRequestDTO reviewDTO;
        try {
            reviewDTO = objectMapper.readValue(reviewJson, ReviewRequestDTO.class);
        } catch (Exception e) {
            log.warn("review 파트(JSON) 파싱 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.<ReviewResponseDTO>builder()
                            .message("review 파트(JSON) 파싱 실패")
                            .build()
            );
        }

        List<UploadedImageInfo> uploadedInfos = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                uploadedInfos.add(s3Service.uploadImage(file, ImageCategory.REVIEW));
            }
        }

        ReviewResponseDTO saved = reviewService.createReview(
                userId,
                cafeId,
                reviewDTO,
                uploadedInfos
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ReviewResponseDTO>builder()
                        .data(saved)
                        .message("리뷰가 작성되었습니다.")
                        .build());
    }

    @Operation(
            summary = "리뷰 수정",
            description = "multipart/form-data로 리뷰(JSON 문자열)와 추가 이미지를 업로드하여 리뷰를 수정합니다. `review` 파트에는 ReviewUpdateRequestDTO JSON 문자열을 넣습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "리뷰 수정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReviewEnvelope.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageOnly.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageOnly.class))
            )
    })
    @PutMapping(
            value = "/reviews/{reviewId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> updateReview(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable Long reviewId,
            @RequestPart(value = "review", required = true) String reviewJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        ReviewUpdateRequestDTO reviewDTO;
        try {
            reviewDTO = objectMapper.readValue(reviewJson, ReviewUpdateRequestDTO.class);
        } catch (Exception e) {
            log.warn("review 파트(JSON) 파싱 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.<ReviewResponseDTO>builder()
                            .message("review 파트(JSON) 파싱 실패")
                            .build()
            );
        }

        List<UploadedImageInfo> newlyUploadedInfos = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                newlyUploadedInfos.add(s3Service.uploadImage(file, ImageCategory.REVIEW));
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
                    ApiResponse.<ReviewResponseDTO>builder()
                            .message("리뷰가 수정되었습니다.")
                            .data(updated)
                            .build()
            );

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<ReviewResponseDTO>builder()
                            .message("이 리뷰를 수정할 권한이 없습니다.")
                            .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<ReviewResponseDTO>builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @Operation(summary = "리뷰 삭제", description = "리뷰를 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공")
    })
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(userId, reviewId);
        return ResponseEntity.noContent().build();
    }

    @Schema(name = "ApiResponse<ReviewResponseDTO>")
    static class ReviewEnvelope {
        public String message;
        public ReviewResponseDTO data;
    }

    @Schema(name = "ApiResponse<MessageOnly>")
    static class MessageOnly {
        public String message;
    }
}
