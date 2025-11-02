package com.b1a4.cafeOn.community.post.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.community.post.dto.PostLikeResponseDTO;
import com.b1a4.cafeOn.community.post.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RequestMapping("/api/posts")
@RestController
@RequiredArgsConstructor
@Tag(name = "Posts", description = "커뮤니티 게시글 API")
@SecurityRequirement(name = "Bearer Authentication")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @Operation(summary = "게시글 좋아요 토글", description = "게시글에 대한 좋아요를 토글합니다.")
    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<PostLikeResponseDTO>> toggleLikes(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable("id") Long postId
    ) {
        PostLikeResponseDTO dto = postLikeService.toggleLike(postId, userId);
        String message = dto.liked() ? "좋아요가 반영되었습니다." : "좋아요가 취소되었습니다";
        ApiResponse<PostLikeResponseDTO> body = ApiResponse.<PostLikeResponseDTO>builder()
                .message(message)
                .data(dto)
                .build();
        return ResponseEntity.ok(body);
    }
}
