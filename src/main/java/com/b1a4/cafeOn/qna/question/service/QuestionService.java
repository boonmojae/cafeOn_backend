package com.b1a4.cafeOn.qna.question.service;

import com.b1a4.cafeOn.qna.question.dto.QuestionDetailResponseDTO;
import com.b1a4.cafeOn.qna.question.dto.QuestionListResponseDTO;
import com.b1a4.cafeOn.qna.question.dto.QuestionRequestDTO;
import com.b1a4.cafeOn.qna.question.entity.QuestionEntity;
import com.b1a4.cafeOn.qna.question.enums.QuestionVisibility;
import com.b1a4.cafeOn.qna.question.enums.QuestionStatus;
import com.b1a4.cafeOn.qna.question.repository.QuestionRepository;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import com.b1a4.cafeOn.user.enums.UserStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;


    // [공개/유저] 전체 문의 목록 조회
    @Transactional(readOnly = true)
    public Page<QuestionEntity> getAllQuestionsForPublic(Pageable pageable) {
        return questionRepository.findAll(pageable);
    }

    // [관리자] 전체 문의 목록 조회 (status 선택 필터)
    @Transactional(readOnly = true)
    public Page<QuestionEntity> getAllInquiriesForAdmin(Pageable pageable,
                                                        @Nullable QuestionStatus status) {
        if (status != null) {
            return questionRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }
        return questionRepository.findAll(pageable);
    }


    // [공개/유저] 문의 상세 조회
    @Transactional(readOnly = true)
    public QuestionEntity findById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("문의가 존재하지 않습니다. id=" + id));
    }

    // 비공개 본문 열람 가능 여부: PUBLIC 이거나, 작성자이거나, 관리자이면 true
    public boolean canViewContent(QuestionEntity q, @Nullable String requesterUserId) {
        if (q.getVisibility() == QuestionVisibility.PUBLIC) {
            return true;
        }
        boolean isOwner = requesterUserId != null
                && q.getUser() != null
                && q.getUser().getUserId().equals(requesterUserId);
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return isOwner || isAdmin;
    }


    // [유저] 새 문의 작성
    @Transactional
    public QuestionEntity createQuestion(String userId, QuestionRequestDTO dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. userId=" + userId));

        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 필수입니다.");
        }

        QuestionVisibility visibility =
                dto.getVisibility() != null ? dto.getVisibility() : QuestionVisibility.PRIVATE;

        QuestionEntity entity = QuestionEntity.builder()
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .visibility(visibility)
                .status(QuestionStatus.PENDING)
                .build();

        return questionRepository.save(entity);
    }

    // 마이페이지
    // 내가 작성한 문의
    // GET /api/my/questions
    @Transactional(readOnly = true)
    public Page<QuestionListResponseDTO> getMyQuestions(String userId, Pageable pageable) {
        userStatus(userId);

        Page<QuestionEntity> page = questionRepository
                .findByUserUserIdOrderByCreatedAtDesc(userId, pageable);

        return page.map(q -> QuestionListResponseDTO.builder()
                .id(q.getQuestionId())
                .title(q.getTitle())
                .status(q.getStatus())                 // PENDING | ANSWERED
                .visibility(q.getVisibility())         // PUBLIC | PRIVATE
                .authorNickname(q.getUser() != null ? q.getUser().getNickname() : null)
                .createdAt(q.getCreatedAt())
                .build());
    }
    // 내가 작성한 문의 상세
    /** GET /api/my/questions/{id} */
    @Transactional(readOnly = true)
    public QuestionDetailResponseDTO getMyQuestion(String userId, Long id) {
        userStatus(userId);

        //
        QuestionEntity q = questionRepository
                .findByQuestionIdAndUserUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("문의가 존재하지 않습니다. ID=" + id));

        return toDetailDTO(q);
    }

    // PUT /api/my/questions/{id}
    @Transactional
    public void updateMyQuestion(String userId, Long id, String title, String content, QuestionVisibility visibility) {
        userStatus(userId);

        QuestionEntity q = questionRepository
                .findByQuestionIdAndUserUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("문의가 존재하지 않습니다. ID=" + id));

        // 답변 등록 후 수정 불가
        if (q.getStatus() == QuestionStatus.ANSWERED) {
            throw new IllegalStateException("답변이 등록된 문의는 수정할 수 없습니다.");
        }

        // null 아닌 값만 반영 (PUT이지만 부분 수정 허용)
        if (title != null)   q.setTitle(title);
        if (content != null) q.setContent(content);
        if (visibility != null) q.setVisibility(visibility);
        // JPA dirty checking으로 자동 반영
    }

    // DELETE /api/my/questions/{id}
    @Transactional
    public void deleteMyQuestion(String userId, Long id) {
        userStatus(userId);

        QuestionEntity q = questionRepository
                .findByQuestionIdAndUserUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("문의가 존재하지 않습니다. ID=" + id));

        // 답변 등록 후 삭제 불가
        if (q.getStatus() == QuestionStatus.ANSWERED) {
            throw new IllegalStateException("답변이 등록된 문의는 삭제할 수 없습니다.");
        }

        questionRepository.delete(q);
    }

    private QuestionDetailResponseDTO toDetailDTO(QuestionEntity q) {
        return QuestionDetailResponseDTO.builder()
                .id(q.getQuestionId())
                .title(q.getTitle())
                .content(q.getContent())
                .authorNickname(q.getUser() != null ? q.getUser().getNickname() : null)
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .status(q.getStatus())
                .visibility(q.getVisibility())
                // .answer(null)
                .build();
    }

    // 사용자 검증
    public UserEntity findByUserId(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다. ID: " + userId));
    }

    public UserEntity userStatus(String userId) {
        UserEntity author = findByUserId(userId);
        if (author.getStatus() == null || author.getStatus() == UserStatus.DELETED) {
            throw new RuntimeException("탈퇴한 사용자는 접근 권한이 없습니다.");
        }
        return author;
    }
}