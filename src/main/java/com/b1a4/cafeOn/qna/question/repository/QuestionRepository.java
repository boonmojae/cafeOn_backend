package com.b1a4.cafeOn.qna.question.repository;

import com.b1a4.cafeOn.qna.question.entity.QuestionEntity;
import com.b1a4.cafeOn.qna.question.enums.QuestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {
    @EntityGraph(attributePaths = {"user"})
    Page<QuestionEntity> findByStatusOrderByCreatedAtDesc(QuestionStatus status, Pageable pageable); // 미처리/처리 탭 -관리자

    @EntityGraph(attributePaths = {"user"})
    Page<QuestionEntity> findByUserUserIdOrderByCreatedAtDesc(String userId, Pageable pageable); // 내 문의 - 유저

    @EntityGraph(attributePaths = {"user"})
    Page<QuestionEntity> findAll(Pageable pageable);
}


