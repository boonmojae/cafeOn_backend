package com.b1a4.cafeOn.qna.answer.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.qna.answer.dto.AnswerRequestDTO;
import com.b1a4.cafeOn.qna.answer.dto.AnswerResponseDTO;
import com.b1a4.cafeOn.qna.answer.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
@Validated
public class AnswerController {

    private final AnswerService answerService;

    // 특정 문의에 대한 답변 작성
    @PostMapping("/{id}/answers")
    public ResponseEntity<ApiResponse<AnswerResponseDTO>> createAnswer(
            @PathVariable("id") Long questionId,
            @AuthenticationPrincipal String adminId,
            @Valid @RequestBody AnswerRequestDTO request
    ) {
        AnswerResponseDTO res = answerService.createAnswer(questionId, adminId, request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AnswerResponseDTO>builder()
                        .message("답변이 작성되었습니다.")
                        .data(res)
                        .build());
    }

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
