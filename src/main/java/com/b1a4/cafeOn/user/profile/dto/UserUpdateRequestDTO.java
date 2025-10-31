package com.b1a4.cafeOn.user.profile.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequestDTO {

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;
}
