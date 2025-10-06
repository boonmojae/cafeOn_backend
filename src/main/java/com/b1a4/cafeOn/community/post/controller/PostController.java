package com.b1a4.cafeOn.community.post.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.community.post.dto.PostDetailResponseDTO;
import com.b1a4.cafeOn.community.post.dto.PostListResponseDTO;
import com.b1a4.cafeOn.community.post.dto.PostRequestDTO;
import com.b1a4.cafeOn.community.post.service.PostService;
import com.b1a4.cafeOn.community.post.service.ViewCountService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;
    private final ViewCountService viewCountService;

    // 전체 게시글 조회
    @GetMapping
    public ResponseEntity<?> getAllPosts(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<PostListResponseDTO> responseDTOS = postService.getAllPosts(pageable);

            ApiResponse<Page<PostListResponseDTO>> response = ApiResponse.<Page<PostListResponseDTO>>builder()
                    .data(responseDTOS)
                    .message("전체 게시글이 성공적으로 조회되었습니다.")
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("전체 게시글 조회 중 오류 발생", e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message("게시글을 조회할 수 없습니다.")
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    // 특정 게시글 조회
    // 조회수 증가 로직(쿠키 기반) 함께 처리
    @GetMapping("/{id}")
    public ResponseEntity<?> getPost(@PathVariable("id") Long postId, HttpServletRequest request, HttpServletResponse response) {

        try {
            handleViewCount(postId, request, response);

        } catch (Exception e) {
            log.warn("Failed to update view count. postId={}", postId, e);
        }

        PostDetailResponseDTO responseDTO = postService.findPostById(postId);

        ApiResponse<PostDetailResponseDTO> responseF = ApiResponse.<PostDetailResponseDTO>builder()
                .data(responseDTO)
                .message("게시글을 성공적으로 조회했습니다")
                .build();

        return ResponseEntity.ok(responseF);

    }

    // 조회수 중복 방지를 위한 쿠키 로직을 처리함
    private void handleViewCount(Long postId, HttpServletRequest request, HttpServletResponse response) {
        Cookie oldCookie = null;
        Cookie[] cookies = request.getCookies();

        // 기존 쿠키들 확인
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("postView")) {
                    oldCookie = cookie;
                }
            }
        }

        if (oldCookie != null) {
            if (!oldCookie.getValue().contains("[" + postId.toString() + "]")) {
                log.info("쿠키는 있지만 처음 보는 글 -> 조회수를 증가");
                viewCountService.increaseViewCount(postId);
                oldCookie.setValue(oldCookie.getValue() + "_[" + postId.toString() + "]");
                oldCookie.setPath("/");
                oldCookie.setMaxAge(60 * 60 * 24);
                response.addCookie(oldCookie);
            }
        } else {
            viewCountService.increaseViewCount(postId);
            Cookie newCookie = new Cookie("postView", "[" + postId.toString() + "]");
            newCookie.setPath("/");
            newCookie.setMaxAge(60 * 60 * 24);
            response.addCookie(newCookie);
        }
    }


    // 내가 작성한 게시글
    // fixme: mypage 브랜치로 이동
    @GetMapping("/my")
    public ResponseEntity<?> myPosts(@AuthenticationPrincipal String userId, @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        try {

            Page<PostListResponseDTO> responseDTOS  = postService.getPostsById(userId, pageable);

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


    // 게시글 생성
    // JSON + 파일 (멀티파트)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createPostMultipart(
            @AuthenticationPrincipal String userId,
            @RequestPart("postRequestDTO") PostRequestDTO postRequestDTO,
            @RequestPart(value = "image", required = false) List<MultipartFile> imageFiles
    ) throws IOException {
        PostDetailResponseDTO saved = postService.createPost(userId, postRequestDTO, imageFiles);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.builder()
                        .data(saved)
                        .message("게시글이 생성되었습니다.")
                        .build());
    }

    // JSON만
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createPostJsonOnly(
            @AuthenticationPrincipal String userId,
            @RequestBody PostRequestDTO postRequestDTO
    ) throws IOException {
        PostDetailResponseDTO saved = postService.createPost(userId, postRequestDTO, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.builder()
                        .data(saved)
                        .message("게시글이 생성되었습니다.")
                        .build());
    }


    // 게시글 수정
    // 멀티파트
    @PutMapping(path = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updatePostMultipart(
            @AuthenticationPrincipal String userId,
            @PathVariable("id") Long postId,
            @RequestPart("postRequestDTO") PostRequestDTO postRequestDTO,
            @RequestPart(value = "image", required = false) List<MultipartFile> imageFiles) {

        try {
            PostDetailResponseDTO post = postService.updatePost(userId, postId, postRequestDTO, imageFiles);
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .message("게시글이 수정되었습니다.")
                            .data(post)
                            .build()
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.builder().message(e.getMessage()).build());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.builder().message("이 게시글을 수정할 권한이 없습니다.").build());
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder().message("이미지 파일 처리 중 오류가 발생했습니다: " + e.getMessage()).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder().message("요청 처리 중 예상치 못한 오류가 발생했습니다.").build());
        }
    }

    // JSON
    @PutMapping(path = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updatePostJsonOnly(
            @AuthenticationPrincipal String userId,
            @PathVariable("id") Long postId,
            @RequestBody PostRequestDTO postRequestDTO) {

        try {
            // JSON-only → 이미지 변경 없음
            PostDetailResponseDTO post = postService.updatePost(userId, postId, postRequestDTO, null);
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .message("게시글이 수정되었습니다.")
                            .data(post)
                            .build()
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.builder().message(e.getMessage()).build());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.builder().message("이 게시글을 수정할 권한이 없습니다.").build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder().message("요청 처리 중 예상치 못한 오류가 발생했습니다.").build());
        }
    }


    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@AuthenticationPrincipal String userId, @PathVariable(name = "id") Long postId) {

        postService.deletePost(userId, postId);

        return ResponseEntity.noContent().build();
    }


}
