package com.b1a4.cafeOn.admin.penalty.repository;

import com.b1a4.cafeOn.admin.penalty.entity.PenaltyEntity;
import com.b1a4.cafeOn.admin.penalty.enums.PenaltyStatus;
import com.b1a4.cafeOn.admin.penalty.enums.PenaltyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PenaltyRepository extends JpaRepository<PenaltyEntity, Long> {

    // 사용자별 패널티 목록 (최신순) — 상세 화면용
    @EntityGraph(attributePaths = {"admin"})
    Page<PenaltyEntity> findByUser_UserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // 사용자별 전체 패널티 건수
    long countByUser_UserId(String userId);

    // 사용자별 타입별 패널티 건수 (예: WARNING 몇 건, SUSPEND 몇 건)
    long countByUser_UserIdAndPenaltyType(String userId, PenaltyType penaltyType);

    // 특정 신고(report_id)로 생성된 패널티 존재 여부 (중복 방지/멱등성)
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

    // 현재 진행 중인 정지 목록 (필요시 상세 확인용)
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

    // 필터링(선택): 타입/상태 다중 조건 + 페이지네이션
    Page<PenaltyEntity> findByUser_UserIdAndPenaltyTypeInAndStatusInOrderByCreatedAtDesc(
            String userId,
            List<PenaltyType> types,
            List<PenaltyStatus> statuses,
            Pageable pageable
    );
}
