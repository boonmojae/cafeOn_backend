package com.b1a4.cafeOn.qna.answer.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.qna.answer.dto.AnswerResponseDTO;
import com.b1a4.cafeOn.qna.answer.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qna/questions")
@RequiredArgsConstructor
@Validated
public class AnswerPublicController {

    private final AnswerService answerService;

    // 특정 문의에 대한 답변 목록 조회
    @GetMapping("/{id}/answers")
    public ResponseEntity<ApiResponse<List<AnswerResponseDTO>>> getAnswers(
            @PathVariable("id") Long questionId
    ) {
        List<AnswerResponseDTO> list = answerService.getAnswersByQuestion(questionId);
        return ResponseEntity.ok(
                ApiResponse.<List<AnswerResponseDTO>>builder()
                        .message("답변 목록 조회 성공")
                        .data(list)
                        .build()
        );
    }
}


