package com.b1a4.cafeOn.community.post.service;

import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.community.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ViewCountService {

    private final PostRepository postRepository;

    @Transactional
    public void increaseViewCount(Long postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        post.increaseViewCount();
    }
}
