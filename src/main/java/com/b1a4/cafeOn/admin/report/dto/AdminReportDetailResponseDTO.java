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
    // 신고 기본
    private Long reportId;
    private ReportStatus status;        // PENDING / RESOLVED / REJECTED
    private String targetType;          // POST / COMMENT / REVIEW
    private Long targetId;
    private Long parentId;
    private String content;             // 신고 사유(텍스트)
    private String reporterId;
    private String reporterNickname;
    private String reportedUserId;
    private String reportedNickname;
    private LocalDateTime createdAt;

    // 처리 정보(처리하기 버튼/모달에서 필요)
    private String adminNote;           // 처리 메모
    private String handledBy;           // 처리 관리자 ID
    private LocalDateTime handledAt;    // 처리 시각

    // 원본
    private TargetPreview target;

    @Getter @Setter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class TargetPreview {
        private String title;           // 댓글/제목 없는 타입이면 null
        private String body;            // 본문
        private List<String> imageUrls; // 없으면 []
    }
}
