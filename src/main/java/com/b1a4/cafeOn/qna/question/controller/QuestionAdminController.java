package com.b1a4.cafeOn.qna.question.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.qna.question.dto.QuestionDetailResponseDTO;
import com.b1a4.cafeOn.qna.question.dto.QuestionListResponseDTO;
import com.b1a4.cafeOn.qna.question.entity.QuestionEntity;
import com.b1a4.cafeOn.qna.question.enums.QuestionStatus;
import com.b1a4.cafeOn.qna.question.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Question (Admin)", description = "관리자 문의(QnA) 조회 API")
public class QuestionAdminController {

    private final QuestionService questionService;

    // 전체 문의 목록 (status 필터)
    @GetMapping
    @Operation(
            summary = "문의 목록 조회",
            description = "상태(status)와 키워드(keyword)로 필터링하고 페이징하여 문의 목록을 조회합니다. 비공개 문의도 관리자는 조회할 수 있습니다."
    )
    public ResponseEntity<ApiResponse<Page<QuestionListResponseDTO>>> getAllInquiries(
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(required = false) QuestionStatus status,
            @RequestParam(required = false) String keyword
    ) {
        try {
            Page<QuestionListResponseDTO> page = questionService.getAdminList(pageable, status, keyword);
            return ResponseEntity.ok(
                    ApiResponse.<Page<QuestionListResponseDTO>>builder()
                            .message("문의 목록 조회 성공")
                            .data(page)
                            .build()
            );
        } catch (Exception e) {
            log.error("관리자 문의 목록 조회 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<QuestionListResponseDTO>>builder()
                            .message("문의 목록을 조회할 수 없습니다.")
                            .build());
        }
    }

    // 문의 상세 (관리자: 비공개도 본문 노출)
    @GetMapping("/{id}")
    @Operation(
            summary = "문의 상세 조회",
            description = "문의의 상세 정보를 조회합니다. 비공개 문의라도 관리자는 본문을 확인할 수 있습니다."
    )
    public ResponseEntity<ApiResponse<QuestionDetailResponseDTO>> getInquiryDetail(@PathVariable Long id) {
        try {
            QuestionEntity q = questionService.findById(id);
            return ResponseEntity.ok(
                    ApiResponse.<QuestionDetailResponseDTO>builder()
                            .message("문의 상세 조회 성공")
                            .data(QuestionDetailResponseDTO.from(q))
                            .build()
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<QuestionDetailResponseDTO>builder()
                            .message("존재하지 않거나 삭제된 문의입니다.")
                            .build());
        } catch (Exception e) {
            log.error("관리자 문의 상세 조회 중 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<QuestionDetailResponseDTO>builder()
                            .message("문의 상세 조회 중 오류가 발생했습니다.")
                            .build());
        }
    }
}
