package com.b1a4.cafeOn.community.comment.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.community.comment.dto.CommentLikeResponseDTO;
import com.b1a4.cafeOn.community.comment.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "댓글 좋아요 API")
@SecurityRequirement(name = "Bearer Authentication")
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    @Operation(summary = "댓글 좋아요 토글", description = "댓글에 대한 좋아요를 토글합니다.")
    @PostMapping("/{commentId}/like")
    public ResponseEntity<ApiResponse<CommentLikeResponseDTO>> toggleLikes(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable Long commentId
    ) {
        CommentLikeResponseDTO dto = commentLikeService.toggleLike(commentId, userId);
        String message = dto.liked() ? "좋아요가 반영되었습니다." : "좋아요가 취소되었습니다.";
        ApiResponse<CommentLikeResponseDTO> body = ApiResponse.<CommentLikeResponseDTO>builder()
                .message(message)
                .data(dto)
                .build();
        return ResponseEntity.ok(body);
    }
}
