package com.b1a4.cafeOn.report.entity;

import com.b1a4.cafeOn.report.enums.ReportStatus;
import com.b1a4.cafeOn.report.enums.TargetType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reports",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reports_once",
                        columnNames = {"reporter_id", "target_type", "target_id"}
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    // 신고자
    @Column(name = "reporter_id", nullable = false, length = 36)
    private String reporterId;

    // 피신고자
    @Column(name = "reported_user_id", length = 36)
    private String reportedUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private ReportStatus status;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "admin_note")
    private String adminNote;

    @Column(name = "handled_by", length = 36)
    private String handledBy;

    @Column(name = "handled_at")
    private LocalDateTime handledAt;

    // 신고 처리(수락)
    public void resolve(String adminId, String note) {
        this.status = ReportStatus.RESOLVED;
        this.adminNote = note;
        this.handledBy = adminId;
        this.handledAt = LocalDateTime.now();
    }

    // 신고 기각
    public void reject(String adminId, String note) {
        this.status = ReportStatus.REJECTED;
        this.adminNote = note;
        this.handledBy = adminId;
        this.handledAt = LocalDateTime.now();
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (status == null) {
            this.status = ReportStatus.PENDING;
        }
    }
}
