package com.b1a4.cafeOn.user.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 프로필 이미지 변경 응답 DTO
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileImageResponseDTO {
    private String profileImageUrl;
    private String message;

    public static UserProfileImageResponseDTO of(String url) {
        return UserProfileImageResponseDTO.builder()
                .profileImageUrl(url)
                .message("프로필 이미지가 변경되었습니다.")
                .build();
    }
}
