package com.b1a4.cafeOn.community.comment.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.community.comment.dto.CommentRequestDTO;
import com.b1a4.cafeOn.community.comment.dto.CommentResponseDTO;
import com.b1a4.cafeOn.community.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 생성
    @PostMapping({"/posts/{postId}/comments", "/posts/{postId}/comments/{parentId}"})
    public ResponseEntity<?> createComment(@AuthenticationPrincipal String userId,
                                           @PathVariable Long postId, @PathVariable(required = false) Long parentId,
                                           @RequestBody CommentRequestDTO commentRequestDTO) {

        try {

            CommentResponseDTO commentResponseDTO = commentService.createComment(userId, postId, parentId, commentRequestDTO);

            ApiResponse<CommentResponseDTO> response = ApiResponse.<CommentResponseDTO>builder()
                    .data(commentResponseDTO)
                    .message("댓글이 추가되었습니다.")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {

            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message("댓글 추'가에 실패했습니다.")
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 댓글 목록 조회
    @GetMapping("/comments")
    public ResponseEntity<?> getAllComments(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        try {

            Page<CommentResponseDTO> responseDTOS = commentService.getAllComments(pageable);

            ApiResponse<Page<CommentResponseDTO>> response = ApiResponse.<Page<CommentResponseDTO>>builder()
                    .data(responseDTOS)
                    .message("댓글 목록을 조회했습니다.")
                    .build();

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            log.error("전체 댓글 조회 중 오류 발생", e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message("댓글을 조회할 수 없습니다.")
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


    // 특정 댓글 상세 조회

    // 내가 작성한 댓글 목록

    // 댓글 수정

    // 댓글 삭제

}
