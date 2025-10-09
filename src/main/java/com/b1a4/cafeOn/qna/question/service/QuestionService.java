package com.b1a4.cafeOn.qna.question.service;

import com.b1a4.cafeOn.qna.question.entity.QuestionEntity;
import com.b1a4.cafeOn.qna.question.enums.QuestionStatus;
import com.b1a4.cafeOn.qna.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionRepository questionRepository;

    /* =========================================================
     * [유저/공개] 전체 문의 목록 조회
     *  - GET /api/qna/questions
     *  - 비공개 제목 마스킹은 DTO.fromPublic에서 처리
     *  - status는 응답에 포함하지 않음(fromPublic 사용)
     * ========================================================= */
    @Transactional(readOnly = true)
    public Page<QuestionEntity> getAllQuestionsForPublic(Pageable pageable,
                                                         @Nullable QuestionType type /* 검색 q는 이후 추가 */) {
        if (type != null) {
            return questionRepository.findByType(type, pageable);
        }
        return questionRepository.findAll(pageable);
    }

    /* =========================================================
     * [관리자] 전체 문의 목록 조회 (status/type 필터)
     *  - GET /api/admin/inquiries
     *  - status/type 둘 다 없으면 전체 조회
     *  - status/type 조합으로 필터
     * ========================================================= */
    @Transactional(readOnly = true)
    public Page<QuestionEntity> getAllInquiriesForAdmin(Pageable pageable,
                                                        @Nullable QuestionStatus status,
                                                        @Nullable QuestionType type) {
        if (status != null && type != null) {
            return questionRepository.findByStatusAndType(status, type, pageable);
        } else if (status != null) {
            return questionRepository.findByStatus(status, pageable);
        } else if (type != null) {
            return questionRepository.findByType(type, pageable);
        }
        return questionRepository.findAll(pageable);
    }
}
