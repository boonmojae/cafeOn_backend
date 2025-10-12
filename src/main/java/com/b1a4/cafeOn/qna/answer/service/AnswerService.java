package com.b1a4.cafeOn.qna.answer.service;

import com.b1a4.cafeOn.qna.answer.dto.AnswerResponseDTO;
import com.b1a4.cafeOn.qna.answer.entity.AnswerEntity;
import com.b1a4.cafeOn.qna.answer.repository.AnswerRepository;
import com.b1a4.cafeOn.qna.question.entity.QuestionEntity;
import com.b1a4.cafeOn.qna.question.repository.QuestionRepository;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Transactional
    public AnswerResponseDTO createAnswer(Long questionId, String adminId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("답변 내용을 입력해주세요.");
        }

        QuestionEntity question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("문의가 존재하지 않습니다."));
        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("관리자가 존재하지 않습니다."));

        AnswerEntity answer = AnswerEntity.builder()
                .question(question)
                .admin(admin)
                .content(content)
                .build();

        answerRepository.save(answer);
        return AnswerResponseDTO.fromEntity(answer);
    }

    // 특정 문의에 대한 답변 목록 조회
    @Transactional(readOnly = true)
    public List<AnswerResponseDTO> getAnswersByQuestion(Long questionId) {
        // 존재하지 않는 문의 에러 처리
        questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("해당 문의를 찾을 수 없습니다."));

        return answerRepository.findByQuestionQuestionIdOrderByCreatedAtAsc(questionId)
                .stream()
                .map(AnswerResponseDTO::fromEntity)
                .toList();
    }
}
