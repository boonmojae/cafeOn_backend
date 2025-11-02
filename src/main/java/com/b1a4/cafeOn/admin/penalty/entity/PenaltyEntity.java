package com.b1a4.cafeOn.admin.penalty.entity;

import com.b1a4.cafeOn.admin.penalty.enums.PenaltyStatus;
import com.b1a4.cafeOn.admin.penalty.enums.PenaltyType;
import com.b1a4.cafeOn.admin.penalty.enums.ReasonCode;
import com.b1a4.cafeOn.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "penalties",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_penalties_report",
                        columnNames = {"report_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PenaltyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "penalty_id")
    private Long penaltyId;

    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private UserEntity admin;

    @Enumerated(EnumType.STRING)
    @Column(name = "penalty_type", nullable = false)
    private PenaltyType penaltyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code")
    private ReasonCode reasonCode;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PenaltyStatus status = PenaltyStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 생성 시각 자동 설정
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 정지기간 체크
    public boolean isActiveSuspend() {
        return penaltyType == PenaltyType.SUSPEND
                && status == PenaltyStatus.ACTIVE
                && startsAt != null && endsAt != null
                && LocalDateTime.now().isBefore(endsAt);
    }
}
