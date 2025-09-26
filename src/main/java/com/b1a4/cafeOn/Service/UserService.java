package com.b1a4.cafeOn.Service;

import com.b1a4.cafeOn.entity.UserEntity;
import com.b1a4.cafeOn.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

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

//    2. 로그인(인증)
    public UserEntity getByCredentials(final String email,
                                       final String password) {
//                                       final PasswordEncoder encoder) {
//        DB에서 해당 email, password가 일치하는 유저가 있는지를 조회
        return userRepository.findByEmailAndPassword(email, password);  // 있으면 UserEntity, 없으면 null 반환
    }
}