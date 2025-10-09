package com.b1a4.cafeOn.community.comment.service;

import com.b1a4.cafeOn.community.comment.dto.CommentLikeResponseDTO;
import com.b1a4.cafeOn.community.comment.entity.CommentEntity;
import com.b1a4.cafeOn.community.comment.entity.CommentLikeEntity;
import com.b1a4.cafeOn.community.comment.exception.CommentNotFoundException;
import com.b1a4.cafeOn.community.comment.repository.CommentLikeRepository;
import com.b1a4.cafeOn.community.comment.repository.CommentRepository;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentLikeResponseDTO toggleLike(Long commentId, String userId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재 하지 않는 유저입니다."));

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        Optional<CommentLikeEntity> existingLike = commentLikeRepository.findByCommentAndUser(comment, user);

        boolean isLiked;

        if (existingLike.isPresent()) {
            commentLikeRepository.delete(existingLike.get());
            isLiked = false;
        } else {
            commentLikeRepository.save(CommentLikeEntity.of(comment, user));
            isLiked = true;
        }

        long totalLikes = commentLikeRepository.countByComment(comment);

        return new CommentLikeResponseDTO(comment.getCommentId(), isLiked, totalLikes);

    }

}
