package com.b1a4.cafeOn.qna.answer.dto;

import com.b1a4.cafeOn.qna.answer.entity.AnswerEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AnswerResponseDTO {
    private Long answerId;
    private Long questionId;
    private String adminNickname;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AnswerResponseDTO fromEntity(AnswerEntity entity) {
        return AnswerResponseDTO.builder()
                .answerId(entity.getAnswerId())
                .questionId(entity.getQuestion().getQuestionId())
                .adminNickname(entity.getAdmin().getNickname())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

