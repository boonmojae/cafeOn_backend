package com.b1a4.cafeOn.services;

import com.b1a4.cafeOn.dto.post.PostRequestDTO;
import com.b1a4.cafeOn.entity.PostEntity;
import com.b1a4.cafeOn.entity.UserEntity;
import com.b1a4.cafeOn.enums.UserStatus;
import com.b1a4.cafeOn.repositories.PostRepository;
import com.b1a4.cafeOn.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
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

//    @Transactional(readOnly = true)
//    public PostEntity getPost(Long postId) {
//        PostEntity post = findByPostId(postId);
//
//        return post;
//    }


    // 내가 작성한 게시글 목록
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
    public PostEntity createPost(String userId, PostRequestDTO requestDto, MultipartFile imageFile) throws IOException {

        UserEntity author = userStatus(userId);

        String imageUrl = null;

        // 이미지 파일이 존재할 경우 서버에 저장하는 로직
        if (imageFile != null && !imageFile.isEmpty()) {
            
            // 원본 파일 이름에서 확장자 추출
            String originalFileName = imageFile.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            // UUID를 이용해 고유한 파일명 생성
            String storedFileName = UUID.randomUUID().toString() + extension;

            // 저장할 전체 경로 설정 (예: C:/cafeon/uploads/posts/UUID.jpg)
            Path filePath = Paths.get(uploadDir, storedFileName);

            // 해당 경로에 디렉토리가 없으면 생성
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 파일을 서버에 실제로 저장
            imageFile.transferTo(filePath.toFile());

            // DB에 저장할 웹 접근 경로 설정
            imageUrl = "/images/" + storedFileName;
        }

        PostEntity post = PostEntity.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .type(requestDto.getType())
                .imageUrl(imageUrl)
                .user(author).
                build();

        return postRepository.save(post);
    }


    // 게시글 수정
//    public PostEntity updatePost(String userId, Long postId, PostRequestDTO postRequestDTO) {
//        userStatus(userId);
//
//        PostEntity post = postRepository.findByPostIdAndUser_UserId(postId, userId)
//                .orElseThrow(() -> new RuntimeException("수정 권한이 없거나 게시글이 존재하지 않습니다."));
//
//        post.update(postRequestDTO.getTitle(), postRequestDTO.getContent(), postRequestDTO.getType(), postRequestDTO.getImageUrl());
//
//        return postRepository.save(post);
//
//    }


    // 게시글 삭제
    public void deletePost(String userId, Long postId) {
        userStatus(userId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ADMIN"));

        PostEntity post = findPostById(postId);

        if (isAdmin || post.getUser().getUserId().equals(userId)) {
            postRepository.delete(post);
        } else {
            throw new AccessDeniedException("이 게시글을 삭제할 권한이 없습니다.");
        }

    }


    // 게시글 검증
//    public PostEntity findByPostId(Long postId) {
//        PostEntity post = postRepository.findById(postId)
//                .orElseThrow(() -> new RuntimeException("해당 게시글을 찾을 수 없습니다. ID: " + postId));
//        return post;
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
