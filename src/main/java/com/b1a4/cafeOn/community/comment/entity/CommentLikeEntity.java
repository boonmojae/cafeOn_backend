package com.b1a4.cafeOn.community.comment.entity;

import com.b1a4.cafeOn.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment_likes", uniqueConstraints = @UniqueConstraint(name = "uk_comment_like_comment_user", columnNames = {"comment_id", "user_id"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_like_id")
    private Long commentLikeId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private CommentEntity comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    public static CommentLikeEntity of(CommentEntity comment, UserEntity user) {
        return CommentLikeEntity.builder()
                .comment(comment)
                .user(user)
                .build();
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
