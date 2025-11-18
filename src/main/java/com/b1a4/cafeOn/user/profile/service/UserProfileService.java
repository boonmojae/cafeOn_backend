package com.b1a4.cafeOn.user.profile.service;

import com.b1a4.cafeOn.common.exception.ClientErrorException;
import com.b1a4.cafeOn.image.enums.ImageCategory;
import com.b1a4.cafeOn.image.service.S3Service;
import com.b1a4.cafeOn.image.service.UploadedImageInfo;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.enums.UserStatus;
import com.b1a4.cafeOn.user.profile.dto.*;
import com.b1a4.cafeOn.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserProfileService {

    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserProfileResponseDTO getMyProfile(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        return UserProfileResponseDTO.of(
                user.getNickname(),
                user.getName(),
                user.getEmail(),
                user.getProfileImageUrl()
        );
    }

    public UserUpdateResponseDTO updateProfile(String userId, UserUpdateRequestDTO dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        user.setNickname(dto.getNickname());
        userRepository.save(user);
        return UserUpdateResponseDTO.success();
    }

    public UserProfileImageResponseDTO updateProfileImage(String userId, UserProfileImageRequestDTO dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        UploadedImageInfo uploaded = s3Service.uploadImage(dto.getFile(), ImageCategory.PROFILE);

        user.setProfileImageUrl(uploaded.getPublicUrl());
        userRepository.save(user);

        return UserProfileImageResponseDTO.of(uploaded.getPublicUrl());
    }

    @Transactional
    public void changePassword(String userId, UserPasswordUpdateRequestDTO dto) {
        // 새 비밀번호 재입력 불일치
        if (!Objects.equals(dto.getNewPassword(), dto.getConfirmPassword())) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "새 비밀번호와 재입력이 일치하지 않습니다.");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 현재 비밀번호 불일치
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "현재 비밀번호가 올바르지 않습니다.");
        }

        validatePasswordPolicy(dto.getNewPassword());

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    private void validatePasswordPolicy(String pw) {
        if (pw == null || pw.length() < 8) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "비밀번호는 8자 이상이어야 합니다.");
        }
    }

    // 회원 탈퇴
    @Transactional
    public void deleteMyAccount(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (user.getDeletedAt() != null || user.getStatus() == UserStatus.DELETED) {
            throw new ClientErrorException(HttpStatus.CONFLICT, "이미 탈퇴한 사용자입니다.");
        }

        user.softDelete();

        userRepository.save(user);
    }
}
