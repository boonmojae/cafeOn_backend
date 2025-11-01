package com.b1a4.cafeOn.user.profile.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

// 프로필 이미지 변경 요청 DTO

@Getter
@Setter
public class UserProfileImageRequestDTO {
    private MultipartFile file;
}
