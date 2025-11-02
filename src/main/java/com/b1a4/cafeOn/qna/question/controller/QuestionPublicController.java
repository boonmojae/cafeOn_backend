package com.b1a4.cafeOn.qna.question.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.qna.question.dto.QuestionDetailResponseDTO;
import com.b1a4.cafeOn.qna.question.dto.QuestionListResponseDTO;
import com.b1a4.cafeOn.qna.question.dto.QuestionRequestDTO;
import com.b1a4.cafeOn.qna.question.entity.QuestionEntity;
import com.b1a4.cafeOn.qna.question.enums.QuestionVisibility;
import com.b1a4.cafeOn.qna.question.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qna/questions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Question", description = "문의 공개 API")
public class QuestionPublicController {

    private final QuestionService questionService;

    // 전체 문의 목록 조회 (비공개 제목은 "비공개 문의"로 표시)
    @GetMapping
    @Operation(
            summary = "문의 목록 조회",
            description = "전체 문의를 조회합니다. 비공개 문의의 제목은 '비공개 문의'로 표시됩니다."
    )
    public ResponseEntity<ApiResponse<Page<QuestionListResponseDTO>>> getAllQuestions(
            @AuthenticationPrincipal String userId,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(required = false) String keyword
    ) {
        try {
            Page<QuestionListResponseDTO> page = questionService.getQnaList(userId, keyword, pageable);
            return ResponseEntity.ok(
                    ApiResponse.<Page<QuestionListResponseDTO>>builder()
                            .message("문의 목록 조회 성공")
                            .data(page)
                            .build()
            );
        } catch (Exception e) {
            log.error("문의 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<QuestionListResponseDTO>>builder()
                            .message("문의 목록을 조회할 수 없습니다.")
                            .build());
        }
    }

    // 문의 상세 조회 (비공개는 작성자/관리자만 확인 가능)
    @GetMapping("/{id}")
    @Operation(
            summary = "문의 상세 조회",
            description = "특정 문의의 상세 정보를 조회합니다. 비공개 문의는 작성자 또는 관리자만 본문을 볼 수 있습니다."
    )
    public ResponseEntity<ApiResponse<QuestionDetailResponseDTO>> getQuestionDetail(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal String userId
    ) {
        try {
            QuestionEntity q = questionService.findById(id);
            boolean canView = questionService.canViewContent(q, userId);

            QuestionDetailResponseDTO dto;
            if (canView) {
                dto = QuestionDetailResponseDTO.from(q);
            } else {
                dto = QuestionDetailResponseDTO.builder()
                        .id(q.getQuestionId())
                        .title("비공개 문의")
                        .content(null)
                        .authorNickname(q.getUser().getNickname())
                        .createdAt(q.getCreatedAt())
                        .updatedAt(q.getUpdatedAt())
                        .status(q.getStatus())
                        .visibility(QuestionVisibility.PRIVATE)
                        .build();
            }

            return ResponseEntity.ok(
                    ApiResponse.<QuestionDetailResponseDTO>builder()
                            .message("문의 상세 조회 성공")
                            .data(dto)
                            .build()
            );

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<QuestionDetailResponseDTO>builder()
                            .message("존재하지 않거나 삭제된 문의입니다.")
                            .build());
        } catch (Exception e) {
            log.error("문의 상세 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<QuestionDetailResponseDTO>builder()
                            .message("문의 상세 조회 중 오류가 발생했습니다.")
                            .build());
        }
    }

    // 새 문의 작성
    @PostMapping
    @Operation(
            summary = "문의 등록",
            description = "새로운 문의를 등록합니다. 공개/비공개 여부(visibility)를 선택할 수 있습니다."
    )
    public ResponseEntity<ApiResponse<QuestionDetailResponseDTO>> createQuestion(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody QuestionRequestDTO dto
    ) {
        try {
            QuestionEntity saved = questionService.createQuestion(userId, dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<QuestionDetailResponseDTO>builder()
                            .message("문의가 등록되었습니다.")
                            .data(QuestionDetailResponseDTO.from(saved))
                            .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<QuestionDetailResponseDTO>builder()
                            .message(e.getMessage() != null ? e.getMessage() : "요청이 올바르지 않습니다.")
                            .build());
        } catch (Exception e) {
            log.error("문의 등록 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<QuestionDetailResponseDTO>builder()
                            .message("문의 등록 중 오류가 발생했습니다.")
                            .build());
        }
    }
}
