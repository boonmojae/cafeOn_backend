package com.b1a4.cafeOn.qna.answer.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.qna.answer.dto.AnswerResponseDTO;
import com.b1a4.cafeOn.qna.answer.service.AnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qna/questions")
@RequiredArgsConstructor
@Validated
@Tag(name = "Answer", description = "문의 답변 조회 API")
public class AnswerPublicController {

    private final AnswerService answerService;

    // 특정 문의에 대한 답변 목록 조회
    @GetMapping("/{id}/answers")
    @Operation(
            summary = "문의 답변 목록 조회",
            description = "특정 문의에 등록된 답변 목록을 조회합니다. 비회원도 접근할 수 있습니다."
    )
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


