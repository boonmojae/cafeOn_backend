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
    private Long questionId;
    private String title;
    private String authorNickname;
    private LocalDateTime createdAt;
    private QuestionStatus status; // 관리자 전용
    private QuestionVisibility visibility;

    // 유저용 목록
    public static QuestionListResponseDTO fromUser(QuestionEntity question) {
        return QuestionListResponseDTO.builder()
                .questionId(question.getQuestionId())
                .title(question.getVisibility() == QuestionVisibility.PRIVATE ? "비공개 문의" : question.getTitle())
                .authorNickname(question.getUser().getNickname())
                .createdAt(question.getCreatedAt())
                .visibility(question.getVisibility())
                .build();
    }

    // 관리자용 목록 (status 포함)
    public static QuestionListResponseDTO fromAdmin(QuestionEntity question) {
        return QuestionListResponseDTO.builder()
                .questionId(question.getQuestionId())
                .title(question.getTitle())
                .authorNickname(question.getUser().getNickname())
                .createdAt(question.getCreatedAt())
                .status(question.getStatus())
                .visibility(question.getVisibility())
                .build();
    }
}
