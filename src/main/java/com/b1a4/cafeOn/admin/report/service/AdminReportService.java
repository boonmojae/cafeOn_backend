package com.b1a4.cafeOn.admin.report.service;

import com.b1a4.cafeOn.admin.report.dto.AdminReportDetailResponseDTO;
import com.b1a4.cafeOn.admin.report.dto.AdminReportRequestDTO;
import com.b1a4.cafeOn.community.comment.entity.CommentEntity;
import com.b1a4.cafeOn.community.comment.repository.CommentRepository;
import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.community.post.repository.PostRepository;
import com.b1a4.cafeOn.image.entity.ImageEntity;
import com.b1a4.cafeOn.report.dto.ReportResponseDTO;
import com.b1a4.cafeOn.report.entity.ReportEntity;
import com.b1a4.cafeOn.report.enums.ReportStatus;
import com.b1a4.cafeOn.report.enums.TargetType;
import com.b1a4.cafeOn.report.repository.ReportRepository;
import com.b1a4.cafeOn.review.entity.ReviewEntity;
import com.b1a4.cafeOn.review.repository.ReviewRepository;
import com.b1a4.cafeOn.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public List<ReportResponseDTO> getReports(ReportStatus status) {
        List<ReportEntity> reports = (status == null)
                ? reportRepository.findByStatus(ReportStatus.PENDING)
                : reportRepository.findByStatus(status);

        return reports.stream()
                .map(r -> new ReportResponseDTO(
                        r.getReportId(),
                        r.getStatus(),
                        r.getTargetType(),
                        r.getTargetId(),
                        r.getContent(),
                        r.getReporterId(),
                        userRepository.findNicknameById(r.getReporterId()).orElse(null),
                        r.getReportedUserId(),
                        r.getReportedUserId() == null ? null : userRepository.findNicknameById(r.getReportedUserId()).orElse(null),
                        r.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    // 신고 상세
    @Transactional(readOnly = true)
    public AdminReportDetailResponseDTO getReportDetail(Long reportId) {
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다. id=" + reportId));

        String reporterNickname = userRepository.findNicknameById(report.getReporterId()).orElse(null);
        String reportedNickname = (report.getReportedUserId() == null)
                ? null
                : userRepository.findNicknameById(report.getReportedUserId()).orElse(null);

        AdminReportDetailResponseDTO.TargetPreview preview;
        if (report.getTargetType() == TargetType.POST) {
            preview = buildPostPreview(report.getTargetId());
        } else if (report.getTargetType() == TargetType.COMMENT) {
            preview = buildCommentPreview(report.getTargetId());
        } else if (report.getTargetType() == TargetType.REVIEW) {
            preview = buildReviewPreview(report.getTargetId());
        } else {
            preview = emptyPreview();
        }

        return AdminReportDetailResponseDTO.builder()
                .reportId(report.getReportId())
                .status(report.getStatus())
                .targetType(report.getTargetType().name())
                .targetId(report.getTargetId())
                .content(report.getContent())
                .reporterId(report.getReporterId())
                .reporterNickname(reporterNickname)
                .reportedUserId(report.getReportedUserId())
                .reportedNickname(reportedNickname)
                .adminNote(report.getAdminNote())
                .handledBy(report.getHandledBy())
                .createdAt(report.getCreatedAt())
                .handledAt(report.getHandledAt())
                .target(preview)
                .build();
    }

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

    // 원본

    private AdminReportDetailResponseDTO.TargetPreview buildPostPreview(Long postId) {
        PostEntity post = postRepository.findById(postId).orElse(null);
        if (post == null) return emptyPreview();

        return AdminReportDetailResponseDTO.TargetPreview.builder()
                .title(post.getTitle())
                .body(post.getContent())
                .imageUrls(toImageUrls(post.getImages()))
                .build();
    }

    private AdminReportDetailResponseDTO.TargetPreview buildCommentPreview(Long commentId) {
        CommentEntity comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) return emptyPreview();

        return AdminReportDetailResponseDTO.TargetPreview.builder()
                .title(null) // 댓글은 제목 없음
                .body(comment.getContent())
                .imageUrls(List.of()) // 댓글은 이미지 없음
                .build();
    }

    private AdminReportDetailResponseDTO.TargetPreview buildReviewPreview(Long reviewId) {
        ReviewEntity review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) return emptyPreview();

        return AdminReportDetailResponseDTO.TargetPreview.builder()
                .title(null) // 리뷰는 제목 없음
                .body(review.getContent())
                .imageUrls(toImageUrls(review.getImages()))
                .build();
    }

    private AdminReportDetailResponseDTO.TargetPreview emptyPreview() {
        return AdminReportDetailResponseDTO.TargetPreview.builder()
                .title(null)
                .body(null)
                .imageUrls(List.of())
                .build();
    }

    private List<String> toImageUrls(List<ImageEntity> images) {
        if (images == null || images.isEmpty()) return List.of();
        return images.stream()
                .map(ImageEntity::getPublicUrl)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
