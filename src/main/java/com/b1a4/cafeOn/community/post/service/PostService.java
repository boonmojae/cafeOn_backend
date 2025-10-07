package com.b1a4.cafeOn.community.post.service;

import com.b1a4.cafeOn.community.post.dto.PostDetailResponseDTO;
import com.b1a4.cafeOn.community.post.dto.PostListResponseDTO;
import com.b1a4.cafeOn.community.post.dto.PostRequestDTO;
import com.b1a4.cafeOn.community.post.exception.PostForbiddenException;
import com.b1a4.cafeOn.community.post.exception.PostNotFoundException;
import com.b1a4.cafeOn.community.post.repository.LikeCount;
import com.b1a4.cafeOn.community.post.repository.PostLikeRepository;
import com.b1a4.cafeOn.image.entity.ImageEntity;
import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.enums.UserStatus;
import com.b1a4.cafeOn.community.post.repository.PostRepository;
import com.b1a4.cafeOn.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 전체 게시글 조회
    public Page<PostListResponseDTO> getAllPosts(Pageable pageable, String userId) {
        Page<PostEntity> postsPage = postRepository.findAll(pageable);
        List<PostEntity> postsContent = postsPage.getContent();

        if (postsContent.isEmpty()) {
            return Page.empty();
        }

        List<Long> postIds = postsContent.stream()
                .map(PostEntity::getPostId)
                .toList();

        // 좋아요 수
        Map<Long, Long> likeCountMap = postLikeRepository.findLikeCountByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(LikeCount::getPostId, LikeCount::getCnt));

        // 댓글 수
        Map<Long, Long> commentCountMap = postRepository.findCommentCountByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(PostRepository.PostCommentCount::getPostId, PostRepository.PostCommentCount::getCnt));

        // 내가 좋아요한 글
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

    // 특정 게시글 상세 조회
    @Transactional(readOnly = true)
    public PostDetailResponseDTO findPostById(Long postId, String userId) {

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // 좋아요 카운트
        long likeCount = postLikeRepository.countByPost(post);
        
        boolean likedByMe = (userId == null || userId.isBlank())
                && postLikeRepository.existsByPost_PostIdAndUser_UserId(postId, userId);

        return PostDetailResponseDTO.from(post, likeCount, likedByMe);
    }


    // 내가 작성한 게시글 목록
    // fixme: mypage 브랜치
    public Page<PostListResponseDTO> getPostsById(String userId, Pageable pageable) {
        userStatus(userId);
        Page<PostEntity> postsPage = postRepository.findAllByUser_UserId(userId, pageable);
        List<PostEntity> postsContent = postsPage.getContent();

        if (postsContent.isEmpty()) {
            return Page.empty();
        }

        // 게시글 목록 id
        List<Long> postIds = postsContent.stream().map(PostEntity::getPostId).toList();

        // 게시글 좋아요 카운트
        Map<Long, Long> likeCountMap = postLikeRepository.findLikeCountByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(LikeCount::getPostId, LikeCount::getCnt));
        
        // 게시글 댓글 카운트
        Map<Long, Long> commentCountMap = postRepository.findCommentCountByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(PostRepository.PostCommentCount::getPostId, PostRepository.PostCommentCount::getCnt));

        Set<Long> likedIds = new HashSet<>(postLikeRepository.findLikedPostIds(userId, postIds));

        return postsPage.map(p -> PostListResponseDTO.from(
                p,
                likeCountMap.getOrDefault(p.getPostId(), 0L),
                commentCountMap.getOrDefault(p.getPostId(), 0L),
                likedIds.contains(p.getPostId())
        ));

    }

    // 내가 좋아요한 게시글 목록
    // fixme: mypage 브랜치
    @Transactional(readOnly = true)
    public Page<PostListResponseDTO> getLikePostById(String userId, Pageable pageable) {
        userStatus(userId);

        // 내가 좋아요한 게시글 페이징
        Page<Long> idPage = postLikeRepository.findLikedPostIdsByUserId(userId, pageable);
        List<Long> postIds = idPage.getContent();
        if (postIds.isEmpty()) return Page.empty(pageable);

        List<PostEntity> posts = postRepository.findByPostIdIn(postIds);
        Map<Long, PostEntity> byId = posts.stream().collect(Collectors.toMap(PostEntity::getPostId, p -> p));
        List<PostEntity> ordered = postIds.stream().map(byId::get).filter(Objects::nonNull).toList();

        Map<Long, Long> likeCountMap = postLikeRepository.findLikeCountByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(LikeCount::getPostId, LikeCount::getCnt));

        Map<Long, Long> commentCountMap = postRepository.findCommentCountByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(PostRepository.PostCommentCount::getPostId,
                        PostRepository.PostCommentCount::getCnt));

        // DTO (이 목록은 전부 likedByMe = true)
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


    // 게시글 생성
    @Transactional
    public PostDetailResponseDTO createPost(String userId, PostRequestDTO requestDTO, List<MultipartFile> imageFiles) throws IOException {

        UserEntity author = userStatus(userId);

        PostEntity post = PostEntity.builder()
                .title(requestDTO.getTitle())
                .content(requestDTO.getContent())
                .type(requestDTO.getType())
                .user(author)
                .build();

        if (imageFiles != null && !imageFiles.isEmpty()) {

            for (MultipartFile imageFile : imageFiles) {

                // 원본 파일명 추출
                String originalFileName = imageFile.getOriginalFilename();

                // 확장자 추출
                String extension = "";
                if (originalFileName != null && originalFileName.contains(".")) {
                    extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                }

                // UUID를 이용해 고유한 파일명 생성
                String storedFileName = UUID.randomUUID().toString() + extension;

                // 저장할 전체 경로 설정
                Path filePath = Paths.get(uploadDir, storedFileName);

                // 디렉토리가 없으면 생성
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                // 파일을 서버에 실제로 저장
                imageFile.transferTo(filePath.toFile());

                ImageEntity image = ImageEntity.builder()
                        .originalFileName(originalFileName)
                        .storedFileName(storedFileName)
                        .build();

                post.addImage(image);
            }
        }

        PostEntity savedPost = postRepository.save(post);

        return PostDetailResponseDTO.from(savedPost, 0L, false);
    }


    // 게시글 수정
    @Transactional
    public PostDetailResponseDTO updatePost(String userId, Long postId, PostRequestDTO requestDTO, List<MultipartFile> newImageFiles) throws IOException {
        userStatus(userId);

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if (!post.getUser().getUserId().equals(userId)) {
            throw new PostForbiddenException();
        }

        post.update(requestDTO.getTitle(), requestDTO.getContent(), requestDTO.getType());

        List<Long> imagesToKeepIds = requestDTO.getExistingImageIds();
        if (imagesToKeepIds == null) {
            imagesToKeepIds = Collections.emptyList();
        }

        Iterator<ImageEntity> iterator = post.getImages().iterator();
        while (iterator.hasNext()) {
            ImageEntity image = iterator.next();

            Long imageId = image.getImageId();

            if (imageId == null || !imagesToKeepIds.contains(imageId)) {
                try {
                    Path filePath = Paths.get(uploadDir, image.getStoredFileName());
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    throw new RuntimeException("이미지 파일 삭제에 실패했습니다: " + image.getStoredFileName(), e);
                }

                iterator.remove();
            }
        }

        if (newImageFiles != null && !newImageFiles.isEmpty()) {
            for (MultipartFile imageFile : newImageFiles) {
                String originalFileName = imageFile.getOriginalFilename();
                String extension = "";
                if (originalFileName != null && originalFileName.contains(".")) {
                    extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                }

                String storedFileName = UUID.randomUUID().toString() + extension;
                Path filePath = Paths.get(uploadDir, storedFileName);

                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                imageFile.transferTo(filePath.toFile());

                ImageEntity newImage = ImageEntity.builder()
                        .originalFileName(originalFileName)
                        .storedFileName(storedFileName)
                        .build();

                post.addImage(newImage);
            }
        }

        long currentLikeCount = postLikeRepository.countByPost(post);
        boolean likedByMe = postLikeRepository.existsByPost_PostIdAndUser_UserId(postId, userId);

        return PostDetailResponseDTO.from(post, currentLikeCount, likedByMe);
    }


    // 게시글 삭제
    @Transactional
    public void deletePost(String userId, Long postId) {
        UserEntity user = findByUserId(userId);
        PostEntity post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));

        boolean isOwner = post.getUser() != null && post.getUser().getUserId().equals(userId);
        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        // 파일 경로만 미리 수집 (커밋 후 삭제)
        List<Path> paths = post.getImages().stream()
                .map(img -> Paths.get(uploadDir, img.getStoredFileName()))
                .toList();


        // 게시글 삭제 (이미지 엔티티는 JPA 연관/캐스케이드로 함께 삭제)
        postRepository.delete(post);

        // 커밋 후 실제 파일 삭제 (DB 정합성 확정 뒤 I/O 처리)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (Path p : paths) {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException e) {
                        log.warn("이미지 파일 삭제 실패: {}", p, e);
                    }
                }
            }
        });
    }


    // 사용자 검증
    public UserEntity findByUserId(String userId) {
        UserEntity author = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다. ID: " + userId));
        return author;
    }

    // 탈퇴한 사용자 검증
    public UserEntity userStatus(String userId) {
        UserEntity author = findByUserId(userId);

        if (author.getStatus() == null || author.getStatus() == UserStatus.DELETED) {
            throw new RuntimeException("탈퇴한 사용자는 게시글 접근 권한이 없습니다.");
        }

        return author;
    }
}
