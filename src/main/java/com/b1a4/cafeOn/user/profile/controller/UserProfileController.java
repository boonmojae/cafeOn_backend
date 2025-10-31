package com.b1a4.cafeOn.user.profile.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.user.profile.dto.*;
import com.b1a4.cafeOn.user.profile.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "회원 정보 및 프로필 관리 API")
public class UserProfileController {

    private final UserProfileService userProfileService;

    // 회원 정보 조회
    @GetMapping
    @Operation(summary = "회원 정보 조회", description = "로그인한 사용자의 프로필 정보를 조회합니다.")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal String userId) {
        var profile = userProfileService.getMyProfile(userId);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("회원 정보 조회 완료")
                .data(profile)
                .build());
    }

    // 회원 정보 수정 (닉네임)
    @PutMapping
    @Operation(summary = "회원 정보 수정", description = "닉네임을 변경합니다.")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal String userId,
            @RequestBody UserUpdateRequestDTO dto) {
        var updated = userProfileService.updateProfile(userId, dto);
        return ResponseEntity.ok(ApiResponse.builder()
                .message(updated.getMessage())
                .build());
    }

    // 프로필 이미지 변경
    @PutMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 이미지 변경", description = "단일 이미지 파일을 업로드하여 프로필 이미지를 변경합니다.")
    public ResponseEntity<?> updateProfileImage(
            @AuthenticationPrincipal String userId,
            @ModelAttribute UserProfileImageRequestDTO dto) {
        var result = userProfileService.updateProfileImage(userId, dto);
        return ResponseEntity.ok(ApiResponse.builder()
                .message(result.getMessage())
                .data(result)
                .build());
    }

    // 비밀번호 재설정, 재입력
    @PutMapping(value = "/password", consumes = MediaType.APPLICATION_JSON_VALUE) // ← 상대 경로로 수정
    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호 확인 후 새 비밀번호로 변경합니다.")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody UserPasswordUpdateRequestDTO dto) {

        userProfileService.changePassword(userId, dto);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("비밀번호가 변경되었습니다.")
                .build());
    }


}
