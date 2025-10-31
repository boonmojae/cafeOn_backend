package com.b1a4.cafeOn.admin.report.dto;

import com.b1a4.cafeOn.report.enums.ReportStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportDetailResponseDTO {

    private Long reportId;
    private ReportStatus status;
    private String targetType;
    private Long targetId;
    private Long parentId;
    private String content;
    private String reporterId;
    private String reporterNickname;
    private String reportedUserId;
    private String reportedNickname;
    private LocalDateTime createdAt;

    // 처리 정보
    private String adminNote;
    private String handledBy;
    private LocalDateTime handledAt;

    // 원본
    private TargetPreview target;

    @Getter @Setter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class TargetPreview {
        private String title;
        private String body;
        private List<String> imageUrls;
    }
}
