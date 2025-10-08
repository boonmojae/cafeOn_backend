package com.b1a4.cafeOn.community.post.entity;

import com.b1a4.cafeOn.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_likes",uniqueConstraints = @UniqueConstraint(name = "uk_post_like_post_user", columnNames = {"post_id", "user_id"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_like_id")
    private Long postLikeId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private PostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    public static PostLikeEntity of(PostEntity post, UserEntity user) {
        return PostLikeEntity.builder()
                .post(post)
                .user(user)
                .build();
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


}
