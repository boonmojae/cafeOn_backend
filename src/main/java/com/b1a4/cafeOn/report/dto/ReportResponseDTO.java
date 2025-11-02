package com.b1a4.cafeOn.report.dto;

import com.b1a4.cafeOn.report.enums.ReportStatus;
import com.b1a4.cafeOn.report.enums.TargetType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "ReportResponseDTO", description = "신고 응답 DTO")
public record ReportResponseDTO(

        @Schema(description = "신고 ID", example = "101")
        Long reportId,

        @Schema(description = "신고 상태", example = "PENDING", allowableValues = {"PENDING","RESOLVED","REJECTED"})
        ReportStatus status,

        @Schema(description = "신고 대상 유형", example = "POST", allowableValues = {"POST","COMMENT","REVIEW","USER","CHAT"})
        TargetType targetType,

        @Schema(description = "신고 대상 ID", example = "345")
        Long targetId,

        @Schema(description = "신고 내용", example = "부적절한 표현이 포함되어 있습니다.")
        String content,

        @Schema(description = "신고자 ID", example = "userA")
        String reporterId,

        @Schema(description = "신고자 닉네임", example = "모짜")
        String reporterNickname,

        @Schema(description = "피신고자 ID", example = "userB")
        String reportedUserId,

        @Schema(description = "피신고자 닉네임", example = "도토리")
        String reportedNickname,

        @Schema(description = "신고 생성 시각(ISO-8601)", example = "2025-11-02T15:10:00")
        LocalDateTime createdAt
) {}
