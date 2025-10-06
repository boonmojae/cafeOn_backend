package com.b1a4.cafeOn.community.comment.repository;

import com.b1a4.cafeOn.community.comment.entity.CommentEntity;
import com.b1a4.cafeOn.community.comment.entity.CommentLikeEntity;
import com.b1a4.cafeOn.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLikeEntity, Long> {

    // 특정 유저의 좋아요 확인(토글)
    Optional<CommentLikeEntity> findByCommentAndUser(CommentEntity comment, UserEntity user);

    // 단일 댓글의 좋아요 카운트
    long countByComment(CommentEntity comment);

    // 댓글 목록 좋아요 카운트
    @Query("""
            SELECT cl.comment.commentId as commentId, count(cl) as cnt
            FROM CommentLikeEntity cl
            WHERE cl.comment.commentId in :commentIds
            GROUP BY cl.comment.commentId
            """)
    List<LikeCount> findLikeCountByCommentIdIn(@Param("commentIds") List<Long> commentIds);

    // 내가 좋아요 누른 댓글 목록 조회
    @Query("""
            SELECT DISTINCT cl.comment.commentId 
            FROM CommentLikeEntity cl 
            WHERE cl.user.userId = :userId 
              AND cl.comment.commentId in :commentIds
            """)
    List<Long> findLikedCommentIds(@Param("userId") String userId, @Param("commentIds") List<Long> commentIds);

    // 댓글 좋아요 여부
    boolean existsByComment_CommentIdAndUser_UserId(Long commentId, String userId);

    // 내가 좋아요한 댓글 목록
    @EntityGraph(attributePaths = {"comment", "comment.post"})
    Page<CommentLikeEntity> findByUser_UserId(String userId, Pageable pageable);

}
