package com.b1a4.cafeOn.community.post.service;

import com.b1a4.cafeOn.community.post.dto.PostDetailResponseDTO;
import com.b1a4.cafeOn.community.post.dto.PostListResponseDTO;
import com.b1a4.cafeOn.community.post.dto.PostRequestDTO;
import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.community.post.enums.PostType;
import com.b1a4.cafeOn.community.post.exception.PostForbiddenException;
import com.b1a4.cafeOn.community.post.exception.PostNotFoundException;
import com.b1a4.cafeOn.community.post.repository.LikeCount;
import com.b1a4.cafeOn.community.post.repository.PostLikeRepository;
import com.b1a4.cafeOn.community.post.repository.PostRepository;
import com.b1a4.cafeOn.image.entity.ImageEntity;
import com.b1a4.cafeOn.image.service.ImageService;
import com.b1a4.cafeOn.image.service.S3Service;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.enums.UserStatus;
import com.b1a4.cafeOn.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final ImageService imageService;

    // 전체 게시글 목록 (검색/필터 포함)
    @Transactional(readOnly = true)
    public Page<PostListResponseDTO> getAllPosts(Pageable pageable, String userId, PostType type, String keyword
    ) {
        Page<PostEntity> postsPage;
        boolean noFilter = (type == null) && (keyword == null || keyword.isBlank());

        if (noFilter) {
            postsPage = postRepository.findAll(pageable);
        } else {
            postsPage = postRepository.search(type, keyword, pageable);
        }

        List<PostEntity> postsContent = postsPage.getContent();
        if (postsContent.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> postIds = postsContent.stream()
                .map(PostEntity::getPostId)
                .toList();

        // 좋아요 수
        Map<Long, Long> likeCountMap = postLikeRepository.findLikeCountByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(LikeCount::getPostId, LikeCount::getCnt));

        // 댓글 수
        Map<Long, Long> commentCountMap = postRepository.findCommentCountByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(
                        PostRepository.PostCommentCount::getPostId,
                        PostRepository.PostCommentCount::getCnt
                ));

        // 내가 좋아요한 글들
        Set<Long> likedIds = (userId == null || userId.isBlank())
                ? Set.of()
                : new HashSet<>(postLikeRepository.findLikedPostIds(userId, postIds));

        return postsPage.map(p -> PostListResponseDTO.from(
                p,
                likeCountMap.getOrDefault(p.getPostId(), 0L),
                commentCountMap.getOrDefault(p.getPostId(), 0L),
                likedIds.contains(p.getPostId())
        ));
    }

    // 게시글 상세 조회
    @Transactional(readOnly = true)
    public PostDetailResponseDTO findPostById(Long postId, String userId) {

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        long likeCount = postLikeRepository.countByPost(post);

        boolean likedByMe = (userId != null && !userId.isBlank())
                && postLikeRepository.existsByPost_PostIdAndUser_UserId(postId, userId);

        return PostDetailResponseDTO.from(post, likeCount, likedByMe);
    }

    // 내가 쓴 게시글 목록
    @Transactional(readOnly = true)
    public Page<PostListResponseDTO> getPostsById(String userId, Pageable pageable) {
        userStatus(userId);

        Page<PostEntity> postsPage = postRepository.findAllByUser_UserId(userId, pageable);
        List<PostEntity> postsContent = postsPage.getContent();
        if (postsContent.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> postIds = postsContent.stream()
                .map(PostEntity::getPostId)
                .toList();

        Map<Long, Long> likeCountMap = postLikeRepository.findLikeCountByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(LikeCount::getPostId, LikeCount::getCnt));

        Map<Long, Long> commentCountMap = postRepository.findCommentCountByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(
                        PostRepository.PostCommentCount::getPostId,
                        PostRepository.PostCommentCount::getCnt
                ));

        Set<Long> likedIds = new HashSet<>(postLikeRepository.findLikedPostIds(userId, postIds));

        return postsPage.map(p -> PostListResponseDTO.from(
                p,
                likeCountMap.getOrDefault(p.getPostId(), 0L),
                commentCountMap.getOrDefault(p.getPostId(), 0L),
                likedIds.contains(p.getPostId())
        ));
    }

    // 내가 좋아요한 게시글 목록
    @Transactional(readOnly = true)
    public Page<PostListResponseDTO> getLikePostById(String userId, Pageable pageable) {
        userStatus(userId);

        Page<Long> idPage = postLikeRepository.findLikedPostIdsByUserId(userId, pageable);
        List<Long> postIds = idPage.getContent();
        if (postIds.isEmpty()) {
            return Page.empty(pageable);
        }

        List<PostEntity> posts = postRepository.findByPostIdIn(postIds);
        Map<Long, PostEntity> byId = posts.stream()
                .collect(Collectors.toMap(PostEntity::getPostId, p -> p));

        List<PostEntity> ordered = postIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, Long> likeCountMap = postLikeRepository.findLikeCountByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(LikeCount::getPostId, LikeCount::getCnt));

        Map<Long, Long> commentCountMap = postRepository.findCommentCountByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(
                        PostRepository.PostCommentCount::getPostId,
                        PostRepository.PostCommentCount::getCnt
                ));

        List<PostListResponseDTO> content = ordered.stream()
                .map(p -> PostListResponseDTO.from(
                        p,
                        likeCountMap.getOrDefault(p.getPostId(), 0L),
                        commentCountMap.getOrDefault(p.getPostId(), 0L),
                        true
                ))
                .toList();

        return new PageImpl<>(content, pageable, idPage.getTotalElements());
    }

    // 게시글 생성(글, 글+이미지)
    @Transactional
    public PostDetailResponseDTO createPost(String userId, PostRequestDTO requestDTO, List<S3Service.UploadedImageInfo> uploadedImages) {
        UserEntity author = userStatus(userId);

        if (requestDTO == null) {
            throw new IllegalArgumentException("게시글 정보(post)가 필요합니다.");
        }
        if (requestDTO.getTitle() == null || requestDTO.getTitle().isBlank()) {
            throw new IllegalArgumentException("게시글 제목은 필수입니다.");
        }

        PostEntity post = PostEntity.builder()
                .title(requestDTO.getTitle())
                .content(requestDTO.getContent())
                .type(requestDTO.getType())
                .user(author)
                .build();

        PostEntity savedPost = postRepository.save(post);

        if (uploadedImages != null && !uploadedImages.isEmpty()) {
            for (S3Service.UploadedImageInfo imgInfo : uploadedImages) {
                ImageEntity imageEntity = imageService.attachNewImageToPost(savedPost, imgInfo);
                savedPost.addImage(imageEntity);
            }
        }

        long likeCount = 0L;
        boolean likedByMe = false;
        return PostDetailResponseDTO.from(savedPost, likeCount, likedByMe);
    }

    // 게시글 수정
    @Transactional
    public PostDetailResponseDTO updatePost(String userId, Long postId, PostRequestDTO requestDTO,
                                            List<S3Service.UploadedImageInfo> newlyUploadedImages) {
        userStatus(userId);

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if (!isOwnerOrAdmin(userId, post)) {
            throw new PostForbiddenException();
        }

        if (requestDTO == null) {
            throw new IllegalArgumentException("수정할 게시글 정보가 없습니다.");
        }

        post.update(
                requestDTO.getTitle(),
                requestDTO.getContent(),
                requestDTO.getType()
        );

        // 유지 이미지 id 목록
        List<Long> imagesToKeepIds = requestDTO.getExistingImageIds();
        if (imagesToKeepIds == null) {
            imagesToKeepIds = Collections.emptyList();
        }

        // 이미지 갱신
        imageService.updatePostImages(
                post,
                imagesToKeepIds,
                newlyUploadedImages != null ? newlyUploadedImages : Collections.emptyList()
        );

        long currentLikeCount = postLikeRepository.countByPost(post);
        boolean likedByMe = postLikeRepository.existsByPost_PostIdAndUser_UserId(postId, userId);

        return PostDetailResponseDTO.from(post, currentLikeCount, likedByMe);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(String userId, Long postId) {
        UserEntity caller = findByUserId(userId);

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if (!isOwnerOrAdmin(caller.getUserId(), post)) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        imageService.removeAllImagesOfPost(post);

        postRepository.delete(post);
    }


    // 내부 유틸
    private boolean isOwnerOrAdmin(String userId, PostEntity post) {
        boolean isOwner = post.getUser() != null
                && post.getUser().getUserId().equals(userId);

        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return isOwner || isAdmin;
    }

    public UserEntity findByUserId(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다. ID: " + userId));
    }

    public UserEntity userStatus(String userId) {
        UserEntity author = findByUserId(userId);

        if (author.getStatus() == null || author.getStatus() == UserStatus.DELETED) {
            throw new RuntimeException("탈퇴한 사용자는 게시글 접근 권한이 없습니다.");
        }

        return author;
    }
}
