package com.b1a4.cafeOn.qna.question.dto;

import com.b1a4.cafeOn.qna.question.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequestDTO {
    private String title;
    private String content;
    private Boolean isPrivate;
    private QuestionType type;
}

