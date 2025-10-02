package com.b1a4.cafeOn.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO : 필요한 필드만 포함하거나, 가공된 데이터(token, fullName, likeCount 등)를 추가 가능
// API 요청/응답 맞춤용
// nullable 컬럼은 아예 뺄 수도 있음
// 불필요한 민감 정보(예:password, refreshToken)는 아예 제외하기도 함
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String token;           // jwt 저장 공간 (DB에는 없음, DTO에서만 필요)

    private String userId;              // CHAR(36)
    private String email;               // VARCHAR(255)
    private String password;            // VARCHAR(255)
    private String nickname;            // VARCHAR(50)
//    private String profileImage;        // JSON -> String (필요시 DTO/Map 으로 변환 가능)
//    private String status;              // ENUM('ACTIVE', 'SUSPENDED', 'DELETED')
//    private String role;                // ENUM('USER', 'ADMIN')
//    private String provider;            // ENUM('LOCAL', 'GOOGLE', 'KAKAO', 'NAVER')
//    private String providerId;          // VARCHAR(255)
    private String preferenceKeywords;  // JSON -> String (필요시 List<String>으로 변환)
    private String refreshToken;        // VARCHAR(512)
//    private int penaltyCount;           // INT
//    private LocalDateTime createdAt;    // TIMESTAMP
//    private LocalDateTime updatedAt;    // TIMESTAMP
//    private LocalDateTime deletedAt;    // TIMESTAMP
}