package com.b1a4.cafeOn.community.comment.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.community.comment.dto.CommentRequestDTO;
import com.b1a4.cafeOn.community.comment.dto.CommentResponseDTO;
import com.b1a4.cafeOn.community.comment.service.CommentService;
import jakarta.validation.Valid;
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
    // (parentId는 body 우선, path의 parentId는 선택)
    @PostMapping({"/posts/{postId}/comments", "/posts/{postId}/comments/{parentId}"})
    public ResponseEntity<?> createComment(@AuthenticationPrincipal String userId,
                                           @PathVariable Long postId, @PathVariable(required = false) Long parentId,
                                           @Valid @RequestBody CommentRequestDTO commentRequestDTO
    ) {
        try {
            CommentResponseDTO responseDTO = commentService.createComment(userId, postId, parentId, commentRequestDTO);

            ApiResponse<CommentResponseDTO> response = ApiResponse.<CommentResponseDTO>builder()
                    .data(responseDTO)
                    .message("댓글이 추가되었습니다")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("댓글 생성 실패", e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 게시글 상세 댓글 트리(루트 페이징)
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<?> getCommentsTree(@AuthenticationPrincipal String userId, @PathVariable Long postId,
                                             @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        try {
            Page<CommentResponseDTO> page = commentService.getPostCommentsAsTree(postId, userId, pageable);
            ApiResponse<Page<CommentResponseDTO>> response = ApiResponse.<Page<CommentResponseDTO>>builder()
                    .data(page)
                    .message("게시글 상세 댓글 트리 조회 성공")
                    .build();

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.error("댓글 트리 조회 실패", e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 특정 댓글 상세 조회
    // includeChildren=true 이면 서브트리까지
    @GetMapping("/comments/{commentId}")
    public ResponseEntity<?> getComment(@AuthenticationPrincipal String userId, @PathVariable Long commentId,
                                        @RequestParam(defaultValue = "false") boolean includeChildren
    ) {
        try {
            CommentResponseDTO responseDTO = includeChildren
                    ? commentService.getCommentWithChildren(commentId, userId)
                    : commentService.findCommentById(commentId, userId);

            ApiResponse<CommentResponseDTO> response = ApiResponse.<CommentResponseDTO>builder()
                    .data(responseDTO)
                    .message("댓글 단건(및 서브트리) 조회 성공")
                    .build();

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.error("댓글 단건(및 서브트리) 조회 실패", e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .message(e.getMessage())
                            .build()
            );
        }
    }


    // 내가 작성한 댓글 목록
    // fixme: mypage
    @GetMapping("/my/comments")
    public ResponseEntity<?> findCommentByUserId(@AuthenticationPrincipal String userId,
                                                 @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        try {
            Page<CommentResponseDTO> page = commentService.getCommentByUserId(userId, pageable);
            ApiResponse<Page<CommentResponseDTO>> response = ApiResponse.<Page<CommentResponseDTO>>builder()
                    .data(page)
                    .message("내가 작성한 댓글 목록 조회 성공")
                    .build();
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.error("내 댓글 목록 조회 실패", e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // 내가 좋아요한 댓글 목록
    // fixme: mypage
    @GetMapping("/my/comments/likes")
    public ResponseEntity<?> findLikeCommentByUserId(@AuthenticationPrincipal String userId,
                                                     @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {

            Page<CommentResponseDTO> page = commentService.getLikeCommentByUserId(userId, pageable);
            ApiResponse<Page<CommentResponseDTO>> response = ApiResponse.<Page<CommentResponseDTO>>builder()
                    .data(page)
                    .message("내 좋아요 댓글 목록 조회 성공")
                    .build();

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            log.error("내 좋아요 댓글 목록 조회 실패", e);
            ApiResponse<?> errorRepsonse = ApiResponse.builder()
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(errorRepsonse);
        }
    }


    // 댓글 수정
    @PutMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<?> updateComment(@AuthenticationPrincipal String userId,
                                           @PathVariable Long commentId, @PathVariable Long postId,
                                           @Valid @RequestBody CommentRequestDTO commentRequestDTO
    ) {
        try {
            CommentResponseDTO responseDTO = commentService.updateComment(commentId, postId, userId, commentRequestDTO);
            ApiResponse<CommentResponseDTO> response = ApiResponse.<CommentResponseDTO>builder()
                    .data(responseDTO)
                    .message("댓글이 수정되었습니다.")
                    .build();
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.error("댓글 수정 실패", e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


    // 댓글 삭제
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@AuthenticationPrincipal String userId,
                                           @PathVariable Long commentId, @PathVariable Long postId
    ) {
        try {
            commentService.deleteComment(commentId, postId, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("댓글 삭제 실패", e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);

        }
    }
}
