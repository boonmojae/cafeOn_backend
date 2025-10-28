package com.b1a4.cafeOn.community.post.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.community.post.dto.PostDetailResponseDTO;
import com.b1a4.cafeOn.community.post.dto.PostListResponseDTO;
import com.b1a4.cafeOn.community.post.dto.PostRequestDTO;
import com.b1a4.cafeOn.community.post.enums.PostType;
import com.b1a4.cafeOn.community.post.service.PostService;
import com.b1a4.cafeOn.community.post.service.ViewCountService;
import com.b1a4.cafeOn.image.enums.ImageCategory;
import com.b1a4.cafeOn.image.service.S3Service;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;
    private final ViewCountService viewCountService;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 전체 게시글 조회
    @GetMapping
    public ResponseEntity<?> getAllPosts(@AuthenticationPrincipal String userId, @RequestParam(required = false) PostType type,
                                         @RequestParam(required = false) String keyword,
                                         @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<PostListResponseDTO> responseDTOS =
                    postService.getAllPosts(pageable, userId, type, keyword);

            return ResponseEntity.ok(
                    ApiResponse.<Page<PostListResponseDTO>>builder()
                            .data(responseDTOS)
                            .message("전체 게시글이 성공적으로 조회되었습니다.")
                            .build()
            );
        } catch (Exception e) {
            log.error("전체 게시글 조회 중 오류 발생", e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .message("게시글을 조회할 수 없습니다.")
                            .build());
        }
    }

    // 특정 게시글 조회 (조회수 증가 포함)
    @GetMapping("/{id}")
    public ResponseEntity<?> getPost(@AuthenticationPrincipal String userId, @PathVariable("id") Long postId,
                                     HttpServletRequest request, HttpServletResponse response) {

        try {
            handleViewCount(postId, request, response);
        } catch (Exception e) {
            log.warn("Failed to update view count. postId={}", postId, e);
        }

        PostDetailResponseDTO responseDTO = postService.findPostById(postId, userId);

        return ResponseEntity.ok(
                ApiResponse.<PostDetailResponseDTO>builder()
                        .data(responseDTO)
                        .message("게시글을 성공적으로 조회했습니다.")
                        .build()
        );
    }

    // 조회수 쿠키 처리
    private void handleViewCount(Long postId, HttpServletRequest request, HttpServletResponse response) {
        Cookie oldCookie = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("postView".equals(cookie.getName())) {
                    oldCookie = cookie;
                    break;
                }
            }
        }

        if (oldCookie != null) {
            if (!oldCookie.getValue().contains("[" + postId.toString() + "]")) {
                viewCountService.increaseViewCount(postId);
                oldCookie.setValue(oldCookie.getValue() + "_[" + postId.toString() + "]");
                oldCookie.setPath("/");
                oldCookie.setMaxAge(60 * 60 * 24); // 24h
                response.addCookie(oldCookie);
            }
        } else {
            viewCountService.increaseViewCount(postId);
            Cookie newCookie = new Cookie("postView", "[" + postId.toString() + "]");
            newCookie.setPath("/");
            newCookie.setMaxAge(60 * 60 * 24); // 24h
            response.addCookie(newCookie);
        }
    }


    // 게시글 생성 (글, 글+이미지)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createPost(@AuthenticationPrincipal String userId,
                                        @RequestPart(value = "post", required = true) String postJson,
                                        @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        PostRequestDTO postRequestDTO;
        try {
            postRequestDTO = objectMapper.readValue(postJson, PostRequestDTO.class);
        } catch (Exception e) {
            log.warn("post 파트 파싱 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .message("post 파트(JSON) 파싱 실패")
                            .build()
            );
        }

        List<S3Service.UploadedImageInfo> uploadedInfos = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                S3Service.UploadedImageInfo info =
                        s3Service.uploadImage(file, ImageCategory.POST);
                uploadedInfos.add(info);
            }
        }

        PostDetailResponseDTO saved = postService.createPost(
                userId,
                postRequestDTO,
                uploadedInfos
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.builder()
                        .data(saved)
                        .message("게시글이 생성되었습니다.")
                        .build());
    }

    // 게시글 수정
    @PutMapping(value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updatePost(@AuthenticationPrincipal String userId, @PathVariable("id") Long postId,
                                        @RequestPart(value = "post", required = true) String postJson,
                                        @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        PostRequestDTO postRequestDTO;
        try {
            postRequestDTO = objectMapper.readValue(postJson, PostRequestDTO.class);
        } catch (Exception e) {
            log.warn("post 파트(JSON) 파싱 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .message("post 파트(JSON) 파싱 실패")
                            .build()
            );
        }

        List<S3Service.UploadedImageInfo> newlyUploadedInfos = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                S3Service.UploadedImageInfo info =
                        s3Service.uploadImage(file, ImageCategory.POST);
                newlyUploadedInfos.add(info);
            }
        }

        try {
            PostDetailResponseDTO updated = postService.updatePost(
                    userId,
                    postId,
                    postRequestDTO,
                    newlyUploadedInfos
            );

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .message("게시글이 수정되었습니다.")
                            .data(updated)
                            .build()
            );

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.builder()
                            .message("이 게시글을 수정할 권한이 없습니다.")
                            .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }


    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@AuthenticationPrincipal String userId, @PathVariable(name = "id") Long postId) {
        postService.deletePost(userId, postId);
        return ResponseEntity.noContent().build();
    }
}