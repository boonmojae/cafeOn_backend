package com.b1a4.cafeOn.services;

import com.b1a4.cafeOn.entity.PostEntity;
import com.b1a4.cafeOn.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    // 전체 게시글 조회
    public List<PostEntity> getAllPosts() {
        return postRepository.findAll();
    }

    // 특정 게시글 조회
    public PostEntity findByPostId(Long postId) {
        return postRepository.findById(postId)
                // fixme: global 에러 추가 후 수정하기
                .orElseThrow(() -> new RuntimeException("존재하지 않는 게시글 입니다."));
    }

    // 내가 작성한 게시글 목록
    public List<PostEntity> findByPostIdAndUserId(String userId) {
        return postRepository.findAllByUserId(userId);
    }

    // 특정 게시글 단어 검색

    // 게시글 생성
    public PostEntity createPost(PostEntity postEntity) {
        return postRepository.save(postEntity);
    }

    // 게시글 수정
    // fixme: 커스텀 예외 생성 -> 게시글 여부, 소유권 확인
    public PostEntity updatePost(Long postId, String userId, PostEntity postEntity) {

        PostEntity postToUpdate = postRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new RuntimeException("수정 권한이 없거나 존재하지 않는 게시글 입니다."));

        postToUpdate.update(postEntity.getTitle(), postEntity.getContent(), postEntity.getType());

        return postToUpdate;
    }

    // 게시글 삭제
    public void deletePost(Long postId, String userId) {

        PostEntity existsPostIdAndUserId = postRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new RuntimeException("수정 권한이 없거나 존재하지 않는 게시글 입니다."));

        postRepository.delete(existsPostIdAndUserId);
    }

}
