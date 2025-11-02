package com.b1a4.cafeOn.qna.answer.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.qna.answer.dto.AnswerRequestDTO;
import com.b1a4.cafeOn.qna.answer.dto.AnswerResponseDTO;
import com.b1a4.cafeOn.qna.answer.service.AnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Answer", description = "문의 답변 관리 API")
public class AnswerController {

    private final AnswerService answerService;

    // 특정 문의에 대한 답변 작성
    @PostMapping("/{id}/answers")
    @Operation(
            summary = "문의 답변 등록(관리자)",
            description = "특정 문의에 대한 답변을 등록합니다. 한 문의에 여러 답변 작성이 가능합니다."
    )
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
    @Operation(
            summary = "문의 답변 목록 조회(관리자)",
            description = "특정 문의에 대한 모든 답변을 조회합니다."
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
