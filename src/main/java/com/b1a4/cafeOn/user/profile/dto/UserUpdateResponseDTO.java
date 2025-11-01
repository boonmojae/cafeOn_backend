package com.b1a4.cafeOn.user.profile.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateResponseDTO {
    private String message;

    public static UserUpdateResponseDTO success() {
        return UserUpdateResponseDTO.builder()
                .message("회원정보가 수정되었습니다.")
                .build();
    }
}