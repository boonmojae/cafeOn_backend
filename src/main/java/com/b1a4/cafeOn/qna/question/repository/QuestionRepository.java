package com.b1a4.cafeOn.qna.question.repository;

import com.b1a4.cafeOn.qna.question.entity.QuestionEntity;
import com.b1a4.cafeOn.qna.question.enums.QuestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {

    //관리자: 상태별 목록 (미처리/처리 탭 등), 최신순
    @EntityGraph(attributePaths = {"user"})
    Page<QuestionEntity> findByStatusOrderByCreatedAtDesc(QuestionStatus status, Pageable pageable);

    // 마이페이지: 특정 사용자의 문의 목록, 최신순
    @EntityGraph(attributePaths = {"user"})
    Page<QuestionEntity> findByUserUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // 마이페이지: 본인이 작성한 단일 문의 조회 (소유자 필터 포함)
    @EntityGraph(attributePaths = {"user"})
    Optional<QuestionEntity> findByQuestionIdAndUserUserId(Long questionId, String userId);

    // 공개/관리자 공통: 전체 목록 (필요 시), 최신순 정렬은 Pageable에서 지정
    @EntityGraph(attributePaths = {"user"})
    Page<QuestionEntity> findAll(Pageable pageable);
}
