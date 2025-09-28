package com.b1a4.cafeOn.controllers;

import com.b1a4.cafeOn.dto.ApiResponse;
import com.b1a4.cafeOn.dto.post.PostDetailResponseDTO;
import com.b1a4.cafeOn.dto.post.PostListResponseDTO;
import com.b1a4.cafeOn.dto.post.PostRequestDTO;
import com.b1a4.cafeOn.entity.PostEntity;
import com.b1a4.cafeOn.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 전체 게시글 조회
    @GetMapping
    public ResponseEntity<?> getAllPosts(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<PostEntity> posts = postService.getAllPosts(pageable);

            Page<PostListResponseDTO> responseDTOS = posts.map(PostListResponseDTO::from);

            ApiResponse<Page<PostListResponseDTO>> response = ApiResponse.<Page<PostListResponseDTO>>builder()
                    .data(responseDTOS)
                    .message("전체 게시글이 성공적으로 조회되었습니다.")
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message("게시글을 조회할 수 없습니다.")
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


    // 특정 게시글 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> getPost(@PathVariable("id") Long postId) {

        try {
            PostEntity post = postService.getPost(postId);

            PostDetailResponseDTO responseDTO = PostDetailResponseDTO.from(post);

            ApiResponse<PostDetailResponseDTO> response = ApiResponse.<PostDetailResponseDTO>builder()
                    .data(responseDTO)
                    .message("게시글을 성공적으로 조회했습니다")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message("게시글을 조회할 수 없습니다")
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }

    }

    // 내가 작성한 게시글
    // fixme: mypage 브랜치로 이동
    @GetMapping("/my")
    public ResponseEntity<?> myPosts(@AuthenticationPrincipal String userId, @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        try {

            Page<PostEntity> posts = postService.getPostsById(userId, pageable);

            Page<PostListResponseDTO> responseDTOS = posts.map(PostListResponseDTO::from);

            ApiResponse<Page<PostListResponseDTO>> response = ApiResponse.<Page<PostListResponseDTO>>builder()
                    .data(responseDTOS)
                    .message("내가 작성한 게시글 조회 성공")
                    .build();

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message("게시글을 찾을 수 없습니다.")
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 특정 게시글 단어 검색
    @GetMapping("/search")
    public ResponseEntity<?> searchPosts(@RequestParam String keyword, @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        try {

            Page<PostEntity> posts = postService.searchPosts(keyword, pageable);

            if (posts.isEmpty()) {
                ApiResponse<?> response = ApiResponse.builder()
                        .message("'" + keyword + "'에 대한 검색 결과가 없습니다.")
                        .build();
                return ResponseEntity.ok().body(response);
            }

            Page<PostDetailResponseDTO> responseDTOS = posts.map(PostDetailResponseDTO::from);

            ApiResponse<Page<PostDetailResponseDTO>> response = ApiResponse.<Page<PostDetailResponseDTO>>builder()
                    .data(responseDTOS)
                    .message("'" + keyword + "'에 대한 검색 결과입니다.")
                    .build();

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message("검색 중 오류가 발생했습니다.")
                    .build();

            return ResponseEntity.internalServerError().body(errorResponse);
        }

    }

    // 게시글 생성
    @PostMapping
    public ResponseEntity<?> createPost(@AuthenticationPrincipal String userId, @RequestBody PostRequestDTO postRequestDTO) {

        try {
            PostEntity savePost = postService.createPost(userId, postRequestDTO);

            PostDetailResponseDTO responseDTO = PostDetailResponseDTO.from(savePost);

            ApiResponse<PostDetailResponseDTO> response = ApiResponse.<PostDetailResponseDTO>builder()
                    .data(responseDTO)
                    .message("게시글이 생성 되었습니다.")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message("사용자를 찾을 수 없습니다.")
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@AuthenticationPrincipal String userId, @PathVariable("id") Long postId, @RequestBody PostRequestDTO postRequestDTO) {

        try {

            PostEntity updatePost = postService.updatePost(userId, postId, postRequestDTO);

            PostDetailResponseDTO responseDTO = PostDetailResponseDTO.from(updatePost);

            ApiResponse<PostDetailResponseDTO> response = ApiResponse.<PostDetailResponseDTO>builder()
                    .data(responseDTO)
                    .message("게시글이 수정되었습니다.")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message("게시글을 수정할 수 없습니다.")
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@AuthenticationPrincipal String userId, @PathVariable("id") Long postId) {

        try {

            postService.deletePost(userId, postId);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message("게시글을 삭제할 수 없습니다.")
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


}
