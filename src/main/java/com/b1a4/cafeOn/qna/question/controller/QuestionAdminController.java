package com.b1a4.cafeOn.qna.question.controller;

import com.b1a4.cafeOn.qna.question.dto.QuestionDetailResponseDTO;
import com.b1a4.cafeOn.qna.question.dto.QuestionListResponseDTO;
import com.b1a4.cafeOn.qna.question.entity.QuestionEntity;
import com.b1a4.cafeOn.qna.question.enums.QuestionStatus;
import com.b1a4.cafeOn.qna.question.service.QuestionService;
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
public class QuestionAdminController {

    private final QuestionService questionService;

    // 전체 문의 목록 (status 필터)
    @GetMapping
    public ResponseEntity<Page<QuestionListResponseDTO>> getAllInquiries(
            @ParameterObject  // 추가하면 page/size/sort가 개별 칸으로 나옴
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(required = false) QuestionStatus status
    ) {
        Page<QuestionEntity> entities = questionService.getAllInquiriesForAdmin(pageable, status);
        Page<QuestionListResponseDTO> dtos = entities.map(QuestionListResponseDTO::fromAdmin);
        return ResponseEntity.ok(dtos);
    }

    // 문의 상세 (관리자: 비공개도 본문 노출)
    @GetMapping("/{id}")
    public ResponseEntity<QuestionDetailResponseDTO> getInquiryDetail(@PathVariable Long id) {
        try {
            QuestionEntity q = questionService.findById(id);
            return ResponseEntity.ok(QuestionDetailResponseDTO.from(q));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("관리자 문의 상세 조회 중 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
