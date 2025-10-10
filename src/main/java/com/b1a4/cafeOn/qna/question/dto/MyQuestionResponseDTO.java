package com.b1a4.cafeOn.qna.question.dto;
// 상세
import com.b1a4.cafeOn.qna.question.dto.QuestionDetailResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyQuestionResponseDTO {
    private String message;
    private QuestionDetailResponseDTO question;
}
