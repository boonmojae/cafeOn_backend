package com.b1a4.cafeOn.cafe.repository;

import com.b1a4.cafeOn.cafe.entity.CafeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.Optional;

public interface CafeRepository extends JpaRepository<CafeEntity, Long> {
  @Query("SELECT c.name FROM CafeEntity c WHERE c.cafeId =:cafeId")
    Optional<String> findNameById(@Param("cafeId") Long cafeId);
  
    /**
     * 1. 전체 목록 조회
     */
    List<CafeEntity> findAll();

    /**
     * 1-1. 태그로 조회 todo: cafe_tags 테이블 만들고 join문 검토 필요
     * @param tag
     */
    @Query(value = """
            SELECT c.* FROM cafes c
            JOIN cafe_tags ct ON c.cafe_id = ct.cafe_id
            JOIN tags t ON ct.tag_id = t.tag_id
            WHERE t.name = :tag
            """, nativeQuery = true)
    List<CafeEntity> findByTag(@Param("tag") String tag);

    /**
     * 1-2. 검색어로 조회
     */
    @Query("""
            SELECT c FROM CafeEntity c
            WHERE c.name LIKE %:query% OR c.address LIKE %:query%
            """)
    List<CafeEntity> searchByQuery(@Param("query") String query);

    /**
     * 1-3. 검색어 + 태그 조회 (필요 시)
     */
    @Query(value = """
            SELECT c.* FROM cafes c
            JOIN cafe_tags ct ON c.cafe_id = ct.cafe_id
            JOIN tags t ON ct.tag_id = t.tag_id
            WHERE (c.name LIKE %:query% OR c.address LIKE %:query%)
            AND t.name = :tag
            """, nativeQuery = true)
    List<CafeEntity> searchByQueryAndTag(@Param("query") String query, @Param("tag") String tag);

    /**
     * 카카오 검색 결과의 이름 목록으로 DB 일괄 조회 (name 인덱스 활용)
     */
    List<CafeEntity> findByNameIn(List<String> names);

    /**
     * @Async 동기화 시 중복 체크용 (kakao_id)
     */
    boolean existsByKakaoId(String kakaoId);

//    (선택) 업데이트 로직을 구현할 경우 사용
    Optional<CafeEntity> findByKakaoId(String kakaoId);



    /**
     * 랜덤10개
     */
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
