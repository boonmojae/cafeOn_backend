package com.b1a4.cafeOn.review.repository;

import com.b1a4.cafeOn.review.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    Page<ReviewEntity> findByUser_UserId(String userId, Pageable pageable);

    Page<ReviewEntity> findByCafe_CafeId(Long cafeId, Pageable pageable);
    List<ReviewEntity> findByCafe_CafeId(Long cafeId);  // 작성자(김도이) 페이지어블 안쓰고, 전체가져오기용 메서드 오버로딩 추가

    // 리뷰 신고 - 작성자 userId
    @Query("SELECT r.user.userId FROM ReviewEntity r WHERE r.reviewId =:reviewId")
    Optional<String> findAuthorIdByReviewId(@Param("reviewId") Long reviewId);

//    @Query("""
//              SELECT r FROM ReviewEntity r
//              JOIN FETCH r.user u
//              JOIN FETCH r.cafe c
//              WHERE c.cafeId = :cafeId
//              ORDER BY r.createdAt DESC
//            """)
//    Page<ReviewEntity> findByCafeIdWithJoins(@Param("cafeId") Long cafeId, Pageable pageable);
}