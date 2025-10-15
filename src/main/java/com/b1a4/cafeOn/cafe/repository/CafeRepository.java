package com.b1a4.cafeOn.cafe.repository;

import com.b1a4.cafeOn.cafe.entity.CafeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CafeRepository extends JpaRepository<CafeEntity, Long> {
//    1. 전체 목록 조회
    List<CafeEntity> findAll();

//    1-1. 태그로 조회 todo: cafe_tags 테이블 만들고 join문 검토 필요
    @Query(value = """
            SELECT c.* FROM cafes c
            JOIN cafe_tags ct ON c.cafe_id = ct.cafe_id
            JOIN tags t ON ct.tag_id = t.tag_id
            WHERE t.name = :tag
            """, nativeQuery = true)
    List<CafeEntity> findByTag(@Param("tag") String tag);

//    1-2. 검색어로 조회
    @Query("""
            SELECT c FROM CafeEntity c
            WHERE c.name LIKE %:query% OR c.address LIKE %:query%
            """)
    List<CafeEntity> searchByQuery(@Param("query") String query);

//    1-3. 정렬기준으로 조회
//      1-3-1. 평점순
    @Query("""
            SELECT c FROM CafeEntity c
            ORDER BY c.avgRating DESC
            """)
    List<CafeEntity> findAllOrderByRating();

//      1-3-2. 찜 많은 순 todo: wishlist 테이블 만들고 JOIN문 검토 필요
    @Query(value = """
            SELECT * FROM cafes
            ORDER BY wish_count DESC
            """, nativeQuery = true)
    List<CafeEntity> findAllOrderByWishCount();

//      1-3-3. 랜덤순
    @Query(value = """
            SELECT * FROM cafes
            ORDER BY RAND()
            """, nativeQuery = true)
    List<CafeEntity> findRandom();

//    1-4. 검색어 + 정렬 조회
    @Query("""
            SELECT c FROM CafeEntity c
            WHERE c.name LIKE %:query% OR c.address LIKE %:query%
            ORDER BY
                CASE WHEN :sort = 'rating' THEN c.avgRating END DESC
            """)
    List<CafeEntity> searchByQueryAndSort(@Param("query") String query, @Param("sort") String sort);




//    랜덤10개
    @Query(value = "SELECT * FROM cafes ORDER BY RAND() LIMIT 10", nativeQuery = true)
    List<CafeEntity> findRandom10();

//    지도 기반 (위도/경도/반경)
    @Query(value = """
            SELECT * FROM cafes
            WHERE (6371 * acos(
                cos(radians(:lat)) * cos(radians(latitude)) * cos (radians(longitude) - radians(:lng)) +
                sin(radians(:lat)) * sin(radians(latitude))
            )) < :radius
            """, nativeQuery = true)
    List<CafeEntity> findNearby(@Param("lat") double lat, @Param("lng") double lng, @Param("radius") double radius);

//    평점순 정렬
    @Query("""
            SELECT c FROM CafeEntity c
            WHERE (:query IS NULL OR c.name LIKE %:query% OR c.address LIKE %:query%)
            ORDER BY c.avgRating DESC
            """)
    List<CafeEntity> searchCafesByRating(@Param("query") String query);
}
