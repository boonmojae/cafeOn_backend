package com.b1a4.cafeOn.mypage.wishlist.repository;

import com.b1a4.cafeOn.mypage.wishlist.dto.WishlistListResponseDTO;
import com.b1a4.cafeOn.mypage.wishlist.entity.WishlistEntity;
import com.b1a4.cafeOn.mypage.wishlist.enums.WishlistCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistEntity, Long> {

    // 특정 유저가 특정 카페를 특정 카테고리에 이미 담았는지 확인
    boolean existsByUserUserIdAndCafeCafeIdAndCategory(String userId, Long cafeId, WishlistCategory category);

    // 특정 카테고리의 위시 조회
    Optional<WishlistEntity> findByUserUserIdAndCafeCafeIdAndCategory(String userId, Long cafeId, WishlistCategory category);

    // 특정 카테고리의 위시 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    delete from WishlistEntity w
    where w.user.userId = :userId
      and w.cafe.cafeId = :cafeId
      and w.category = :category
""")
    int deleteByUserUserIdAndCafeCafeIdAndCategory(
            @Param("userId") String userId,
            @Param("cafeId") Long cafeId,
            @Param("category") WishlistCategory category
    );

    // 카테고리별 목록
    @Query("""
        select new com.b1a4.cafeOn.mypage.wishlist.dto.WishlistListResponseDTO(
            w.wishlistId,
            w.cafe.cafeId,
            w.cafe.name,
            concat('', w.category)
        )
        from WishlistEntity w
        where w.user.userId = :userId
          and w.category = :category
        order by w.createdAt desc
    """)
    Page<WishlistListResponseDTO> findListByUserAndCategory(
            @Param("userId") String userId,
            @Param("category") WishlistCategory category,
            Pageable pageable
    );


    // 이미 담긴 카테고리 조회
    @Query("""
        select w.category
        from WishlistEntity w
        where w.user.userId = :userId
          and w.cafe.cafeId = :cafeId
    """)
    List<WishlistCategory> findCategoriesByUserAndCafe(
            @Param("userId") String userId,
            @Param("cafeId") Long cafeId
    );
}
