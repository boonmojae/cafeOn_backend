package com.b1a4.cafeOn.user.profile.service;

import com.b1a4.cafeOn.image.enums.ImageCategory;
import com.b1a4.cafeOn.image.service.S3Service;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.profile.dto.*;
import com.b1a4.cafeOn.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        return UserProfileResponseDTO.of(
                user.getNickname(),
                user.getName(),
                user.getEmail(),
                user.getProfileImageUrl()
        );
    }

    public UserUpdateResponseDTO updateProfile(String userId, UserUpdateRequestDTO dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        user.setNickname(dto.getNickname());
        userRepository.save(user);
        return UserUpdateResponseDTO.success();
    }

    public UserProfileImageResponseDTO updateProfileImage(String userId, UserProfileImageRequestDTO dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        S3Service.UploadedImageInfo uploaded = s3Service.uploadImage(dto.getFile(), ImageCategory.PROFILE);

        user.setProfileImageUrl(uploaded.getPublicUrl());
        userRepository.save(user);

        return UserProfileImageResponseDTO.of(uploaded.getPublicUrl());
    }

    @Transactional
    public void changePassword(String userId, UserPasswordUpdateRequestDTO dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("새 비밀번호와 재입력이 일치하지 않습니다.");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new AccessDeniedException("현재 비밀번호가 올바르지 않습니다.");
        }

        validatePasswordPolicy(dto.getNewPassword());

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    private void validatePasswordPolicy(String pw) {
        if (pw.length() < 8) throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
    }

}
