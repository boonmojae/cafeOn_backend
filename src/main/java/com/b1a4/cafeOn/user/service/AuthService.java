package com.b1a4.cafeOn.user.service;

import com.b1a4.cafeOn.config.security.TokenProvider;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

@Slf4j
@Service
// /api/user/**
// 내정보 조회/수정/탈퇴, 위시리스트 관리, 마이페이지 관련 API (reviews, bookmarks, posts, comments, questions 등)
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenProvider tokenProvider;

    //    1. 회원가입
    public UserEntity create(final UserEntity userEntity) {
//        1-1. 유효성 검사: userEntity 혹은 email이 null인 경우 예외 던짐
        if (userEntity == null || userEntity.getEmail() == null) {
            throw new RuntimeException("UserEntity 혹은 email이 null임");
        }
        final String email = userEntity.getEmail();

//        1-2. 유효성 검사: 이메일이 이미 존재하는 경우 예외를 던짐 (email필드는 unique해야 하므로)
        if (userRepository.existsByEmail(email)) {
            log.warn("Email already exists {}", email);
            throw new RuntimeException("이메일이 이미 존재함");
        }

        return userRepository.save(userEntity); // UserEntity를 DB에 저장
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
}