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


    // Map<Long, Long> 반환 → 인터페이스 리스트 반환으로 교체
    @Query("""
       SELECT l.post.postId AS postId, count(l) AS cnt
       FROM PostLikeEntity l
       WHERE l.post.postId in :postIds
       GROUP by l.post.postId
       """)
    List<LikeCount> findLikeCountByPostIdIn(@Param("postIds") List<Long> postIds);

//    @Query("SELECT l.post.postId, COUNT(l) " +
//            "FROM PostLikeEntity l " +
//            "WHERE l.post IN :posts " +
//            "GROUP BY l.post.postId")
//    Map<Long, Long> findLikeCountByPostIn(@Param("posts")List<PostEntity> posts); postId, count
}
