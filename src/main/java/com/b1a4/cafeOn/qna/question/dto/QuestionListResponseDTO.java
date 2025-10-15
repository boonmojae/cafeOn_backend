package com.b1a4.cafeOn.qna.question.dto;

import com.b1a4.cafeOn.qna.question.entity.QuestionEntity;
import com.b1a4.cafeOn.qna.question.enums.QuestionStatus;
import com.b1a4.cafeOn.qna.question.enums.QuestionVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class QuestionListResponseDTO {
    private Long id;
    private String title;
    private String authorNickname;
    private LocalDateTime createdAt;
    private QuestionStatus status;
    private QuestionVisibility visibility;

    // 유저용 목록 (타인기준)
    public static QuestionListResponseDTO fromUser(QuestionEntity question) {
        return fromUser(question, null); // 사용자 정보 모르면 타인으로 간주되어 제목 마스킹됨
    }

    // 유저용 목록 (현재 사용자 ID로 소유자 여부 판단)
    //  - 타인 비공개: "비공개 문의"
    //  - 본인 비공개: 원제목 노출
    public static QuestionListResponseDTO fromUser(QuestionEntity question, String currentUserId) {
        String authorNickname = (question.getUser() != null) ? question.getUser().getNickname() : null;
        boolean isOwner = (question.getUser() != null)
                && (currentUserId != null)
                && currentUserId.equals(question.getUser().getUserId());

        String safeTitle = (question.getVisibility() == QuestionVisibility.PRIVATE && !isOwner)
                ? "비공개 문의"
                : question.getTitle();

        return QuestionListResponseDTO.builder()
                .id(question.getQuestionId())
                .title(safeTitle)
                .authorNickname(authorNickname)
                .createdAt(question.getCreatedAt())
                .status(question.getStatus())
                .visibility(question.getVisibility())
                .build();
    }

    // 관리자용 목록
    public static QuestionListResponseDTO fromAdmin(QuestionEntity question) {
        return QuestionListResponseDTO.builder()
                .id(question.getQuestionId())
                .title(question.getTitle())
                .authorNickname(question.getUser().getNickname())
                .createdAt(question.getCreatedAt())
                .status(question.getStatus())
                .visibility(question.getVisibility())
                .build();
    }

    // 마이페이지(본인 글)
    public static QuestionListResponseDTO fromMy(QuestionEntity question) {
        return QuestionListResponseDTO.builder()
                .id(question.getQuestionId())
                .title(question.getTitle()) // 비공개여도 그대로 노출
                .authorNickname(question.getUser().getNickname())
                .createdAt(question.getCreatedAt())
                .status(question.getStatus())
                .visibility(question.getVisibility())
                .build();
    }
}
