package com.b1a4.cafeOn.qna.question.dto;
// 마이페이지 - 내 문의 상세 웅답
// 추후에 코드 수정 후 삭제 예정

import com.b1a4.cafeOn.qna.question.dto.QuestionDetailResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyQuestionResponseDTO {
    private String message;
    private QuestionDetailResponseDTO question;
}
