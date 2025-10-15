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
    public Page<QuestionListResponseDTO> getQnaList(@Nullable String userId,
                                                    @Nullable String keyword,
                                                    Pageable pageable) {
        final boolean hasKeyword = (keyword != null && !keyword.isBlank());

        Page<QuestionEntity> page = hasKeyword
                // 검색은 PUBLIC만
                ? questionRepository.findByVisibilityAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                QuestionVisibility.PUBLIC, keyword, pageable)
                // 전체 목록은 공개+비공개 모두
                : questionRepository.findAll(pageable);

        // 비공개는 "비공개 문의" 마스킹 / 내 비공개는 원제목
        // 로그인한 사용자 기준으로 본인 여부 판단
        return page.map(q -> QuestionListResponseDTO.fromUser(q, userId));
    }

    // [관리자] 전체 문의 목록 조회 (status 선택 필터)
    public Page<QuestionListResponseDTO> getAdminList(Pageable pageable,
                                                      @Nullable QuestionStatus status,
                                                      @Nullable String keyword) {
        Page<QuestionEntity> page;
        if (keyword != null && !keyword.isBlank()) {
            page = questionRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(keyword, pageable);
        } else if (status != null) {
            page = questionRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            page = questionRepository.findAll(pageable);
        }
        return page.map(QuestionListResponseDTO::fromAdmin);
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
    // 내가 작성한 문의 조회
    @Transactional(readOnly = true)
    public Page<QuestionListResponseDTO> getMyQuestions(String userId,
                                                        Pageable pageable,
                                                        @Nullable String keyword) {
        Page<QuestionEntity> page = (keyword != null && !keyword.isBlank())
                ? questionRepository.findByUserUserIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                userId, keyword, pageable)
                : questionRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable);

        return page.map(QuestionListResponseDTO::fromMy);
    }

    // 내가 작성한 문의 상세
    @Transactional(readOnly = true)
    public QuestionDetailResponseDTO getMyQuestion(String userId, Long id) {
        userStatus(userId);

        //
        QuestionEntity q = questionRepository
                .findByQuestionIdAndUserUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("문의가 존재하지 않습니다. ID=" + id));

        return toDetailDTO(q);
    }

    // 문의 수정
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

    // 문의 삭제
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