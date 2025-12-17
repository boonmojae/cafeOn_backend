package com.b1a4.cafeOn.user.entity;

import com.b1a4.cafeOn.user.enums.UserProvider;
import com.b1a4.cafeOn.user.enums.UserRole;
import com.b1a4.cafeOn.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")

@SQLRestriction("deleted_at IS NULL")
public class UserEntity {

    @Id
    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    private String userId;

    @Column(name = "name", columnDefinition = "CHAR(50)")
    private String name;

    @Column(name = "phone", columnDefinition = "CHAR(15)")
    private String phone;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(name = "profile_image", columnDefinition = "JSON")
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserProvider provider = UserProvider.LOCAL;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "preference_keywords", columnDefinition = "JSON")
    private String preferenceKeywords;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Column(name = "penalty_count", nullable = false)
    @Builder.Default
    private int penaltyCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.status = UserStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
        this.refreshToken = null; // 재로그인 방지 (선택)
        this.nickname = "탈퇴회원";
        this.profileImage = null;
        this.profileImageUrl = null;
    }

    // 탈퇴 여부 확인
    public boolean isDeleted() {
        return this.deletedAt != null || this.status == UserStatus.DELETED;
    }

}