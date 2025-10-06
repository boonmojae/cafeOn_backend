//package com.b1a4.cafeOn.community.comment.controller;
//
//import com.b1a4.cafeOn.common.api.ApiResponse;
//import com.b1a4.cafeOn.community.comment.dto.CommentRequestDTO;
//import com.b1a4.cafeOn.community.comment.dto.CommentResponseDTO;
//import com.b1a4.cafeOn.community.comment.service.CommentService;
//import jakarta.validation.Valid;
//import lombok.Builder;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.web.PageableDefault;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@Slf4j
//@RequestMapping("/api")
//@RequiredArgsConstructor
//public class CommentController {
//
//    private final CommentService commentService;
//
//    // 댓글 생성
//    // fixme: parentId는 body로 받는걸 권장
//    @PostMapping({"/posts/{postId}/comments", "/posts/{postId}/comments/{parentId}"})
//    public ResponseEntity<?> createComment(@AuthenticationPrincipal String userId,
//                                           @PathVariable Long postId, @PathVariable(required = false) Long parentId,
//                                           @RequestBody @Valid CommentRequestDTO commentRequestDTO) {
//
//        try {
//
//            CommentResponseDTO commentResponseDTO = commentService.createComment(userId, postId, parentId, commentRequestDTO);
//
//            ApiResponse<CommentResponseDTO> response = ApiResponse.<CommentResponseDTO>builder()
//                    .data(commentResponseDTO)
//                    .message("댓글이 추가되었습니다.")
//                    .build();
//
//            return ResponseEntity.status(HttpStatus.CREATED).body(response);
//
//        } catch (Exception e) {
//
//            ApiResponse<?> errorResponse = ApiResponse.builder()
//                    .message("댓글 추가에 실패했습니다.")
//                    .build();
//
//            return ResponseEntity.badRequest().body(errorResponse);
//        }
//    }
//
//    // 게시글 상세 댓글 트리(루트 페이징)
//    @GetMapping("/posts/{postId}/comments")
//    public ResponseEntity<?> getCommentsTree(
//            @PathVariable Long postId,
//            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
//
//        try {
//
//            Page<CommentResponseDTO> responseDTO = commentService.getPostCommentsAsTree(postId, pageable);
//
//            ApiResponse<Page<CommentResponseDTO>> response = ApiResponse.<Page<CommentResponseDTO>>builder()
//                    .data(responseDTO)
//                    .message("게시글 상세 댓글 조회 성공")
//                    .build();
//
//            return ResponseEntity.ok().body(response);
//
//        } catch (Exception e) {
//            ApiResponse<?> errorResponse = ApiResponse.builder()
//                    .message(e.getMessage())
//                    .build();
//
//            return ResponseEntity.badRequest().body(errorResponse);
//
//        }
//    }
//
//
//    // 특정 댓글 상세 조회
//    @GetMapping("/comments/{commentId}")
//    public ResponseEntity<?> getComment(@AuthenticationPrincipal String userId, @PathVariable Long commentId,
//                                        @RequestParam(defaultValue = "false") boolean includeChildren) {
//        try {
//            CommentResponseDTO responseDTO = includeChildren
//                    ? commentService.getCommentWithChildren(commentId)
//                    : commentService.findCommentById(commentId, userId);
//
//            ApiResponse<CommentResponseDTO> response = ApiResponse.<CommentResponseDTO>builder()
//                    .data(responseDTO)
//                    .message("댓글을 조회했습니다.")
//                    .build();
//
//            return ResponseEntity.ok().body(response);
//
//        } catch (Exception e) {
//            ApiResponse<?> errorResponse = ApiResponse.builder()
//                    .message(e.getMessage())
//                    .build();
//
//            return ResponseEntity.badRequest().body(errorResponse);
//        }
//
//    }
//
//    // 내가 작성한 댓글 목록 fixme: mypage 브랜치로 이동
//    @GetMapping("/my/comments")
//    public ResponseEntity<?> findCommentByUserId(@AuthenticationPrincipal String userId,
//                                                 @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
//        try {
//            Page<CommentResponseDTO> responseDTOS = commentService.getCommentByUserId(userId, pageable);
//
//            ApiResponse<Page<CommentResponseDTO>> response = ApiResponse.<Page<CommentResponseDTO>>builder()
//                    .data(responseDTOS)
//                    .message("내가 작성한 댓글 목록 조회")
//                    .build();
//
//            return ResponseEntity.ok().body(response);
//
//        } catch (Exception e) {
//            ApiResponse errorResponse = ApiResponse.builder()
//                    .message(e.getMessage())
//                    .build();
//
//            return ResponseEntity.badRequest().body(errorResponse);
//        }
//
//    }
//
//    // 내가 좋아요한 댓글 목록
//    // fixme
//
//    // 댓글 수정
//    @PutMapping("/posts/{postId}/comments/{commentId}")
//    public ResponseEntity<?> updateComment(@AuthenticationPrincipal String userId,
//                                           @PathVariable Long commentId, @PathVariable Long postId,
//                                           @RequestBody @Valid CommentRequestDTO commentRequestDTO) {
//
//        try {
//            CommentResponseDTO responseDTO = commentService.updateComment(commentId, postId, userId, commentRequestDTO);
//
//            ApiResponse<CommentResponseDTO> response = ApiResponse.<CommentResponseDTO>builder()
//                    .data(responseDTO)
//                    .message("댓글이 수정되었습니다.")
//                    .build();
//
//            return ResponseEntity.ok().body(response);
//
//        } catch (Exception e) {
//            ApiResponse<?> errorResponse = ApiResponse.builder()
//                    .message(e.getMessage())
//                    .build();
//
//            return ResponseEntity.badRequest().body(errorResponse);
//        }
//    }
//
//    // 댓글 삭제
//    @DeleteMapping("/posts/{postId}/comments/{commentId}")
//    public ResponseEntity<?> deleteComment(@AuthenticationPrincipal String userId,
//                                           @PathVariable Long commentId, @PathVariable Long postId) {
//
//        try {
//            commentService.deleteComment(commentId, postId, userId);
//
//            return ResponseEntity.noContent().build();
//
//        } catch (Exception e) {
//            ApiResponse<?> errorResponse = ApiResponse.builder()
//                    .message(e.getMessage())
//                    .build();
//
//            return ResponseEntity.badRequest().body(errorResponse);
//        }
//    }
//
//}

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
    public ResponseEntity<?> createComment(
            @AuthenticationPrincipal String userId,
            @PathVariable Long postId,
            @PathVariable(required = false) Long parentId,
            @Valid @RequestBody CommentRequestDTO commentRequestDTO
    ) {
        try {
            CommentResponseDTO dto = commentService.createComment(userId, postId, parentId, commentRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<CommentResponseDTO>builder()
                            .data(dto)
                            .message("댓글이 추가되었습니다.")
                            .build()
            );
        } catch (Exception e) {
            log.error("댓글 생성 실패", e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .message(e.getMessage() != null ? e.getMessage() : "댓글 추가에 실패했습니다.")
                            .build()
            );
        }
    }

    // 게시글 상세 댓글 트리(루트 페이징)
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<?> getCommentsTree(
            @AuthenticationPrincipal String userId,
            @PathVariable Long postId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        try {
            Page<CommentResponseDTO> page = commentService.getPostCommentsAsTree(postId, userId, pageable);
            return ResponseEntity.ok().body(
                    ApiResponse.<Page<CommentResponseDTO>>builder()
                            .data(page)
                            .message("게시글 상세 댓글 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("댓글 트리 조회 실패", e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    // 특정 댓글 상세 조회
    // includeChildren=true 이면 서브트리까지
    @GetMapping("/comments/{commentId}")
    public ResponseEntity<?> getComment(
            @AuthenticationPrincipal String userId, // 비로그인 가능
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "false") boolean includeChildren
    ) {
        try {
            CommentResponseDTO dto = includeChildren
                    ? commentService.getCommentWithChildren(commentId, userId)
                    : commentService.findCommentById(commentId, userId);

            return ResponseEntity.ok().body(
                    ApiResponse.<CommentResponseDTO>builder()
                            .data(dto)
                            .message("댓글을 조회했습니다.")
                            .build()
            );
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
    @GetMapping("/my/comments")
    public ResponseEntity<?> findCommentByUserId(
            @AuthenticationPrincipal String userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        try {
            Page<CommentResponseDTO> page = commentService.getCommentByUserId(userId, pageable);
            return ResponseEntity.ok().body(
                    ApiResponse.<Page<CommentResponseDTO>>builder()
                            .data(page)
                            .message("내가 작성한 댓글 목록 조회")
                            .build()
            );
        } catch (Exception e) {
            log.error("내 댓글 목록 조회 실패", e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .message(e.getMessage())
                            .build()
            );
        }
    }


    // 댓글 수정
    @PutMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<?> updateComment(
            @AuthenticationPrincipal String userId,
            @PathVariable Long commentId,
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequestDTO commentRequestDTO
    ) {
        try {
            CommentResponseDTO dto = commentService.updateComment(commentId, postId, userId, commentRequestDTO);
            return ResponseEntity.ok().body(
                    ApiResponse.<CommentResponseDTO>builder()
                            .data(dto)
                            .message("댓글이 수정되었습니다.")
                            .build()
            );
        } catch (Exception e) {
            log.error("댓글 수정 실패", e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .message(e.getMessage())
                            .build()
            );
        }
    }


    // 댓글 삭제
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @AuthenticationPrincipal String userId,
            @PathVariable Long commentId,
            @PathVariable Long postId
    ) {
        try {
            commentService.deleteComment(commentId, postId, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("댓글 삭제 실패", e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .message(e.getMessage())
                            .build()
            );
        }
    }
}
