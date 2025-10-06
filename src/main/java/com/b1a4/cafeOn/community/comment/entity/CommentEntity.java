package com.b1a4.cafeOn.community.comment.entity;

import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CommentEntity parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<CommentEntity> children = new ArrayList<>();

    @Column(name = "content")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private PostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<CommentLikeEntity> likes = new ArrayList<>();

    public void update(String content) {
        this.content = content;
    }


    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
