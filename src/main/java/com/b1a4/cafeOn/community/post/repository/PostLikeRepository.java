package com.b1a4.cafeOn.community.post.repository;

import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.community.post.entity.PostLikeEntity;
import com.b1a4.cafeOn.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLikeEntity, Long> {

    // 게시글의 좋아요 카운트
    long countByPost(PostEntity post);

    // 게시글과 유저 정보로 좋아요 엔티티 조회
    Optional<PostLikeEntity> findByPostAndUser(PostEntity post, UserEntity user);

    // 게시글 목록 좋아요 카운트
    @Query("""
            SELECT l.post.postId AS postId, count(l) AS cnt
            FROM PostLikeEntity l
            WHERE l.post.postId in :postIds
            GROUP by l.post.postId
            """)
    List<LikeCount> findLikeCountByPostIdIn(@Param("postIds") List<Long> postIds);

    // 내가 좋아요 누른 게시글
    @Query("""
            SELECT DISTINCT pl.post.postId
            FROM PostLikeEntity pl
            WHERE pl.user.userId =:userId
            AND pl.post.postId in :postIds
            """)
    List<Long> findLikedPostIds(@Param("userId") String userId, @Param("postIds") List<Long> postIds);

    // 게시글 좋아요 여부
    boolean existsByPost_PostIdAndUser_UserId(Long postId, String userId);
}
