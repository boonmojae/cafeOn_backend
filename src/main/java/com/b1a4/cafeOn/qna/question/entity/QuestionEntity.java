package com.b1a4.cafeOn.qna.question.entity;

import com.b1a4.cafeOn.qna.question.enums.QuestionVisibility;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.qna.question.enums.QuestionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "questions")
public class QuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id", nullable = false)
    private Long questionId;

//    @Column(name = "user_id", nullable = false)
//    private int userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionVisibility visibility;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 엔티티 저장 시 자동 실행
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = QuestionStatus.PENDING;
        if (this.visibility == null) this.visibility = QuestionVisibility.PRIVATE;
    }

    // 엔티티 수정 시 자동 실행
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 수정 로직 (제목, 내용, 공개 여부)
    public void update(String title, String content, QuestionVisibility visibility) {
        this.title = title;
        this.content = content;
        this.visibility = visibility;
    }
}
