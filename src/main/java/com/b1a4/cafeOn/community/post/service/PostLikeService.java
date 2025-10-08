package com.b1a4.cafeOn.community.post.service;

import com.b1a4.cafeOn.community.post.dto.PostLikeResponseDTO;
import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.community.post.entity.PostLikeEntity;
import com.b1a4.cafeOn.community.post.exception.PostNotFoundException;
import com.b1a4.cafeOn.community.post.repository.PostLikeRepository;
import com.b1a4.cafeOn.community.post.repository.PostRepository;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;


    // 게시글 토글(좋아요/삭제)
    @Transactional
    public PostLikeResponseDTO toggleLike(Long postId, String userId) {

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재 하지 않는 유저입니다"));

        Optional<PostLikeEntity> existingLike = postLikeRepository.findByPostAndUser(post, user);

        boolean isLiked;

        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            isLiked = false;
        } else {
            postLikeRepository.save(PostLikeEntity.of(post, user));
            isLiked = true;
        }

        long totalLikes = postLikeRepository.countByPost(post);

        return new PostLikeResponseDTO(post.getPostId(), isLiked, totalLikes);

    }

}
