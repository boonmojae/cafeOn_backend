package com.b1a4.cafeOn.user.profile.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ✅ 회원 정보 조회 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDTO {

    private String nickname;
    private String name;
    private String email;
    private String profileImageUrl; // 프로필 이미지 URL

    public static UserProfileResponseDTO of(String nickname, String name, String email, String profileImageUrl) {
        return UserProfileResponseDTO.builder()
                .nickname(nickname)
                .name(name)
                .email(email)
                .profileImageUrl(profileImageUrl)
                .build();
    }
}
