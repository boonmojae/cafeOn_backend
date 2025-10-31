package com.b1a4.cafeOn.admin.report.dto;

import com.b1a4.cafeOn.report.enums.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReportRequestDTO {

    // 신고 처리 상태 RESOLVED / REJECTED
    @NotNull(message = "status는 필수 값입니다.")
    private ReportStatus status;

    @NotBlank(message = "관리자 처리 메모를 입력해 주세요.")
    private String adminNote;
}
