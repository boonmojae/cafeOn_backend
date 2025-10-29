package com.b1a4.cafeOn.cafe.repository;

import com.b1a4.cafeOn.cafe.entity.CafeEntity;
import com.b1a4.cafeOn.review.entity.ReviewEntity;
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
     * todo : 1-2. 검색어로 조회 (이거 빼는게맞지않나?)
     */
    @Query("""
            SELECT c FROM CafeEntity c
            WHERE c.name LIKE %:query% OR c.address LIKE %:query%
            """)
    List<CafeEntity> searchByQuery(@Param("query") String query);

    /**
     * todo : 1-3. 검색어 + 태그 조회 (필요 시) (얘또한 빼야맞지않나?)
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
     * 3. 사용자 위치기반 (위도/경도/반경) 근처 카페 조회(거리 계산 SQL)
     * Haversine 공식을 이용해 거리(m) 계산
     * 반경 `radius` m 이내 카페만 필터링
     * 정렬은 거리 오름차순
     * 최대 100개만 응답
     */
    @Query(value = """
    SELECT *,
           (6371000 * ACOS(
               COS(RADIANS(:latitude)) * COS(RADIANS(latitude))
               * COS(RADIANS(longitude) - RADIANS(:longitude))
               + SIN(RADIANS(:latitude)) * SIN(RADIANS(latitude))
           )) AS distance
    FROM cafes
    HAVING distance <= :radius
    ORDER BY distance ASC
    LIMIT 100
    """, nativeQuery = true)
    List<CafeEntity> findNearbyCafes(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radius") int radius
    );

    /**
     * 4. 랜덤 카페 10개 조회
     */
    @Query(value = "SELECT * FROM cafes ORDER BY RAND() LIMIT 10", nativeQuery = true)
    List<CafeEntity> findRandom10();

    /**
     * 5. 카페id로 태그 찾기
     */
    @Query(value = """
        SELECT t.name
        FROM cafe_tags ct
        JOIN tags t ON ct.tag_id = t.tag_id
        WHERE ct.cafe_id = :cafeId
    """, nativeQuery = true)
    List<String> findTagNamesByCafeId(@Param("cafeId") Long cafeId);


    //    평점순 정렬
    @Query("""
            SELECT c FROM CafeEntity c
            WHERE (:query IS NULL OR c.name LIKE %:query% OR c.address LIKE %:query%)
            ORDER BY c.avgRating DESC
            """)
    List<CafeEntity> searchCafesByRating(@Param("query") String query);
}
