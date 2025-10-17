package com.b1a4.cafeOn.qna.answer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerRequestDTO {

    @NotBlank(message = "답변 내용을 입력해주세요.")
    private String content;  // 답변 내용
}

