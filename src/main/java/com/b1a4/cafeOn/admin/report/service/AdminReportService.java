package com.b1a4.cafeOn.admin.report.service;

import com.b1a4.cafeOn.admin.report.dto.AdminReportRequestDTO;
import com.b1a4.cafeOn.report.dto.ReportResponseDTO;
import com.b1a4.cafeOn.report.entity.ReportEntity;
import com.b1a4.cafeOn.report.enums.ReportStatus;
import com.b1a4.cafeOn.report.repository.ReportRepository;
import com.b1a4.cafeOn.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    // 신고 내역 조회 (상태별)
    @Transactional(readOnly = true)
    public List<ReportResponseDTO> getReports(ReportStatus status) {
        List<ReportEntity> reports;

        if (status == null) {
            reports = reportRepository.findByStatus(ReportStatus.PENDING); // 기본값: 미처리
        } else {
            reports = reportRepository.findByStatus(status);
        }

        return reports.stream().map(r -> {
            String reporterNickname = userRepository.findNicknameById(r.getReporterId()).orElse(null);
            String reportedNickname = r.getReportedUserId() == null ? null :
                    userRepository.findNicknameById(r.getReportedUserId()).orElse(null);

            return new ReportResponseDTO(
                    r.getReportId(),
                    r.getStatus(),
                    r.getTargetType(),
                    r.getTargetId(),
                    r.getContent(),
                    r.getReporterId(), reporterNickname,
                    r.getReportedUserId(), reportedNickname,
                    r.getCreatedAt()
            );
        }).toList();
    }

     // 신고 처리 (RESOLVED / REJECTED)

    @Transactional
    public void updateReport(Long reportId, String adminId, AdminReportRequestDTO req) {
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다. id=" + reportId));

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신고입니다.");
        }

        switch (req.getStatus()) {
            case RESOLVED -> report.resolve(adminId, req.getAdminNote());
            case REJECTED -> report.reject(adminId, req.getAdminNote());
            default -> throw new IllegalArgumentException("status는 RESOLVED 또는 REJECTED만 가능합니다.");
        }

        log.info("신고 처리 완료 reportId={}, status={}, adminId={}", reportId, req.getStatus(), adminId);
    }
}
