package com.b1a4.cafeOn.community.comment.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.community.comment.dto.CommentLikeResponseDTO;
import com.b1a4.cafeOn.community.comment.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    @PostMapping("/{commentId}/like")
    public ResponseEntity<?> toggleLikes(@AuthenticationPrincipal String userId, @PathVariable Long commentId) {

        CommentLikeResponseDTO responseDTO = commentLikeService.toggleLike(commentId, userId);
        
        String message = responseDTO.liked() ? "좋아요가 반영되었습니다." : "좋아요가 취소되었습니다.";

        ApiResponse<CommentLikeResponseDTO> response = ApiResponse.<CommentLikeResponseDTO>builder()
                .message(message)
                .data(responseDTO)
                .build();

        return ResponseEntity.ok().body(response);

    }

}
