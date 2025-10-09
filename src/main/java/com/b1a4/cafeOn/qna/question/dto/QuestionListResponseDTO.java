package com.b1a4.cafeOn.qna.question.dto;

import com.b1a4.cafeOn.qna.question.entity.QuestionEntity;
import com.b1a4.cafeOn.qna.question.enums.QuestionStatus;
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
    private QuestionStatus status;
    private Boolean isPrivate;

    // 유저 리스트
    public static QuestionListResponseDTO fromPublic(QuestionEntity question) {
        return QuestionListResponseDTO.builder()
                .questionId(question.getQuestionId())
                .title(Boolean.TRUE.equals(question.getIsPrivate()) ? "비공개 문의" : question.getTitle())
                .authorNickname(question.getUser().getNickname())
                .createdAt(question.getCreatedAt())
                .isPrivate(question.getIsPrivate())
                .build(); // status 미설정 → null → 응답에서 숨김
    }

    // 관리자 리스트
    public static QuestionListResponseDTO from(QuestionEntity question) {
        return QuestionListResponseDTO.builder()
                .questionId(question.getQuestionId())
                .title(question.getIsPrivate() ? "비공개 문의" : question.getTitle()) // isPrivate == true → "비공개 문의"
                .authorNickname(question.getUser().getNickname())
                .createdAt(question.getCreatedAt())
                .status(question.getStatus())
                .isPrivate(question.getIsPrivate())
                .build();
    }
}
