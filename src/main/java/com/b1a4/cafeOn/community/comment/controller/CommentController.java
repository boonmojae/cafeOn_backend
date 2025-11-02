package com.b1a4.cafeOn.community.comment.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.community.comment.dto.CommentRequestDTO;
import com.b1a4.cafeOn.community.comment.dto.CommentResponseDTO;
import com.b1a4.cafeOn.community.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@Slf4j
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "댓글 생성/조회/수정/삭제 API")
@SecurityRequirement(name = "Bearer Authentication")
public class CommentController {

    private final CommentService commentService;

    @Operation(
            summary = "댓글 생성",
            description = "게시글(postId)에 댓글을 생성합니다. parentId를 전달하면 대댓글로 등록됩니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "댓글 생성 성공")
    @PostMapping({"/posts/{postId}/comments", "/posts/{postId}/comments/{parentId}"})
    public ResponseEntity<ApiResponse<CommentResponseDTO>> createComment(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable Long postId,
            @PathVariable(required = false) Long parentId,
            @Valid @RequestBody CommentRequestDTO commentRequestDTO
    ) {
        try {
            CommentResponseDTO dto = commentService.createComment(userId, postId, parentId, commentRequestDTO);
            ApiResponse<CommentResponseDTO> body = ApiResponse.<CommentResponseDTO>builder()
                    .data(dto)
                    .message("댓글이 추가되었습니다")
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (Exception e) {
            log.error("댓글 생성 실패", e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.<CommentResponseDTO>builder()
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Operation(
            summary = "게시글 상세 댓글 트리 조회",
            description = "게시글의 루트 댓글을 페이징으로 조회하고, 각 루트 댓글의 대댓글(children)을 포함합니다."
    )
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Page<CommentResponseDTO>>> getCommentsTree(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable Long postId,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        try {
            Page<CommentResponseDTO> page = commentService.getPostCommentsAsTree(postId, userId, pageable);
            ApiResponse<Page<CommentResponseDTO>> body = ApiResponse.<Page<CommentResponseDTO>>builder()
                    .data(page)
                    .message("게시글 상세 댓글 트리 조회 성공")
                    .build();
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("댓글 트리 조회 실패", e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.<Page<CommentResponseDTO>>builder()
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Operation(
            summary = "댓글 단건 조회",
            description = "특정 댓글을 조회합니다. includeChildren=true면 해당 댓글의 서브트리를 함께 반환합니다."
    )
    @GetMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponseDTO>> getComment(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "false") boolean includeChildren
    ) {
        try {
            CommentResponseDTO dto = includeChildren
                    ? commentService.getCommentWithChildren(commentId, userId)
                    : commentService.findCommentById(commentId, userId);

            ApiResponse<CommentResponseDTO> body = ApiResponse.<CommentResponseDTO>builder()
                    .data(dto)
                    .message("댓글 단건(및 서브트리) 조회 성공")
                    .build();

            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("댓글 단건(및 서브트리) 조회 실패", e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.<CommentResponseDTO>builder()
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Operation(summary = "댓글 수정", description = "댓글 내용을 수정합니다.")
    @PutMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponseDTO>> updateComment(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable Long commentId,
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequestDTO commentRequestDTO
    ) {
        try {
            CommentResponseDTO dto = commentService.updateComment(commentId, postId, userId, commentRequestDTO);
            ApiResponse<CommentResponseDTO> body = ApiResponse.<CommentResponseDTO>builder()
                    .data(dto)
                    .message("댓글이 수정되었습니다.")
                    .build();
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("댓글 수정 실패", e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.<CommentResponseDTO>builder()
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "댓글 삭제 성공")
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable Long commentId,
            @PathVariable Long postId
    ) {
        try {
            commentService.deleteComment(commentId, postId, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("댓글 삭제 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
