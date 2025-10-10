package com.b1a4.cafeOn.qna.question.dto;
// 리스트
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
