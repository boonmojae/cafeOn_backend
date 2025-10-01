package com.b1a4.cafeOn.services;

import com.b1a4.cafeOn.dto.post.PostRequestDTO;
import com.b1a4.cafeOn.entity.ImageEntity;
import com.b1a4.cafeOn.entity.PostEntity;
import com.b1a4.cafeOn.entity.UserEntity;
import com.b1a4.cafeOn.enums.UserStatus;
import com.b1a4.cafeOn.repositories.PostRepository;
import com.b1a4.cafeOn.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 전체 게시글 조회
    public Page<PostEntity> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    // 특정 게시글 조회
    @Transactional(readOnly = true)
    public PostEntity findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("해당 게시글을 찾을 수 없습니다. ID: " + postId));
    }


    // 내가 작성한 게시글 목록
    // fixme: feat/mypage 브랜치로 이동
    public Page<PostEntity> getPostsById(String userId, Pageable pageable) {
        userStatus(userId);
        Page<PostEntity> posts = postRepository.findAllByUser_UserId(userId, pageable);
        return posts;
    }


    // 특정 게시글 단어 검색
    public Page<PostEntity> searchPosts(String keyword, Pageable pageable) {
        return postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword, pageable);
    }


    // 게시글 생성
    @Transactional
    public PostEntity createPost(String userId, PostRequestDTO requestDTO, List<MultipartFile> imageFiles) throws IOException {

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

        return postRepository.save(post);
    }


    // 게시글 수정
    @Transactional
    public PostEntity updatePost(String userId, Long postId, PostRequestDTO requestDTO, List<MultipartFile> newImageFiles) throws IOException {
        userStatus(userId);

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시글을 찾을 수 없습니다. ID: " + postId));

        String ownerId = post.getUser().getUserId();
        log.info("[UPDATE] ownerId={}, callerId={}", ownerId, userId);

        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !equalsSafe(ownerId, userId)) {
            throw new AccessDeniedException("이 게시글을 수정할 권한이 없습니다.");
        }

        post.update(requestDTO.getTitle(), requestDTO.getContent(), requestDTO.getType());


        List<Long> imagesToKeepIds = requestDTO.getExistingImageIds();
        if (imagesToKeepIds != null) {
            if (imagesToKeepIds.isEmpty()) {
                imagesToKeepIds = java.util.Collections.emptyList();
            }

            java.util.Iterator<ImageEntity> iterator = post.getImages().iterator();
            while (iterator.hasNext()) {
                ImageEntity image = iterator.next();
                Long imageId = image.getImageId();

                if (imageId == null || !imagesToKeepIds.contains(imageId)) {
                    try {
                        java.nio.file.Path filePath = java.nio.file.Paths.get(uploadDir, image.getStoredFileName());
                        java.nio.file.Files.deleteIfExists(filePath);
                    } catch (IOException e) {
                        throw new RuntimeException("이미지 파일 삭제에 실패했습니다: " + image.getStoredFileName(), e);
                    }
                    iterator.remove();
                }
            }
        } else {
            log.info("[UPDATE] imagesToKeepIds is null -> keep existing images as-is");
        }

        if (newImageFiles != null && !newImageFiles.isEmpty()) {
            for (MultipartFile imageFile : newImageFiles) {
                String originalFileName = imageFile.getOriginalFilename();

                String extension = "";
                if (originalFileName != null && originalFileName.contains(".")) {
                    extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                }

                String storedFileName = java.util.UUID.randomUUID().toString() + extension;
                java.nio.file.Path filePath = java.nio.file.Paths.get(uploadDir, storedFileName);

                java.io.File dir = new java.io.File(uploadDir);
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

        return post;
    }

    private boolean equalsSafe(String a, String b) {
        if (a == null || b == null) return false;
        return a.trim().equalsIgnoreCase(b.trim());
    }

//    @Transactional
//    public PostEntity updatePost(String userId, Long postId, PostRequestDTO requestDTO, List<MultipartFile> newImageFiles) throws IOException {
//        userStatus(userId);
//
//        PostEntity post = postRepository.findById(postId)
//                .orElseThrow(() -> new EntityNotFoundException("해당 게시글을 찾을 수 없습니다. ID: " + postId));
//
//        if (!post.getUser().getUserId().equals(userId)) {
//            throw new AccessDeniedException("이 게시글을 수정할 권한이 없습니다.");
//        }
//
//        post.update(requestDTO.getTitle(), requestDTO.getContent(), requestDTO.getType());
//
//        List<Long> imagesToKeepIds = requestDTO.getExistingImageIds();
//        if (imagesToKeepIds == null) {
//            imagesToKeepIds = Collections.emptyList();
//        }
//
//        Iterator<ImageEntity> iterator = post.getImages().iterator();
//        while (iterator.hasNext()) {
//            ImageEntity image = iterator.next();
//
//            Long imageId = image.getImageId();
//
//            if (imageId == null || !imagesToKeepIds.contains(imageId)) {
//                try {
//                    Path filePath = Paths.get(uploadDir, image.getStoredFileName());
//                    Files.deleteIfExists(filePath);
//                } catch (IOException e) {
//                    throw new RuntimeException("이미지 파일 삭제에 실패했습니다: " + image.getStoredFileName(), e);
//                }
//
//                iterator.remove();
//            }
//        }
//
//        if (newImageFiles != null && !newImageFiles.isEmpty()) {
//            for (MultipartFile imageFile : newImageFiles) {
//                String originalFileName = imageFile.getOriginalFilename();
//                String extension = "";
//                if (originalFileName != null && originalFileName.contains(".")) {
//                    extension = originalFileName.substring(originalFileName.lastIndexOf("."));
//                }
//
//                String storedFileName = UUID.randomUUID().toString() + extension;
//                Path filePath = Paths.get(uploadDir, storedFileName);
//
//                File dir = new File(uploadDir);
//                if (!dir.exists()) {
//                    dir.mkdirs();
//                }
//
//                imageFile.transferTo(filePath.toFile());
//
//                ImageEntity newImage = ImageEntity.builder()
//                        .originalFileName(originalFileName)
//                        .storedFileName(storedFileName)
//                        .build();
//
//                post.addImage(newImage);
//            }
//        }
//
//        return post;
//    }


    // 게시글 삭제
    // todo: 이미지 삭제 로직 추가
//    public void deletePost(String userId, Long postId) {
//        userStatus(userId);
//
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        boolean isAdmin = authentication.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .anyMatch(role -> role.equals("ADMIN"));
//
//        PostEntity post = findPostById(postId);
//
//        if (isAdmin || post.getUser().getUserId().equals(userId)) {
//            postRepository.delete(post);
//        } else {
//            throw new AccessDeniedException("이 게시글을 삭제할 권한이 없습니다.");
//        }
//
//    }


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
