package com.b1a4.cafeOn.qna.question.controller;

import com.b1a4.cafeOn.qna.question.dto.QuestionDetailResponseDTO;
import com.b1a4.cafeOn.qna.question.dto.QuestionListResponseDTO;
import com.b1a4.cafeOn.qna.question.dto.QuestionRequestDTO;
import com.b1a4.cafeOn.qna.question.entity.QuestionEntity;
import com.b1a4.cafeOn.qna.question.enums.QuestionVisibility;
import com.b1a4.cafeOn.qna.question.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
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
public class QuestionPublicController {

    private final QuestionService questionService;

    // 전체 문의 목록 조회 (비공개 제목은 "비공개 문의"로 표시)
//    @Operation(summary = "전체 문의 목록 조회", description = "비공개 글은 제목을 '비공개 문의'로 표시", security = {}) // 공개면 security 비우기
    @GetMapping
    public ResponseEntity<Page<QuestionListResponseDTO>> getAllQuestions(
            @AuthenticationPrincipal String userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        try {
            Page<QuestionEntity> entities = questionService.getAllQuestionsForPublic(pageable);

            Page<QuestionListResponseDTO> dtos = entities.map(question -> {
                // 본인 여부 체크
                boolean isOwner = userId != null && userId.equals(question.getUser().getUserId());

                // 비공개 문의일 때 본인만 원제목, 나머지는 '비공개 문의'
                String title = (question.getVisibility() == QuestionVisibility.PRIVATE && !isOwner)
                        ? "비공개 문의"
                        : question.getTitle();

                return QuestionListResponseDTO.builder()
                        .questionId(question.getQuestionId())
                        .title(title)
                        .authorNickname(question.getUser().getNickname())
                        .createdAt(question.getCreatedAt())
                        .visibility(question.getVisibility())
                        .build();
            });

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("문의 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    // 문의 상세 조회 (비공개는 작성자/관리자만 확인 가능)
    @GetMapping("/{id}")
    public ResponseEntity<QuestionDetailResponseDTO> getQuestionDetail(
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
                        .questionId(q.getQuestionId())
                        .title("비공개 문의")
                        .content(null)
                        .authorNickname(q.getUser().getNickname())
                        .createdAt(q.getCreatedAt())
                        .updatedAt(q.getUpdatedAt())
                        .status(q.getStatus())
                        .visibility(QuestionVisibility.PRIVATE)
                        .build();
            }

            return ResponseEntity.ok(dto);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("문의 상세 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 새 문의 작성
    @PostMapping
    public ResponseEntity<QuestionDetailResponseDTO> createQuestion(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody QuestionRequestDTO dto
    ) {
        try {
            QuestionEntity saved = questionService.createQuestion(userId, dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(QuestionDetailResponseDTO.from(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("문의 등록 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
