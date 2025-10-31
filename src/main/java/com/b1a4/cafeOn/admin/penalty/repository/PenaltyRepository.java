package com.b1a4.cafeOn.admin.penalty.repository;

import com.b1a4.cafeOn.admin.penalty.entity.PenaltyEntity;
import com.b1a4.cafeOn.admin.penalty.enums.PenaltyStatus;
import com.b1a4.cafeOn.admin.penalty.enums.PenaltyType;
import com.b1a4.cafeOn.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface PenaltyRepository extends JpaRepository<PenaltyEntity, Long> {

    // 사용자별 패널티 목록
    @EntityGraph(attributePaths = {"admin"})
    Page<PenaltyEntity> findByUser_UserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // 사용자별 전체 패널티 건수
    long countByUser_UserId(String userId);

    // 사용자별 타입별 패널티 건수
    long countByUser_UserIdAndPenaltyType(String userId, PenaltyType penaltyType);

    boolean existsByReportId(Long reportId);

    // 현재 정지 중인지 여부
    @Query("""
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM PenaltyEntity p
        WHERE p.user.userId = :userId
          AND p.penaltyType = com.b1a4.cafeOn.admin.penalty.enums.PenaltyType.SUSPEND
          AND p.status = com.b1a4.cafeOn.admin.penalty.enums.PenaltyStatus.ACTIVE
          AND :now BETWEEN p.startsAt AND p.endsAt
    """)
    boolean hasActiveSuspension(@Param("userId") String userId, @Param("now") LocalDateTime now);

    // 현재 진행 중인 정지 목록
    List<PenaltyEntity> findByUser_UserIdAndPenaltyTypeAndStatusAndStartsAtBeforeAndEndsAtAfter(
            String userId,
            PenaltyType penaltyType,
            PenaltyStatus status,
            LocalDateTime startsBefore,
            LocalDateTime endsAfter
    );

    // 활성 정지의 최종 종료 시각 (가장 늦게 끝나는 것)
    @Query("""
        SELECT MAX(p.endsAt)
        FROM PenaltyEntity p
        WHERE p.user.userId = :userId
          AND p.penaltyType = com.b1a4.cafeOn.admin.penalty.enums.PenaltyType.SUSPEND
          AND p.status = com.b1a4.cafeOn.admin.penalty.enums.PenaltyStatus.ACTIVE
          AND :now < p.endsAt
    """)
    LocalDateTime latestActiveSuspendEndsAt(@Param("userId") String userId, @Param("now") LocalDateTime now);

    Page<PenaltyEntity> findByUser_UserIdAndPenaltyTypeInAndStatusInOrderByCreatedAtDesc(
            String userId,
            List<PenaltyType> types,
            List<PenaltyStatus> statuses,
            Pageable pageable
    );

    // 패널티 목록
    List<PenaltyEntity> findByUserOrderByCreatedAtDesc(UserEntity user);

    // 여러 유저의 패널티 수를 한 번에 집계 (userId -> count)
    @Query("""
       SELECT p.user.userId AS userId, COUNT(p) AS cnt
       FROM PenaltyEntity p
       WHERE p.user.userId IN :userIds
       GROUP BY p.user.userId
       """)
    List<Object[]> countGroupByUserIdRaw(@Param("userIds") List<String> userIds);

    default Map<String, Long> countGroupByUserId(List<String> userIds) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : countGroupByUserIdRaw(userIds)) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }
}
