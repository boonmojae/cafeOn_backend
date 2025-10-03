package com.b1a4.cafeOn.community.post.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.community.post.dto.PostLikeResponseDTO;
import com.b1a4.cafeOn.community.post.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/posts")
@RestController
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/{id}/likes")
    public ResponseEntity<?> toggleLikes(@AuthenticationPrincipal String userId, @PathVariable("id") Long postId) {

        PostLikeResponseDTO responseDTO = postLikeService.toggleLike(postId, userId);

        String message = responseDTO.liked() ? "좋아요가 반영되었습니다." : "좋아요가 취소되었습니다";

        ApiResponse<PostLikeResponseDTO> response = ApiResponse.<PostLikeResponseDTO>builder()
                .message(message)
                .data(responseDTO)
                .build();

        return ResponseEntity.ok().body(response);
    }

    // fixme: 마이페이지 브랜치로 이동
    // 내가 좋아요한 게시글 목록 전체
}
