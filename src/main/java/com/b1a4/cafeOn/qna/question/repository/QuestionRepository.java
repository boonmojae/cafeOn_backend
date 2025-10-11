package com.b1a4.cafeOn.qna.question.repository;

import com.b1a4.cafeOn.qna.question.entity.QuestionEntity;
import com.b1a4.cafeOn.qna.question.enums.QuestionStatus;
import com.b1a4.cafeOn.qna.question.enums.QuestionVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {

    // 문의 목록 조회
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

    // 검색
    // 공개 리스트 기본: PUBLIC만
    @EntityGraph(attributePaths = {"user"})
    Page<QuestionEntity> findByVisibilityOrderByCreatedAtDesc(
            QuestionVisibility visibility, Pageable pageable);

    // 문의페이지 공개 리스트 제목 검색: PUBLIC + title
    @EntityGraph(attributePaths = {"user"})
    Page<QuestionEntity> findByVisibilityAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
            QuestionVisibility visibility, String title, Pageable pageable);

    // 마이페이지 제목 검색: 본인 글 + title
    @EntityGraph(attributePaths = {"user"})
    Page<QuestionEntity> findByUserUserIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
            String userId, String title, Pageable pageable);

    // 관리자 제목 검색: 전체 + title
    @EntityGraph(attributePaths = {"user"})
    Page<QuestionEntity> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(
            String title, Pageable pageable);

    //  문의 페이지
    // public + 로그인 한 유저의 private 문의
    @EntityGraph(attributePaths = {"user"})
    @Query("""
  SELECT q FROM QuestionEntity q
  WHERE (q.visibility = com.b1a4.cafeOn.qna.question.enums.QuestionVisibility.PUBLIC
         OR q.user.userId = :userId)
  ORDER BY q.createdAt DESC
""")
    Page<QuestionEntity> findPublicOrMyPrivateOrderByCreatedAtDesc(
            @Param("userId") String userId, Pageable pageable);

    // 제목 검색
    @EntityGraph(attributePaths = {"user"})
    @Query("""
  SELECT q FROM QuestionEntity q
  WHERE (q.visibility = com.b1a4.cafeOn.qna.question.enums.QuestionVisibility.PUBLIC
         OR q.user.userId = :userId)
    AND LOWER(q.title) LIKE LOWER(CONCAT('%', :title, '%'))
  ORDER BY q.createdAt DESC
""")
    Page<QuestionEntity> searchPublicOrMyPrivateByTitle(
            @Param("userId") String userId,
            @Param("title") String title,
            Pageable pageable);

    // QuestionVisibility.PUBLIC 간편 호출용
    default Page<QuestionEntity> findPublicOnlyOrderByCreatedAtDesc(Pageable pageable) {
        return findByVisibilityOrderByCreatedAtDesc(QuestionVisibility.PUBLIC, pageable);
    }
    default Page<QuestionEntity> searchPublicByTitle(String title, Pageable pageable) {
        return findByVisibilityAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(QuestionVisibility.PUBLIC, title, pageable);
    }
}


