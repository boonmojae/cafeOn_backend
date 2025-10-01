package com.b1a4.cafeOn.dto.question;

import com.b1a4.cafeOn.entity.QuestionEntity;
import com.b1a4.cafeOn.enums.QuestionStatus;
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
