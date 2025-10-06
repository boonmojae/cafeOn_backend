package com.b1a4.cafeOn.community.comment.repository;

import com.b1a4.cafeOn.community.comment.dto.CommentResponseDTO;
import com.b1a4.cafeOn.community.comment.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    
    // 특정 게시글의 댓글 카운트
    long countByPost_PostId(Long postId);

    // 특정 유저의 댓글 목록
    Page<CommentEntity> findByUser_UserId(String userId, Pageable pageable);

    // 댓글 + 게시글 일치 검증
    Optional<CommentEntity> findByCommentIdAndPost_PostId(Long commentId, Long postId);

    // 게시글 내 루트 댓글(부모 없음)만 페이징
    @EntityGraph(attributePaths = {"user", "parent"})
    Page<CommentEntity> findByPost_PostIdAndParentIsNull(Long postId, Pageable pageable);

    // 여러 부모의 직계 자식을 한 번에 조회
    @EntityGraph(attributePaths = {"user", "parent"})
    List<CommentEntity> findByParent_CommentIdIn(Collection<Long> parentIds);


}
