package com.b1a4.cafeOn.user.profile.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class UserPasswordUpdateRequestDTO {
    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    private String oldPassword;

    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    private String newPassword;

    @NotBlank(message = "새 비밀번호 재입력을 입력해주세요.")
    private String confirmPassword;
}
