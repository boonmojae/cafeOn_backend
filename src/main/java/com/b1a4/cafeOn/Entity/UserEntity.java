package com.b1a4.cafeOn.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity // DB 구조와 같아야 함
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")   // DB 테이블명
public class UserEntity {
//    ENUM 기본값으로 첫 값이 0으로 설정되어 들어감. 하지만 엔티티 생성할 때 생성자나 setter로 다른 상태로 가입 처리 가능
    public enum Status { ACTIVE, SUSPENDED, DELETED }
    public enum Role { USER, ADMIN }
    public enum Provider { LOCAL, GOOGLE, KAKAO, NAVER }

    @Id
    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    private String userId;

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
    private Status status = Status.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider = Provider.LOCAL;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "preference_keywords", columnDefinition = "JSON")
    private String preferenceKeywords;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Column(name = "penalty_count", nullable = false)
    private int penaltyCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)   // updatable=false : 엔티티를 merge하거나 save할 때 이 필드는 SQL UPDATE 쿼리에 포함되지 않음 (즉, 한 번 저장된 생성일자는 이후 수정할 수 없음)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}