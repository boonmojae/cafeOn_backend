package com.b1a4.cafeOn.community.post.repository;

import com.b1a4.cafeOn.community.post.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {

    // 유저가 작성한 게시글 목록
    Page<PostEntity> findAllByUser_UserId(String userId, Pageable pageable);

    // 다건 조회(작성자)
    @EntityGraph(attributePaths = {"user"})
    List<PostEntity> findByPostIdIn(Collection<Long> ids);

    // 단건 상세(작성자, 이미지)
    @EntityGraph(attributePaths = {"user", "images"})
    Optional<PostEntity> findWithUserAndImagesByPostId(Long postId);

    // 게시글 목록에 대한 댓글 수 카운트
    @Query("""
            SELECT c.post.postId AS postId, COUNT(c) AS cnt
            FROM CommentEntity c
            WHERE c.post.postId IN :postIds
            GROUP BY c.post.postId
            """)
    List<PostCommentCount> findCommentCountByPostIdIn(@Param("postIds") List<Long> postIds);

    interface PostCommentCount {
        Long getPostId();
        Long getCnt();
    }

}
