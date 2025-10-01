package com.b1a4.cafeOn.dto.question;

import com.b1a4.cafeOn.entity.QuestionEntity;
import com.b1a4.cafeOn.enums.QuestionStatus;
import com.b1a4.cafeOn.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class QuestionDetailResponseDTO {
    private Long questionId;
    private String title;
    private String content;
    private String authorNickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private QuestionType type;
    private QuestionStatus status;
    private Boolean isPrivate;

    public static QuestionDetailResponseDTO from(QuestionEntity question) {
        return QuestionDetailResponseDTO.builder()
                .questionId(question.getQuestionId())
                .title(question.getTitle())
                .content(question.getContent())
                .authorNickname(question.getUser().getNickname())
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .type(question.getType())
                .status(question.getStatus())
                .isPrivate(question.getIsPrivate())
                .build();
    }
}
