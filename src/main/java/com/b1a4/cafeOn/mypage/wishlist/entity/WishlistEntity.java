//package com.b1a4.cafeOn.mypage.wishlist.entity;
//
//import com.b1a4.cafeOn.user.entity.UserEntity;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@Entity
//@Table(
//        name = "wishlists",
//        uniqueConstraints = {
//                @UniqueConstraint(name = "uk_wishlists_user_cafe", columnNames = {"user_id", "cafe_id"})
//        },
//        indexes = {
//                @Index(name = "idx_wishlists_user_id", columnList = "user_id"),
//                @Index(name = "idx_wishlists_cafe_id", columnList = "cafe_id")
//        }
//)
//public class WishlistEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "wishlist_id", nullable = false)
//    private Long wishlistId; // BIGINT UNSIGNED → Java에서는 Long
//
//    // FK: users.user_id
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private UserEntity user;
//
//    // FK: cafes.cafe_id (현재 CafeEntity가 없으므로 값만 보관)
//    @Column(name = "cafe_id", nullable = false)
//    private Long cafeId;
//
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @PrePersist
//    private void onCreate() {
//        if (createdAt == null) createdAt = LocalDateTime.now();
//    }
//}
