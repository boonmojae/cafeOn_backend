package com.b1a4.cafeOn.mypage;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.community.comment.dto.CommentResponseDTO;
import com.b1a4.cafeOn.community.comment.service.CommentService;
import com.b1a4.cafeOn.community.post.dto.PostListResponseDTO;
import com.b1a4.cafeOn.community.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
@Slf4j
public class MyPageController {

    private final PostService postService;
    private final CommentService commentService;


    // community-post 내가 작성한 게시글
    @GetMapping("/posts")
    public ResponseEntity<?> myPosts(@AuthenticationPrincipal String userId,
                                     @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<PostListResponseDTO> responseDTOS = postService.getPostsById(userId, pageable);

            ApiResponse<Page<PostListResponseDTO>> response = ApiResponse.<Page<PostListResponseDTO>>builder()
                    .data(responseDTOS)
                    .message("내가 작성한 게시글 조회 성공")
                    .build();

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            log.error("내가 작성한 게시글 조회 실패 userId:{} ", userId, e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message("내가 작성한 게시글 조회 실패")
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }

    }


    // community-post 내가 좋아요한 게시글
    @GetMapping("/likes/posts")
    public ResponseEntity<?> likedPost(@AuthenticationPrincipal String userId,
                                       @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable) {
        try {

            Page<PostListResponseDTO> responseDTOS = postService.getLikePostById(userId, pageable);

            ApiResponse<Page<PostListResponseDTO>> response = ApiResponse.<Page<PostListResponseDTO>>builder()
                    .data(responseDTOS)
                    .message("내가 좋아요한 게시글 조회 성공")
                    .build();

            return ResponseEntity.ok().body(response);
             
        } catch (Exception e) {
            log.error("내가 좋아요한 게시글 조회 실패 userId:{}", userId, e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message("내가 좋아요한 게시글 조회 실패")
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


    // community-comment 내가 작성한 댓글
    @GetMapping("/comments")
    public ResponseEntity<?> myComments(@AuthenticationPrincipal String userId,
                                        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable) {
        try {

            Page<CommentResponseDTO> responseDTOS = commentService.getCommentByUserId(userId, pageable);
            
            ApiResponse<Page<CommentResponseDTO>> response = ApiResponse.<Page<CommentResponseDTO>>builder()
                    .data(responseDTOS)
                    .message("내가 작성한 댓글 조회 성공")
                    .build();

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            log.error("내가 작성한 댓글 조회 실패 userId:{}", userId, e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message("내가 작성한 댓글 조회 실패")
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


    // community-comment 내가 좋아요한 댓글
    @GetMapping("/likes/comments")
    public ResponseEntity<?> likedComments(@AuthenticationPrincipal String userId,
                                           @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable) {
        try {

            Page<CommentResponseDTO> responseDTOS = commentService.getLikeCommentByUserId(userId, pageable);

            ApiResponse<Page<CommentResponseDTO>> response = ApiResponse.<Page<CommentResponseDTO>>builder()
                    .data(responseDTOS)
                    .message("내가 좋아요한 댓글 조회 성공")
                    .build();

            return ResponseEntity.ok().body(response);
            
        } catch (Exception e) {
            log.error("내가 좋아요한 댓글조회 실패 userId:{}", userId, e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message("내가 좋아요한 댓글 조회 실패")
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }







}
