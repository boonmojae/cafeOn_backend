package com.b1a4.cafeOn.cafe.repository;

import com.b1a4.cafeOn.cafe.entity.CafeEntity;
import com.b1a4.cafeOn.review.entity.ReviewEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.Optional;

public interface CafeRepository extends JpaRepository<CafeEntity, Long> {
  @Query("SELECT c.name FROM CafeEntity c WHERE c.cafeId =:cafeId")
    Optional<String> findNameById(@Param("cafeId") Long cafeId);
  
    /**
     * 1. 전체 목록 조회 (이미지 있는 카페만)
     */
    @Query(value = "SELECT * FROM cafes WHERE photo_url IS NOT NULL AND photo_url <> ''", nativeQuery = true)
    List<CafeEntity> findAll();

    /**
     * 1-1. 태그로 조회 (이미지 있는 카페만)
     */
    @Query(value = """
        SELECT c.* FROM cafes c
        JOIN cafe_tags ct ON c.cafe_id = ct.cafe_id
        JOIN tags t ON ct.tag_id = t.tag_id
        WHERE t.name = :tag
        AND c.photo_url IS NOT NULL AND c.photo_url <> ''
        """, nativeQuery = true)
    List<CafeEntity> findByTag(@Param("tag") String tag);

    /**
     * todo : 1-2. 검색어로 조회 (이미지 있는 카페만)
     */
    @Query(value = """
            SELECT DISTINCT c.*
            FROM cafes c
            LEFT JOIN cafe_tags ct ON c.cafe_id = ct.cafe_id
            LEFT JOIN tags t ON ct.tag_id = t.tag_id
            WHERE c.name LIKE %:query%
            OR c.reviews_summary LIKE %:query%
            OR t.name LIKE %:query%
            AND c.photo_url IS NOT NULL AND c.photo_url <> ''
            """, nativeQuery = true)
    List<CafeEntity> searchByQuery(@Param("query") String query);

    /**
     * todo : 1-3. 검색어 + 태그 조회 (필요 시)(현재 사용 안됨)
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
     * 3. 사용자 위치기반 (위도/경도/반경) 근처 카페 조회(거리 계산 SQL) (이미지 있는 카페만)
     * Haversine 공식을 이용해 거리(m) 계산
     * 반경 `radius` m 이내 카페만 필터링
     * 정렬은 거리 오름차순
     * 최대 100개만 응답
     */
    @Query(value = """
    SELECT *, ST_Distance_Sphere(point(longitude, latitude), point(:longitude, :latitude)) AS distance
    FROM cafes
    WHERE latitude IS NOT NULL AND longitude IS NOT NULL
    AND photo_url IS NOT NULL AND photo_url <> ''
    AND ST_Distance_Sphere(point(longitude, latitude), point(:longitude, :latitude)) <= :radius
    ORDER BY distance ASC
    LIMIT 100
    """, nativeQuery = true)
    List<CafeEntity> findNearbyCafes(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radius") int radius
    );

    /**
     * 4. 랜덤 카페 10개 조회 (이미지 있는 카페만)
     */
    @Query(value = "SELECT * FROM cafes WHERE photo_url IS NOT NULL AND photo_url <> '' ORDER BY RAND() LIMIT 10", nativeQuery = true)
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

    /**
     * 5-1. 여러 카페의 태그를 일괄 조회 (N+1 문제 해결)
     */
    @Query(value = """
        SELECT ct.cafe_id, t.name
        FROM cafe_tags ct
        JOIN tags t ON ct.tag_id = t.tag_id
        WHERE ct.cafe_id IN :cafeIds
        ORDER BY ct.cafe_id, t.name
    """, nativeQuery = true)
    List<Object[]> findTagNamesByCafeIds(@Param("cafeIds") List<Long> cafeIds);



    /**
     * 6. 종합 인기지수 기반 Top 10 카페 조회 (가중 평균 방식) (이미지 있는 카페만)
     *
     * <p>인기지수(Hot Score) 계산 공식:</p>
     * <pre>
     * HotScore =
     *     (최근 7일 조회수 * w7d)
     *   + (전체 누적 조회수 * wAll)
     *   + (평균 평점 * 50 * wRate)
     *   + (리뷰 개수 * 5 * wRev)
     * </pre>
     *
     * 각 항목의 기본 가중치는 다음과 같습니다:
     * - w7d  : 최근 7일간 조회수 비중 (기본값 0.4)
     * - wAll : 누적 조회수 비중 (기본값 0.2)
     * - wRate: 평균 평점 비중 (기본값 0.2)
     * - wRev : 리뷰 수 비중 (기본값 0.2)
     *
     * <p>가중치는 Controller에서 요청 파라미터로 조정할 수 있습니다.<br>
     * 예: /api/cafes/hot10/weighted?w7d=0.4&wAll=0.2&wRate=0.2&wRev=0.2</p>
     *
     * <p>쿼리 방식: JOIN 대신 서브쿼리로 리뷰 수를 계산하여 alias 충돌 방지</p>
     *
     * @param w7d  최근 7일 조회수 가중치
     * @param wAll 누적 조회수 가중치
     * @param wRate 평균 평점 가중치
     * @param wRev 리뷰 수 가중치
     * @return 인기지수 기준 상위 10개 카페 엔티티 리스트
     */
    @Query(value = """
        SELECT c.*
        FROM cafes c
        WHERE c.photo_url IS NOT NULL AND c.photo_url <> ''
        ORDER BY (
            (c.views_last7d * :w7d)
            + (c.view_count * :wAll)
            + (IFNULL(c.avg_rating, 0) * 50 * :wRate)
            + ((SELECT COUNT(*) FROM reviews r WHERE r.cafe_id = c.cafe_id) * 5 * :wRev)
        ) DESC
        LIMIT 10
        """, nativeQuery = true)
    List<CafeEntity> findHotWeightedNative(
            @Param("w7d") double w7d,
            @Param("wAll") double wAll,
            @Param("wRate") double wRate,
            @Param("wRev") double wRev
    );

    /**
     * 6-1. 최근 7일 조회수 자동 갱신하는 배치 코드
     */
    @Modifying
    @Query("""
        UPDATE CafeEntity c
        SET c.viewsLast7d = (c.viewCount - c.lastViewCount),
            c.lastViewCount = c.viewCount
        """)
    void updateViewsLast7d();

    /**
     * 7. 찜 많은 카페 top10 조회 (이미지 있는 카페만)
     */
    @Query(value = """
        SELECT c.*
        FROM cafes c
        JOIN wishlists w ON w.cafe_id = c.cafe_id
        WHERE c.photo_url IS NOT NULL AND c.photo_url <> ''
        GROUP BY c.cafe_id
        ORDER BY COUNT(w.cafe_id) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<CafeEntity> findTopWishlistedCafesFull(@Param("limit") int limit);


    /**
     * 8. 찜 수 계산
     */
//    8-1. 다건 집계: 목록 응답에서 N+1 방지
    @Query(value = """
            SELECT w.cafe_id AS cafeId, COUNT(*) AS cnt
            FROM wishlists w
            WHERE w.cafe_id IN (:ids)
            GROUP BY w.cafe_id
            """, nativeQuery = true)
    List<Object[]> countWishByCafeIds(@Param("ids") List<Long> ids);

//    8-2. 단건 집계: 상세 응답
    @Query(value = "SELECT COUNT(*) FROM wishlists w WHERE w.cafe_id = :cafeId", nativeQuery = true)
    int countWishByCafeId(@Param("cafeId") Long cafeId);

    //    평점순 정렬
    @Query("""
            SELECT c FROM CafeEntity c
            WHERE (:query IS NULL OR c.name LIKE %:query% OR c.address LIKE %:query%)
            ORDER BY c.avgRating DESC
            """)
    List<CafeEntity> searchCafesByRating(@Param("query") String query);
}
