package com.b1a4.cafeOn.Controller;

import com.b1a4.cafeOn.DTO.ApiResponse;
import com.b1a4.cafeOn.DTO.UserDTO;
import com.b1a4.cafeOn.Entity.UserEntity;
import com.b1a4.cafeOn.Enum.UserProvider;
import com.b1a4.cafeOn.Enum.UserRole;
import com.b1a4.cafeOn.Enum.UserStatus;
import com.b1a4.cafeOn.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;  // java에서 제공하는 UUID 클래스

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
//            1-1. userId가 될 UUID 생성
            UUID uuid = UUID.randomUUID();
            System.out.println("생성된 UUID: "+uuid.toString());   // UUID 확인용 출력

//            1-2. 요청 본문과 생성한 UUID를 이용해 저장할 사용자 만들기
            UserEntity user = UserEntity.builder()
//                    유저의 입력으로 DTO를 통해 전달받은 값들로 부여
                    .email(userDTO.getEmail())
                    .password(userDTO.getPassword())    // todo: 패스워드 암호화 추가 필요
                    .nickname(userDTO.getNickname())
//                    여기부턴 서버에서 자동으로 처리해야 할 값들로 부여
                    .userId(uuid.toString())    // 위에서 생성한 uuid값
                    .status(UserStatus.ACTIVE)   // 기본 ACTIVE
                    .role(UserRole.USER) // 기본 USER
                    .provider(UserProvider.LOCAL)    // 기본 LOCAL
//                    .profileImage(userDTO.getProfileImage())
//                    .preferenceKeywords(userDTO.getPreferenceKeywords())
                    .build();

//            1-2. 서비스계층 메서드를 이용해 repo에 사용자 저장
            UserEntity registeredUser = userService.create(user);

//            1-3. 사용자 생성 완료 후, 프론트로 보낼 응답DTO들 세팅
            UserDTO responseUserDTO = UserDTO.builder()
                    .userId(registeredUser.getUserId())
                    .email(registeredUser.getEmail())
                    .nickname(registeredUser.getNickname())
                    .build();

            ApiResponse<UserDTO> response = ApiResponse.<UserDTO>builder()
                    .message("회원가입이 완료되었습니다. 이메일 인증을 진행해주세요.")
                    .data(responseUserDTO)
                    .build();

//            1-4. 201상태코드와 함께 body에 response를 담아 반환
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                    .message("회원가입 실패: "+e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}