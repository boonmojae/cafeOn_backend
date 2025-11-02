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

    // 리뷰 신고 - 작성자 userId
    @Query("SELECT r.user.userId FROM ReviewEntity r WHERE r.reviewId =:reviewId")
    Optional<String> findAuthorIdByReviewId(@Param("reviewId") Long reviewId);

    List<ReviewEntity> findByCafe_CafeId(Long cafeId);

}