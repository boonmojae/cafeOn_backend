package com.b1a4.cafeOn.Controller;

import com.b1a4.cafeOn.DTO.UserDTO;
import com.b1a4.cafeOn.Entity.UserEntity;
import com.b1a4.cafeOn.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class UserController {
    @Autowired
    private UserService userService;

//    1. 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody UserDTO userDTO) {
        try {
//            요청 본문을 이용해 저장할 사용자 만들기
            UserEntity user = UserEntity.builder()
                    .userId(UUID)   // todo: UUID 생성 로직 필요
                    .email(userDTO.getEmail())
                    .password(userDTO.getPassword())    // todo: 패스워드 암호화 추가 필요
                    .nickname(userDTO.getNickname())
                    .profileImage(userDTO.getProfileImage())
                    .status(userDTO.getStatus())
                    .role(userDTO.getRole())
                    .provider(userDTO.getProvider())
                    .preferenceKeywords(userDTO.getPreferenceKeywords())                    
                    .build();

//            서비스계층 메서드를 이용해 repo에 사용자 저장
            UserEntity registeredUser = userService.create(user);
            UserDTO responseUserDTO = UserDTO.builder()
                    .email()
                    .build();
            
        } catch (Exception e) {
            
        }
    }
}
