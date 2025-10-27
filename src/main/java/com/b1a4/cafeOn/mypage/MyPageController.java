package com.b1a4.cafeOn.mypage;

import com.b1a4.cafeOn.chat.dto.room.ChatRoomListItemDTO;
import com.b1a4.cafeOn.chat.service.ChatRoomMemberService;
import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.community.comment.dto.CommentResponseDTO;
import com.b1a4.cafeOn.community.comment.service.CommentService;
import com.b1a4.cafeOn.community.post.dto.PostListResponseDTO;
import com.b1a4.cafeOn.community.post.service.PostService;
import com.b1a4.cafeOn.review.dto.ReviewDTO;
import com.b1a4.cafeOn.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
    private final ChatRoomMemberService chatRoomMemberService;
    private final ReviewService reviewService;


    // 내가 작성한 게시글 목록
    @GetMapping("/posts")
    public ResponseEntity<?> myPosts(@AuthenticationPrincipal String userId,
                                     @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<PostListResponseDTO> pageResult = postService.getPostsById(userId, pageable);

            ApiResponse<Page<PostListResponseDTO>> body = ApiResponse.<Page<PostListResponseDTO>>builder()
                    .message("내가 작성한 게시글 조회 성공")
                    .data(pageResult)
                    .build();

            return ResponseEntity.ok(body);

        } catch (Exception e) {
            log.error("내가 작성한 게시글 조회 실패 userId:{} ", userId, e);

            ApiResponse<?> error = ApiResponse.builder()
                    .message("내가 작성한 게시글 조회 실패")
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }


    // 내가 좋아요한 게시글 목록
    @GetMapping("/likes/posts")
    public ResponseEntity<?> likedPost(@AuthenticationPrincipal String userId,
                                       @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<PostListResponseDTO> pageResult = postService.getLikePostById(userId, pageable);

            ApiResponse<Page<PostListResponseDTO>> body = ApiResponse.<Page<PostListResponseDTO>>builder()
                    .message("내가 좋아요한 게시글 조회 성공")
                    .data(pageResult)
                    .build();

            return ResponseEntity.ok(body);

        } catch (Exception e) {
            log.error("내가 좋아요한 게시글 조회 실패 userId:{}", userId, e);

            ApiResponse<?> error = ApiResponse.builder()
                    .message("내가 좋아요한 게시글 조회 실패")
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }


    // 내가 작성한 댓글 목록
    @GetMapping("/comments")
    public ResponseEntity<?> myComments(@AuthenticationPrincipal String userId,
                                        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<CommentResponseDTO> pageResult = commentService.getCommentByUserId(userId, pageable);

            ApiResponse<Page<CommentResponseDTO>> body = ApiResponse.<Page<CommentResponseDTO>>builder()
                    .message("내가 작성한 댓글 조회 성공")
                    .data(pageResult)
                    .build();

            return ResponseEntity.ok(body);

        } catch (Exception e) {
            log.error("내가 작성한 댓글 조회 실패 userId:{}", userId, e);

            ApiResponse<?> error = ApiResponse.builder()
                    .message("내가 작성한 댓글 조회 실패")
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }


    // 내가 좋아요한 댓글 목록
    @GetMapping("/likes/comments")
    public ResponseEntity<?> likedComments(@AuthenticationPrincipal String userId,
                                           @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<CommentResponseDTO> pageResult = commentService.getLikeCommentByUserId(userId, pageable);

            ApiResponse<Page<CommentResponseDTO>> body = ApiResponse.<Page<CommentResponseDTO>>builder()
                    .message("내가 좋아요한 댓글 조회 성공")
                    .data(pageResult)
                    .build();

            return ResponseEntity.ok(body);

        } catch (Exception e) {
            log.error("내가 좋아요한 댓글 조회 실패 userId:{}", userId, e);

            ApiResponse<?> error = ApiResponse.builder()
                    .message("내가 좋아요한 댓글 조회 실패")
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }


    // 내가 참여한 채팅방 목록
    @GetMapping("/chat/rooms")
    public ResponseEntity<?> myRooms(@AuthenticationPrincipal String userId,
                                     @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<ChatRoomListItemDTO> pageResult = chatRoomMemberService.listMyRooms(userId, pageable);

            ApiResponse<Page<ChatRoomListItemDTO>> body = ApiResponse.<Page<ChatRoomListItemDTO>>builder()
                    .message("내가 참여한 채팅방 목록 조회 성공")
                    .data(pageResult)
                    .build();

            return ResponseEntity.ok(body);

        } catch (Exception e) {
            log.error("내가 참여한 채팅방 목록 조회 실패 userId:{}", userId, e);

            ApiResponse<?> error = ApiResponse.builder()
                    .message("내가 참여한 채팅방 목록 조회 실패")
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }


    // 내가 작성한 리뷰 목록
//    @GetMapping("/reviews")
//    public ResponseEntity<?> myReviews(@AuthenticationPrincipal String userId,
//                                       @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
//        try {
//            Page<ReviewDTO> pageResult = reviewService.getReviewById(userId, pageable);
//
//            ApiResponse<Page<ReviewDTO>> body = ApiResponse.<Page<ReviewDTO>>builder()
//                    .message("내가 작성한 리뷰 목록 조회 성공")
//                    .data(pageResult)
//                    .build();
//
//            return ResponseEntity.ok(body);
//
//        } catch (Exception e) {
//            log.error("내가 작성한 리뷰 목록 조회 실패 userId:{}", userId, e);
//
//            ApiResponse<?> error = ApiResponse.builder()
//                    .message("내가 작성한 리뷰 목록 조회 실패")
//                    .build();
//
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
//        }
//    }

}
