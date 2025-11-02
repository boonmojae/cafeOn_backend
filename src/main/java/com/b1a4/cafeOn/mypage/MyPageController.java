package com.b1a4.cafeOn.mypage;

import com.b1a4.cafeOn.chat.dto.room.ChatRoomListItemDTO;
import com.b1a4.cafeOn.chat.service.ChatRoomMemberService;
import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.community.comment.dto.CommentResponseDTO;
import com.b1a4.cafeOn.community.comment.service.CommentService;
import com.b1a4.cafeOn.community.post.dto.PostListResponseDTO;
import com.b1a4.cafeOn.community.post.service.PostService;
import com.b1a4.cafeOn.review.dto.ReviewResponseDTO;
import com.b1a4.cafeOn.review.service.ReviewService;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "MyPage", description = "마이페이지 API (내 글/댓글/좋아요/채팅방/리뷰 목록)")
@SecurityRequirement(name = "Bearer Authentication")
public class MyPageController {

    private final PostService postService;
    private final CommentService commentService;
    private final ChatRoomMemberService chatRoomMemberService;
    private final ReviewService reviewService;

    @Operation(summary = "내가 작성한 게시글 목록", description = "내가 작성한 게시글을 최신순으로 페이징 조회합니다.")
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<Page<PostListResponseDTO>>> myPosts(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        try {
            Page<PostListResponseDTO> pageResult = postService.getPostsById(userId, pageable);
            ApiResponse<Page<PostListResponseDTO>> body = ApiResponse.<Page<PostListResponseDTO>>builder()
                    .message("내가 작성한 게시글 조회 성공")
                    .data(pageResult)
                    .build();
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("내가 작성한 게시글 조회 실패 userId:{} ", userId, e);
            ApiResponse<Page<PostListResponseDTO>> error = ApiResponse.<Page<PostListResponseDTO>>builder()
                    .message("내가 작성한 게시글 조회 실패")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "내가 좋아요한 게시글 목록", description = "내가 좋아요한 게시글을 최신순으로 페이징 조회합니다.")
    @GetMapping("/likes/posts")
    public ResponseEntity<ApiResponse<Page<PostListResponseDTO>>> likedPost(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        try {
            Page<PostListResponseDTO> pageResult = postService.getLikePostById(userId, pageable);
            ApiResponse<Page<PostListResponseDTO>> body = ApiResponse.<Page<PostListResponseDTO>>builder()
                    .message("내가 좋아요한 게시글 조회 성공")
                    .data(pageResult)
                    .build();
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("내가 좋아요한 게시글 조회 실패 userId:{}", userId, e);
            ApiResponse<Page<PostListResponseDTO>> error = ApiResponse.<Page<PostListResponseDTO>>builder()
                    .message("내가 좋아요한 게시글 조회 실패")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "내가 작성한 댓글 목록", description = "내가 작성한 댓글을 최신순으로 페이징 조회합니다.")
    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<Page<CommentResponseDTO>>> myComments(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        try {
            Page<CommentResponseDTO> pageResult = commentService.getCommentByUserId(userId, pageable);
            ApiResponse<Page<CommentResponseDTO>> body = ApiResponse.<Page<CommentResponseDTO>>builder()
                    .message("내가 작성한 댓글 조회 성공")
                    .data(pageResult)
                    .build();
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("내가 작성한 댓글 조회 실패 userId:{}", userId, e);
            ApiResponse<Page<CommentResponseDTO>> error = ApiResponse.<Page<CommentResponseDTO>>builder()
                    .message("내가 작성한 댓글 조회 실패")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "내가 좋아요한 댓글 목록", description = "내가 좋아요한 댓글을 최신순으로 페이징 조회합니다.")
    @GetMapping("/likes/comments")
    public ResponseEntity<ApiResponse<Page<CommentResponseDTO>>> likedComments(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        try {
            Page<CommentResponseDTO> pageResult = commentService.getLikeCommentByUserId(userId, pageable);
            ApiResponse<Page<CommentResponseDTO>> body = ApiResponse.<Page<CommentResponseDTO>>builder()
                    .message("내가 좋아요한 댓글 조회 성공")
                    .data(pageResult)
                    .build();
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("내가 좋아요한 댓글 조회 실패 userId:{}", userId, e);
            ApiResponse<Page<CommentResponseDTO>> error = ApiResponse.<Page<CommentResponseDTO>>builder()
                    .message("내가 좋아요한 댓글 조회 실패")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "내가 참여한 채팅방 목록", description = "내가 참여한 DM/그룹 채팅방 목록을 페이징 조회합니다.")
    @GetMapping("/chat/rooms")
    public ResponseEntity<ApiResponse<Page<ChatRoomListItemDTO>>> myRooms(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @ParameterObject
            @PageableDefault(size = 10) Pageable pageable
    ) {
        try {
            Page<ChatRoomListItemDTO> pageResult = chatRoomMemberService.listMyRooms(userId, pageable);
            ApiResponse<Page<ChatRoomListItemDTO>> body = ApiResponse.<Page<ChatRoomListItemDTO>>builder()
                    .message("내가 참여한 채팅방 목록 조회 성공")
                    .data(pageResult)
                    .build();
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("내가 참여한 채팅방 목록 조회 실패 userId:{}", userId, e);
            ApiResponse<Page<ChatRoomListItemDTO>> error = ApiResponse.<Page<ChatRoomListItemDTO>>builder()
                    .message("내가 참여한 채팅방 목록 조회 실패")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "내가 작성한 리뷰 목록", description = "내가 작성한 카페 리뷰를 최신순으로 페이징 조회합니다.")
    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewResponseDTO>>> myReviews(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        try {
            Page<ReviewResponseDTO> pageResult = reviewService.getReviewById(userId, pageable);
            ApiResponse<Page<ReviewResponseDTO>> body = ApiResponse.<Page<ReviewResponseDTO>>builder()
                    .message("내가 작성한 리뷰 목록 조회 성공")
                    .data(pageResult)
                    .build();
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("내가 작성한 리뷰 목록 조회 실패 userId:{}", userId, e);
            ApiResponse<Page<ReviewResponseDTO>> error = ApiResponse.<Page<ReviewResponseDTO>>builder()
                    .message("내가 작성한 리뷰 목록 조회 실패")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}
