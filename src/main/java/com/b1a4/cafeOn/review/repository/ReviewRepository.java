package com.b1a4.cafeOn.review.repository;

import com.b1a4.cafeOn.review.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    Page<ReviewEntity> findByUser_UserId(String userId, Pageable pageable);

    Page<ReviewEntity> findByCafe_CafeId(Long cafeId, Pageable pageable);

//    @Query("""
//              SELECT r FROM ReviewEntity r
//              JOIN FETCH r.user u
//              JOIN FETCH r.cafe c
//              WHERE c.cafeId = :cafeId
//              ORDER BY r.createdAt DESC
//            """)
//    Page<ReviewEntity> findByCafeIdWithJoins(@Param("cafeId") Long cafeId, Pageable pageable);
}
