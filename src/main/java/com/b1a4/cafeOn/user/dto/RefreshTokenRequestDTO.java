package com.b1a4.cafeOn.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Refresh Token 갱신 요청 DTO")
public class RefreshTokenRequestDTO {
    @Schema(description = "발급받은 Refresh Token", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String refreshToken;
}