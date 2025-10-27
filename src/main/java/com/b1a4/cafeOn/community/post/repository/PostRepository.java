package com.b1a4.cafeOn.community.post.repository;

import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.community.post.enums.PostType;
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
public interface PostRepository extends JpaRepository<PostEntity, Long> {

    // 전체 게시글 조회 필터링(타입, 키워드)
    @EntityGraph(attributePaths = {"user"})
    @Query("""
            SELECT p
            FROM PostEntity p
            LEFT JOIN p.user u
            WHERE (:type IS NULL OR p.type = :type)
              AND (
                  :kw IS NULL OR :kw = '' OR
                  LOWER(p.title)   LIKE LOWER(CONCAT('%', :kw, '%')) OR
                  LOWER(p.content) LIKE LOWER(CONCAT('%', :kw, '%')) OR
                  LOWER(u.nickname) LIKE LOWER(CONCAT('%'
                  , :kw, '%'))
              )
            ORDER BY p.createdAt DESC
            """)
    Page<PostEntity> search(
            @Param("type") PostType type,
            @Param("kw") String keyword,
            Pageable pageable
    );


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

    // 게시글 신고 - 작성자 userId 조회
    @Query("select p.user.userId from PostEntity p where p.postId = :postId")
    Optional<String> findAuthorIdByPostId(@Param("postId") Long postId);
    
    
    // 유저 탈퇴시 커뮤니티 데이터 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM PostEntity p
            WHERE p.user.userId =:userId
            """)
    int deleteByUserId(@Param("userId") String userId);

}
