package com.b1a4.cafeOn.community.comment.service;

import com.b1a4.cafeOn.community.comment.dto.CommentRequestDTO;
import com.b1a4.cafeOn.community.comment.dto.CommentResponseDTO;
import com.b1a4.cafeOn.community.comment.entity.CommentEntity;
import com.b1a4.cafeOn.community.comment.exception.CommentNotFoundException;
import com.b1a4.cafeOn.community.comment.repository.CommentRepository;
import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.community.post.exception.PostNotFoundException;
import com.b1a4.cafeOn.community.post.repository.PostRepository;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 댓글 생성
    public CommentResponseDTO createComment(String userId, Long postId, Long parentId, CommentRequestDTO commentRequestDTO) {

        // 유저 검증
        UserEntity author = findByUserId(userId);

        // 게시글 검증
        PostEntity post = findByPostId(postId);

        // body의 parentId를 우선적으로 사용하고 없으면 path의 parentId 사용
        Long existsParentId = (commentRequestDTO.getParentId() != null) ? commentRequestDTO.getParentId() : parentId;

        if (parentId != null) {

            CommentEntity parent = findByCommentId(existsParentId);

            if (!parent.getPost().getPostId().equals(postId)) {
                throw new IllegalArgumentException("부모 댓글이 해당 게시글에 속하지 않습니다.");
            }
        }

        CommentEntity saveComment = CommentEntity.builder()
                .parentId(existsParentId)
                .content(commentRequestDTO.getContent())
                .post(post)
                .user(author)
                .build();

        CommentEntity saved = commentRepository.save(saveComment);

        return CommentResponseDTO.from(saved);

    }

    // 댓글 목록 조회
    public Page<CommentResponseDTO> getAllComments(Pageable pageable) {
        Page<CommentEntity> comments = commentRepository.findAll(pageable);

        return comments.map(CommentResponseDTO::from);
    }

    // 특정 댓글 상세 조회

    // 내가 작성한 댓글 목록

    // 댓글 수정

    // 댓글 삭제




    // 유저 검증
    public UserEntity findByUserId(String userId) {
        UserEntity author = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));
        return author;
    }

    // 게시글 검증
    public PostEntity findByPostId(Long postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        return post;
    }

    // 댓글 검증
    public CommentEntity findByCommentId(Long commentId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        return comment;
    }
}
