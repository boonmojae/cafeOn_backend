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
public class QuestionDetailResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String authorNickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private QuestionStatus status;
    private QuestionVisibility visibility;

    public static QuestionDetailResponseDTO from(QuestionEntity question) {
        return QuestionDetailResponseDTO.builder()
                .id(question.getQuestionId())
                .title(question.getTitle())
                .content(question.getContent())
                .authorNickname(question.getUser().getNickname())
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .status(question.getStatus())
                .visibility(question.getVisibility())
                .build();
    }

    public static QuestionDetailResponseDTO fromMy(QuestionEntity q) {
        return QuestionDetailResponseDTO.builder()
                .id(q.getQuestionId())
                .title(q.getTitle())
                .content(q.getContent())
                .authorNickname(q.getUser() != null ? q.getUser().getNickname() : null)
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .visibility(q.getVisibility())
                .build();
    }

}
