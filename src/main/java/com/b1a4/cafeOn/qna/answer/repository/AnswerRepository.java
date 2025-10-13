package com.b1a4.cafeOn.qna.answer.repository;

import com.b1a4.cafeOn.qna.answer.entity.AnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<AnswerEntity, Long> {
    List<AnswerEntity> findByQuestionQuestionIdOrderByCreatedAtAsc(Long questionId);
}

