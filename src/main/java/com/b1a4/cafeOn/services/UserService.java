package com.b1a4.cafeOn.services;

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

//    2. 로그인(암호화된 비밀번호 검증)
    public UserEntity getByCredentials(final String email,
                                       final String password,
                                       final PasswordEncoder encoder) {        
//        [after] 비밀번호 암호화(passwordEncoder.encode(userDTO.getPassword()) 적용 후
        final UserEntity originalUser = userRepository.findByEmail(email);  // 이메일이 일치하는 유저 하나를 찾음
        
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
}