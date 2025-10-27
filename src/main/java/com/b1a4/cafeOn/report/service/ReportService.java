package com.b1a4.cafeOn.report.service;

import com.b1a4.cafeOn.community.comment.exception.CommentNotFoundException;
import com.b1a4.cafeOn.community.comment.repository.CommentRepository;
import com.b1a4.cafeOn.community.post.exception.PostNotFoundException;
import com.b1a4.cafeOn.community.post.repository.PostRepository;
import com.b1a4.cafeOn.report.dto.ReportRequestDTO;
import com.b1a4.cafeOn.report.dto.ReportResponseDTO;
import com.b1a4.cafeOn.report.entity.ReportEntity;
import com.b1a4.cafeOn.report.enums.ReportStatus;
import com.b1a4.cafeOn.report.enums.TargetType;
import com.b1a4.cafeOn.report.repository.ReportRepository;
import com.b1a4.cafeOn.review.exception.ReviewNotFoundException;
import com.b1a4.cafeOn.review.repository.ReviewRepository;
import com.b1a4.cafeOn.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    // 게시글 신고
    @Transactional
    public ReportResponseDTO reportPost(String reporterId, Long postId, ReportRequestDTO reportRequestDTO) {
        String reportedUserId = postRepository.findAuthorIdByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        return createReport(reporterId, TargetType.POST, postId, reportedUserId, reportRequestDTO.content());
    }

    // 댓글 신고
    @Transactional
    public ReportResponseDTO reportComment(String reporterId, Long commentId, ReportRequestDTO reportRequestDTO) {
        String reportedUserId = commentRepository.findAuthorIdByCommentId(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        return createReport(reporterId, TargetType.COMMENT, commentId, reportedUserId, reportRequestDTO.content());
    }

    // 카페 리뷰 신고
    @Transactional
    public ReportResponseDTO reportReview(String reporterId, Long reviewId, ReportRequestDTO reportRequestDTO) {
        String reportUserId = reviewRepository.findAuthorIdByReviewId(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
        return createReport(reporterId, TargetType.REVIEW, reviewId, reportUserId, reportRequestDTO.content());
    }


    // 공통 로직
    private ReportResponseDTO createReport(
            String reporterId, TargetType type, Long targetId,
            String reportedUserId, String content
    ) {
        if (reporterId.equals(reportedUserId)) {
            throw new IllegalStateException("본인 작성물은 신고할 수 없습니다.");
        }
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, type, targetId)) {
            throw new IllegalStateException("이미 신고한 대상입니다.");
        }

        ReportEntity saved = reportRepository.save(
                ReportEntity.builder()
                        .reporterId(reporterId)
                        .reportedUserId(reportedUserId)
                        .targetType(type)
                        .targetId(targetId)
                        .content(content)
                        .status(ReportStatus.PENDING)
                        .build()
        );

        String reporterNickname = userRepository.findNicknameById(reporterId).orElse(null);
        String reportedNickname = userRepository.findNicknameById(reportedUserId).orElse(null);

        return new ReportResponseDTO(
                saved.getReportId(),
                saved.getStatus(),
                saved.getTargetType(),
                saved.getTargetId(),
                saved.getContent(),
                saved.getReporterId(), reporterNickname,
                saved.getReportedUserId(), reportedNickname,
                saved.getCreatedAt()
        );
    }
}