package com.b1a4.cafeOn.report.dto;

import com.b1a4.cafeOn.report.enums.ReportStatus;
import com.b1a4.cafeOn.report.enums.TargetType;

import java.time.LocalDateTime;

public record ReportResponseDTO(
    Long reportId,
    ReportStatus status,
    TargetType targetType,
    Long targetId,
    String content,
    String reporterId,
    String reporterNickname,
    String reportedUserId,
    String reportedNickname,
    LocalDateTime createdAt
) {}
