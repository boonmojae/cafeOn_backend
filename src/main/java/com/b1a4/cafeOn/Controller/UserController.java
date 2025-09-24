package com.b1a4.cafeOn.Controller;

// ✅ Spring MVC
import com.b1a4.cafeOn.Security.TokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;  // 스프링 것만 import
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.b1a4.cafeOn.DTO.ApiResponse;
import com.b1a4.cafeOn.DTO.UserDTO;
import com.b1a4.cafeOn.Entity.UserEntity;
import com.b1a4.cafeOn.Enum.UserProvider;
import com.b1a4.cafeOn.Enum.UserRole;
import com.b1a4.cafeOn.Enum.UserStatus;
import com.b1a4.cafeOn.Service.UserService;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;  // java에서 제공하는 UUID 클래스

// ===== Swagger/OpenAPI =====
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증/회원가입 API")
public class UserController {
    @Autowired
    private UserService userService;
    
//    [after] JWT 적용
    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

//    1. 회원가입
    @Operation(
            summary = "회원가입",
            description = "이메일/비밀번호/닉네임으로 회원 생성. 기본 상태 ACTIVE, 역할 USER, 제공자 LOCAL.",
            // ⬇️ 스웨거 RequestBody는 여기(메서드 수준)에 넣기
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = com.b1a4.cafeOn.DTO.UserDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "회원가입 요청 예시",
                                            value = """
                        {
                          "email": "user@example.com",
                          "password": "P@ssw0rd!",
                          "nickname": "테스트유저"
                        }
                        """
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            // ⬇️ 스웨거 @ApiResponse는 FQN로만(팀 규칙)
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "성공 응답 예시",
                                            value = """
                        {
                          "message": "회원가입이 완료되었습니다. 이메일 인증을 진행해주세요.",
                          "data": {
                            "userId": "550e8400-e29b-41d4-a716-446655440000",
                            "email": "user@example.com",
                            "nickname": "고운오렌지"
                          }
                        }
                        """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "에러 응답 예시",
                                            value = "{ \"message\": \"회원가입 실패: 이메일이 이미 존재합니다\", \"data\": null }"
                                    )
                            }
                    )
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(
            // 🔹 스웨거 RequestBody는 FQN(풀패스:경로 전체 작성)로
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    implementation = com.b1a4.cafeOn.DTO.UserDTO.class
                            ),
                            examples = {
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "회원가입 요청 예시",
                                            value = """
                    {
                      "email": "user@example.com",
                      "password": "P@ssw0rd!",
                      "nickname": "테스트유저"
                    }
                    """
                                    )
                            }
                    )
            )
            // 🔹 스프링 RequestBody는 import한 걸로
            @RequestBody UserDTO userDTO)
    {
        try {
//            1-1. userId가 될 UUID 생성
            UUID uuid = UUID.randomUUID();
            System.out.println("생성된 UUID: "+uuid.toString());   // UUID 확인용 출력

//            1-1-2. 비밀번호 암호화
            System.out.println("입력받은 비밀번호: "+userDTO.getPassword());
            String encryptedPassword = passwordEncoder.encode(userDTO.getPassword()); // 암호화된 비밀번호 생성
            System.out.println("암호화된 비밀번호: "+ encryptedPassword);

//            1-2. 요청 본문과 생성한 UUID를 이용해 저장할 사용자 만들기
            UserEntity user = UserEntity.builder()
//                    유저의 입력으로 DTO를 통해 전달받은 값들로 부여
                    .email(userDTO.getEmail())
                    .password(encryptedPassword)    // 1-1-2에서 암호화된 비밀번호
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

//    2. 로그인(JWT 적용)
    @Operation(
            summary = "로그인",
            description = "이메일/비밀번호로 로그인.",
            // ⬇️ 스웨거 RequestBody는 여기(메서드 수준)에 넣기
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = com.b1a4.cafeOn.DTO.UserDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "로그인 요청 예시",
                                            value = """
                            {
                              "email": "user@example.com",
                              "password": "P@ssw0rd!"
                            }
                            """
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            // ⬇️ 스웨거 @ApiResponse는 FQN로만(팀 규칙)
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "성공 응답 예시",
                                            value = """
                            {
                              "message": "로그인 성공",
                              "data": {
                                "token": "JWT-token",
                                "refreshToken": "refresh-token"
                              }
                            }
                            """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "에러 응답 예시",
                                            value = "{ \"message\": \"로그인 실패: 이메일 또는 비밀번호가 일치하지 않습니다.\", \"data\": null }"
                                    )
                            }
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    implementation = com.b1a4.cafeOn.DTO.UserDTO.class
                            ),
                            examples = {
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "로그인 요청 예시",
                                            value = """
                                                    {
                                                        "email": "user@example.com",
                                                        "password": "P@ssw0rd!"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @RequestBody UserDTO userDTO)
    {
        UserEntity user = userService.getByCredentials( // 사용자 인증하는 메서드
                userDTO.getEmail(), userDTO.getPassword(),passwordEncoder
        );

        if (user != null) { // DB에서 해당 email, password가 일치하는 유저가 있으면,
//            로그인 검사 통과!
//            [after] JWT 적용 후
            final Map<String, String> token = tokenProvider.issueTokens(user);    // JWT Access 토큰 발급

            final UserDTO responseUserDTO = UserDTO.builder()
                    .token(token.get("accessToken"))   // 발급한 JWT Access 토큰
                    .refreshToken(token.get("refreshToken"))    // 발급한 JWT Refresh 토큰
                    .build();

            ApiResponse<UserDTO> response = ApiResponse.<UserDTO>builder()
                    .message("로그인 성공")
                    .data(responseUserDTO)
                    .build();

//            System.out.println("[UserController.login()] response: "+response);   // 응답값 확인용 출력

            return ResponseEntity.ok().body(response);
        } else {
//            로그인 검사 실패! (해당 유저가 존재하지 않았으므로)
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .message("로그인 실패")
                    .build();

            return ResponseEntity.badRequest().body(response);
        }
    }
}