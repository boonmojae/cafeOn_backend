package com.b1a4.cafeOn.review.entity;

import com.b1a4.cafeOn.cafe.entity.CafeEntity;
import com.b1a4.cafeOn.user.entity.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "rating", nullable = false)
    @Min(1) @Max(5)
    private int rating;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(name = "is_reported", nullable = false)
    private boolean reported = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id", nullable = false)
    private CafeEntity cafe;

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void update(int rating, String content) {
        this.rating = rating;
        this.content = content;
    }

}
