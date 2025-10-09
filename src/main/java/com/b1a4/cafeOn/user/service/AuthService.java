package com.b1a4.cafeOn.user.service;

import com.b1a4.cafeOn.config.security.TokenProvider;
import com.b1a4.cafeOn.user.dto.UserDTO;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.enums.UserProvider;
import com.b1a4.cafeOn.user.enums.UserRole;
import com.b1a4.cafeOn.user.enums.UserStatus;
import com.b1a4.cafeOn.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
// /api/user/**
// 내정보 조회/수정/탈퇴, 위시리스트 관리, 마이페이지 관련 API (reviews, bookmarks, posts, comments, questions 등)
public class AuthService {
    @Autowired private UserRepository userRepository;
    @Autowired private TokenProvider tokenProvider;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;

    //    1. 회원가입
    public UserDTO signUp(UserDTO userDTO) {
//        1-1. 유효성 검사: userEntity 혹은 email이 null인 경우 예외 던짐
        if (userDTO == null || userDTO.getEmail() == null) {
            throw new RuntimeException("UserEntity 혹은 email이 null임");
        }

//        1-1-1. 이메일 중복 검사
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("이미 등록된 이메일입니다.");
        }

//        1-2. userId가 될 UUID 생성
        String userId = UUID.randomUUID().toString();
        System.out.println("생성된 UUID: " + userId);   // UUID 확인용 출력

//        1-3. 비밀번호 암호화
        System.out.println("입력받은 비밀번호: " + userDTO.getPassword());
        String encodedPassword = passwordEncoder.encode(userDTO.getPassword()); // 암호화된 비밀번호 생성
        System.out.println("암호화된 비밀번호: " + encodedPassword);

//        1-4. 요청 본문과 생성한 UUID를 이용해 저장할 사용자 만들기
        UserEntity user = UserEntity.builder()
//                유저의 입력으로 DTO를 통해 전달받은 값들로 부여
                .name(userDTO.getName())
                .nickname(userDTO.getNickname())
                .phone(userDTO.getPhone())
                .email(userDTO.getEmail())
                .password(encodedPassword)  // 1-3에서 암호화된 비밀번호
//                여기부턴 서버에서 자동으로 처리해야 할 값들로 부여
                .userId(userId)
                .status(UserStatus.ACTIVE)  // 기본 ACTIVE
                .role(UserRole.USER)    // 기본 USER
                .provider(UserProvider.LOCAL)   // 기본 LOCAL
//                .profileImage(userDTO.getProfileImage())
//                .preferenceKeywords(userDTO.getPreferenceKeywords())
                .build();

//        1-5. 서비스계층 메서드를 이용해 repo에 사용자 저장
        UserEntity registeredUser = userRepository.save(user);

        return UserDTO.builder()
                .userId(registeredUser.getUserId())
                .email(registeredUser.getEmail())
                .nickname(registeredUser.getNickname())
                .build();
    }


    //    2. 로그인(암호화된 비밀번호 검증)
    public UserEntity getByCredentials(final String email,
                                       final String password,
                                       final PasswordEncoder encoder) {
//        [after] 비밀번호 암호화(passwordEncoder.encode(userDTO.getPassword()) 적용 후
        final UserEntity originalUser = userRepository.findByEmail(email).orElse(null);  // 이메일이 일치하는 유저 하나를 찾음

        if (originalUser != null && encoder.matches(password, originalUser.getPassword())) {
//            password: 클라이언트가 주장하는 현재 유저에 대한 비밀번호
//            originalUser.getPassword(): DB에 저장된 정답 (암호화된) 비밀번호
//            이메일로 찾은 유저가 존재하고,
//            매개변수로 받은 password가 유저의 password와 일치하면(encoder.matches()로 알아서 복호화해서 일치하나 확인)
            System.out.println("[UserService.getByCredentails()] 유저가 입력한 비밀번호와 DB의 정답비밀번호가 일치합니다.");
            return originalUser;    // 찾은 유저 반환
        }

        return null;    // 이메일이 일치하는 유저가 없다면, 로그인 실패니까 null 반환
    }


    //    3. 토큰들 갱신(refresh Access Token)
    public Map<String, String> refreshTokens(String refreshToken) {
//        3-1. 토큰 앞뒤 공백/접두어 제거
        String cleanedToken = refreshToken == null ? "" : refreshToken.trim();
        if (cleanedToken.toLowerCase().startsWith("bearer ")) {
            cleanedToken = cleanedToken.substring(7).trim();    // "Bearer " 제거
        }

//        3-2. cleanedToken 유효성 검사 & userId 추출
        Map<String, String> claims = tokenProvider.validateAndExtractClaims(cleanedToken, "refresh");
        if (claims == null) {
            log.warn("RefreshToken 검증 실패: {}", cleanedToken);
            throw new IllegalArgumentException("Invalid Refresh Token");
        }

        String userId = claims.get("userId");
        if (userId == null) {
            log.warn("RefreshToken에서 userId 추출 실패");
            throw new IllegalArgumentException("Invalid Refresh Token");
        }

//        3-3. DB에서 해당 유저 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

//        3-4. DB에 저장된 refreshToken과 비교
        String stored = user.getRefreshToken();
        if (stored == null || stored.isBlank() || !cleanedToken.equals(stored.trim())) {
            log.warn("RefreshToken mismatch (요청값: {}, DB값: {})", cleanedToken, stored);
            throw new IllegalArgumentException("Refresh Token mismatch");
        }

//        3-5. 새로운 Access Token 발급
        String newAccessToken = tokenProvider.issueAccessToken(user);

//        3-6. Refresh Token 회전 전략 (새로 발급 & DB 저장)
        String newRefreshToken = tokenProvider.issueRefreshToken(user);
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        log.info("토큰 재발급 성공 - userId: {}, newAccess: {}, newRefresh: {}", userId, newAccessToken, newRefreshToken);

        return Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken
        );
    }


    //    4. 사용자 정보 수정
    public UserEntity update(final UserEntity user) {
        return userRepository.save(user);
    }


    //    5. Refresh Token 으로 유저 조회
    public UserEntity getByRefreshToken(final String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken).orElse(null);
    }


//    6. 로그아웃
    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("잘못된 토큰 형식입니다.");
        }

        String token = authorizationHeader.substring(7);

        Map<String, String> claims = tokenProvider.validateAndExtractClaims(token, "access");
        if (claims == null) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        String userId = claims.get("userId");

//        RefreshToken 제거 (!! Update쿼리는 Service레벨에서 엔티티 조작으로 (find -> set -> save))
        userRepository.findById(userId).ifPresent(user -> {
            user.setRefreshToken(null);
            userRepository.save(user);
        });
    }


//    7. 비밀번호 변경
    public void changePassword(String userId, String oldPassword, String newPassword) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

//        기존 비밀번호 일치 확인
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

//        새 비밀번호로 변경
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);
        userRepository.save(user);

        log.info("비밀번호 변경 완료 - email: {}, NewPassword(plain): {}", user.getEmail(), newPassword);
    }


//    8. 임시 비밀번호 발급
    public void resetPassword(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 사용자가 없습니다."));

//        8-1. 임시 비밀번호 생성
        String tempPassword = generateTempPassword();

//        8-2. 임시 비밀번호 암호화 후 저장
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

//        8-3. 이메일 전송
        try {
            emailService.sendTempPasswordEmail(user.getEmail(), tempPassword);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("임시 비밀번호 발송 중 오류가 발생했습니다.", e);
        }
    }

    private String generateTempPassword() {
        int length = 10;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i=0; i<length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}