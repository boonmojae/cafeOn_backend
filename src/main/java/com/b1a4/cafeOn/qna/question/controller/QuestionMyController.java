package com.b1a4.cafeOn.qna.question.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.qna.question.dto.QuestionDetailResponseDTO;
import com.b1a4.cafeOn.qna.question.dto.QuestionListResponseDTO;
import com.b1a4.cafeOn.qna.question.dto.QuestionRequestDTO;
import com.b1a4.cafeOn.qna.question.enums.QuestionVisibility;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/my/questions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Question", description = "내 문의 관리 API")
public class QuestionMyController {

    private final QuestionService questionService;

    // 내가 작성한 문의 목록
    @GetMapping
    @Operation(
            summary = "내 문의 목록 조회",
            description = "로그인한 사용자가 작성한 문의 목록을 페이징하여 조회합니다. 키워드 검색도 가능합니다."
    )
    public ResponseEntity<ApiResponse<Page<QuestionListResponseDTO>>> getMyQuestions(
            @AuthenticationPrincipal String userId,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(required = false) String keyword
    ) {
        try {
            Page<QuestionListResponseDTO> page = questionService.getMyQuestions(userId, pageable, keyword);
            return ResponseEntity.ok(
                    ApiResponse.<Page<QuestionListResponseDTO>>builder()
                            .message("내가 작성한 문의 목록 조회 성공")
                            .data(page)
                            .build()
            );
        } catch (Exception e) {
            log.error("내 문의 목록 조회 중 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<QuestionListResponseDTO>>builder()
                            .message("문의 목록을 조회할 수 없습니다.")
                            .build());
        }
    }

    // 내가 작성한 문의 상세
    @GetMapping("/{id}")
    @Operation(
            summary = "내 문의 상세 조회",
            description = "로그인한 사용자가 작성한 특정 문의의 상세 정보를 조회합니다."
    )
    public ResponseEntity<ApiResponse<QuestionDetailResponseDTO>> getMyQuestion(
            @AuthenticationPrincipal String userId,
            @PathVariable("id") Long id
    ) {
        try {
            QuestionDetailResponseDTO detail = questionService.getMyQuestion(userId, id);
            return ResponseEntity.ok(
                    ApiResponse.<QuestionDetailResponseDTO>builder()
                            .message("문의 상세 조회 성공")
                            .data(detail) //
                            .build()
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<QuestionDetailResponseDTO>builder()
                            .message(e.getMessage() != null ? e.getMessage() : "존재하지 않는 문의입니다.")
                            .build());
        } catch (Exception e) {
            log.error("내 문의 상세 조회 중 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<QuestionDetailResponseDTO>builder()
                            .message("문의 상세를 조회할 수 없습니다.")
                            .build());
        }
    }

    // 문의 수정 (답변 전만 가능)
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "내 문의 수정",
            description = "답변이 등록되지 않은 문의만 수정할 수 있습니다."
    )
    public ResponseEntity<ApiResponse<Void>> updateMyQuestion(
            @AuthenticationPrincipal String userId,
            @PathVariable("id") Long id,
            @RequestBody QuestionRequestDTO req   // title, content, visibility(optional)
    ) {
        try {
            final String title = req.getTitle();
            final String content = req.getContent();
            final QuestionVisibility visibility = req.getVisibility();
            questionService.updateMyQuestion(userId, id, title, content, visibility);

            return ResponseEntity.ok(
                    ApiResponse.<Void>builder().message("문의가 수정되었습니다.").build()
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder().message(e.getMessage()).build());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.<Void>builder().message(e.getMessage()).build());
        } catch (Exception e) {
            log.error("내 문의 수정 중 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder().message("문의 수정 중 오류가 발생했습니다.").build());
        }
    }

    // 문의 삭제 (답변 전만 가능)
    @DeleteMapping("/{id}")
    @Operation(
            summary = "내 문의 삭제",
            description = "답변이 등록되지 않은 문의만 삭제할 수 있습니다."
    )
    public ResponseEntity<ApiResponse<Void>> deleteMyQuestion(
            @AuthenticationPrincipal String userId,
            @PathVariable("id") Long id
    ) {
        try {
            questionService.deleteMyQuestion(userId, id);
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder().message("문의가 삭제되었습니다.").build()
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder().message(e.getMessage()).build());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.<Void>builder().message(e.getMessage()).build());
        } catch (Exception e) {
            log.error("내 문의 삭제 중 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder().message("문의 삭제 중 오류가 발생했습니다.").build());
        }
    }
}
