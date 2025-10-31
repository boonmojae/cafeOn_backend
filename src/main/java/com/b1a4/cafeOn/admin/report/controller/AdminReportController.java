package com.b1a4.cafeOn.admin.report.controller;

import com.b1a4.cafeOn.admin.report.dto.AdminReportDetailResponseDTO;
import com.b1a4.cafeOn.admin.report.dto.AdminReportRequestDTO;
import com.b1a4.cafeOn.admin.report.service.AdminReportService;
import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.report.enums.ReportStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/admin/reports", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Report (Admin)", description = "신고 내역 조회/상세/처리 (관리자)")
public class AdminReportController {

    private final AdminReportService reportService;

    // 신고 목록 조회 (상태별) - PENDING 기본

    @GetMapping
    @Operation(summary = "신고 목록 조회", description = "상태(PENDING/RESOLVED/REJECTED)에 따른 신고 목록을 조회합니다. 미지정 시 PENDING.")
    public ResponseEntity<ApiResponse<?>> getReports(@RequestParam(required = false) ReportStatus status) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("신고 내역 조회 성공")
                        .data(reportService.getReports(status))
                        .build()
        );
    }

    // 신고 상세 조회
    @GetMapping("/{id}")
    @Operation(summary = "신고 상세 조회", description = "신고 상세 정보 및 원본(제목/내용/이미지)을 반환합니다.")
    public ResponseEntity<ApiResponse<?>> getReportDetail(@PathVariable("id") Long id) {
        AdminReportDetailResponseDTO dto = reportService.getReportDetail(id);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("신고 상세 조회 성공")
                        .data(dto)
                        .build()
        );
    }

    // 신고 처리 (RESOLVED / REJECTED)
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "신고 처리", description = "신고 상태를 RESOLVED 또는 REJECTED로 변경합니다.")
    public ResponseEntity<ApiResponse<?>> updateReport(@PathVariable("id") Long id,
                                                       @Valid @RequestBody AdminReportRequestDTO req,
                                                       @AuthenticationPrincipal String adminId) {
        reportService.updateReport(id, adminId, req);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("신고가 처리되었습니다.")
                        .build()
        );
    }
}
