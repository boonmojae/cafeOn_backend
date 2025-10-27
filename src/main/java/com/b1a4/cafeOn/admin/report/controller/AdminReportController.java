package com.b1a4.cafeOn.admin.report.controller;

import com.b1a4.cafeOn.admin.report.dto.AdminReportRequestDTO;
import com.b1a4.cafeOn.admin.report.service.AdminReportService;
import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.report.enums.ReportStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Report (Admin)")
public class AdminReportController {

    private final AdminReportService reportService;

    // 신고 내역 조회
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getReports(@RequestParam(required = false) ReportStatus status) {
        return ResponseEntity.ok(
                ApiResponse.<Object>builder()
                        .message("신고 내역 조회 성공")
                        .data(reportService.getReports(status))
                        .build()
        );
    }

    // 신고 처리
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateReport(@PathVariable Long id,
                                                       @Valid @RequestBody AdminReportRequestDTO req,
                                                       @AuthenticationPrincipal String adminId) {
        reportService.updateReport(id, adminId, req);
        return ResponseEntity.ok(
                ApiResponse.<Object>builder()
                        .message("신고가 처리되었습니다.")
                        .build()
        );
    }
}
