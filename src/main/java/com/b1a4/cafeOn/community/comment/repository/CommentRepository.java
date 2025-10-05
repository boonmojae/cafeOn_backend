package com.b1a4.cafeOn.community.comment.repository;

import com.b1a4.cafeOn.community.comment.dto.CommentResponseDTO;
import com.b1a4.cafeOn.community.comment.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    
    // 유저가 댓글 좋아요를 하고 있는지 -> 이거는 CommentLikeEntity만들어서 해야겠지

    // 특정 게시글의 댓글 카운트
    long countByPost_PostId(Long postId);

    // 특정 유저의 댓글 목록
    Page<CommentEntity> findCommentByUser_UserId(String userId, Pageable pageable);

    // plus: Page<>로 되어있는걸 굳이 안만들어도 되는건가?
    // plus: 지금 메서드에는 필요없고 특정 유저가 쓴 게시글 findAllUser_UserId?이렇게
//    Page<CommentEntity> findAllComment(Pageable pageable);

}
