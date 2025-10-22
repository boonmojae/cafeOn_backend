package com.b1a4.cafeOn.mypage.wishlist.entity;

import com.b1a4.cafeOn.cafe.entity.CafeEntity;
import com.b1a4.cafeOn.mypage.wishlist.enums.WishlistCategory;
import com.b1a4.cafeOn.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "wishlists",
        // 한 유저가 같은 카페를 여러 카테고리에 저장할 수 있지만 같은 카테고리에 중복 저장은 불가능
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_wishlists_user_cafe_category", columnNames = {"user_id", "cafe_id", "category"})
        },
        indexes = {
                @Index(name = "idx_wishlists_user_id", columnList = "user_id"),
                @Index(name = "idx_wishlists_cafe_id", columnList = "cafe_id")
        }
)
public class WishlistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id", nullable = false)
    private Long wishlistId;  // PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id", nullable = false)
    private CafeEntity cafe;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private WishlistCategory category;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
