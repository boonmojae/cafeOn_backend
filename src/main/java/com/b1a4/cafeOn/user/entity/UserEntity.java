package com.b1a4.cafeOn.user.entity;

import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.user.enums.UserProvider;
import com.b1a4.cafeOn.user.enums.UserRole;
import com.b1a4.cafeOn.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity // DB 구조와 같아야 함
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")   // DB 테이블명

@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP, status = 'DELETED' WHERE user_id = ?")
@Where(clause = "deleted_at IS NULL")
public class UserEntity {
//    ENUM 기본값으로 첫 값이 0으로 설정되어 들어감. 하지만 엔티티 생성할 때 생성자나 setter로 다른 상태로 가입 처리 가능

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

    @Column(name = "created_at", nullable = false, updatable = false)   // updatable=false : 엔티티를 merge하거나 save할 때 이 필드는 SQL UPDATE 쿼리에 포함되지 않음 (즉, 한 번 저장된 생성일자는 이후 수정할 수 없음)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @PrePersist // Persist(INSERT) 하기 전에 실행됨
    // save() 할 때, 처음DB에 들어가기 직전 호출되어, 자동으로 createdAt/updatedAt이 들어감
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();   // 처음 생성할 때는 created=updated 같게
    }

    @PreUpdate  // Update(UPDATE) 되기 전에 실행됨
    // 기존 엔티티가 수정될 때 호출됨, 주로 updatedAt 값 갱신 시 사용
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