package com.b1a4.cafeOn.qna.question.dto;
// 마이페이지 - 내 문의 목록 웅답
// 추후에 코드 수정 후 삭제 예정

import com.b1a4.cafeOn.qna.question.dto.QuestionListResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class MyQuestionsResponseDTO {
    private String message;
    private List<QuestionListResponseDTO> questions;
}
