package com.b1a4.cafeOn.user.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.user.dto.EmailRequestDTO;
import com.b1a4.cafeOn.user.dto.RefreshTokenRequest;
import com.b1a4.cafeOn.user.dto.UserDTO;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.enums.UserProvider;
import com.b1a4.cafeOn.user.enums.UserRole;
import com.b1a4.cafeOn.user.enums.UserStatus;
import com.b1a4.cafeOn.config.security.TokenProvider;
import com.b1a4.cafeOn.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

// 회원가입, 로그인, 토큰 갱신만 담당 (모든 사용자 접근 가능 - permitAll)
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증/회원가입/로그인 API")
public class AuthController {
    @Autowired private AuthService authService;
    @Autowired private TokenProvider tokenProvider;
    @Autowired private PasswordEncoder passwordEncoder;


//  1. 회원가입
    @Operation(
            summary = "회원가입",
            description = "이름/닉네임/전화번호/이메일/비밀번호로 회원 생성. 기본 상태 ACTIVE, 역할 USER, 제공자 LOCAL.",
            // ⬇️ 스웨거 RequestBody는 여기(메서드 수준)에 넣기
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UserDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "회원가입 요청 예시",
                                            value = """
                        {
                          "name": "테스트",
                          "nickname": "테스트유저",
                          "phone": "010-1111-1111",
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
                                    implementation = UserDTO.class
                            ),
                            examples = {
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "회원가입 요청 예시",
                                            value = """
                    {
                      "name": "테스트",
                      "nickname": "테스트유저",
                      "phone": "010-1111-1111",
                      "email": "user@example.com",
                      "password": "P@ssw0rd!"
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
            UserDTO responseUserDTO = authService.signUp(userDTO);

            ApiResponse<UserDTO> response = ApiResponse.<UserDTO>builder()
                    .message("회원가입이 완료되었습니다. 이메일 인증을 진행해주세요.")
                    .data(responseUserDTO)
                    .build();

//            201상태코드와 함께 body에 response를 담아 반환
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                    .message("회원가입 실패: "+e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
//  1-1. 이메일 인증
    


//  2. 로그인(JWT 적용)
    @Operation(
            summary = "로그인",
            description = "이메일/비밀번호로 로그인.",
            // ⬇️ 스웨거 RequestBody는 여기(메서드 수준)에 넣기
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UserDTO.class),
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
                                    implementation = UserDTO.class
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
        UserEntity user = authService.getByCredentials( // 사용자 인증하는 메서드
                userDTO.getEmail(), userDTO.getPassword(),passwordEncoder
        );

        if (user != null) { // DB에서 해당 email, password가 일치하는 유저가 있으면,
//            로그인 검사 통과!
//            [after] JWT 적용 후
            final Map<String, String> token = tokenProvider.issueTokens(user);    // JWT Access 토큰 발급

            final UserDTO responseUserDTO = UserDTO.builder()
                    .token(token.get("accessToken"))   // 발급한 JWT Access 토큰 설정
                    .refreshToken(token.get("refreshToken"))    // 발급한 JWT Refresh 토큰 설정
                    .build();

//            DB에 refresh_token 저장(user update)
            user.setRefreshToken(token.get("refreshToken"));
            authService.update(user);   // update는 결국 save() 호출하니까 컬럼 하나만 수정

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


//  3. 토큰 갱신(Refresh Access Token)
//    프론트가 /refresh API 호출 시, 헤더에 Refresh Token 넣어서 보내야함
    @Operation(
            summary = "토큰들 갱신",
            description = "만료된 Access Token 대신, Refresh Token으로 새 Access/Refresh Token을 발급합니다. ",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = {
                                    @ExampleObject(
                                            name = "토큰 갱신 요청 예시",
                                            value = """
                                                    {
                                                        "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
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
                    description = "토큰 재발급 성공",
                    content = @Content(
                            schema = @Schema(implementation = Map.class),
                            examples = {
                                    @ExampleObject(
                                            name = "성공 응답 예시",
                                            value = """
                                {
                                  "message": "토큰 재발급 성공",
                                  "data": {
                                    "accessToken": "new-access-token",
                                    "refreshToken": "new-refresh-token"
                                  }
                                }
                                """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 Refresh Token",
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "에러 응답 예시",
                                            value = "{ \"message\": \"Invalid refresh token\", \"data\": null }"
                                    )
                            }
                    )
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

//        3-1. 서비스 단의 토큰갱신 메서드로 새 토큰<Access, Refresh>들 발급
        Map<String, String> newTokens = authService.refreshTokens(refreshToken);

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .message("토큰 재발급 성공")
                .data(newTokens)
                .build();

        return ResponseEntity.ok(response);
    }


//  4. 로그아웃(refresh token 무효화)
//  DB에 저장된 refresh token을 삭제하거나 블랙리스트로 등록 -> 재발급(refresh) 시도 시 토큰이 유효하지 않아 로그인 상태가 완전히 종료
    @Operation(
            summary = "로그아웃",
            description = """
                    클라이언트가 보유 중인 AccessToken을 Authorization 헤더에 담아 요청하면, 서버는 해당 유저의 RefreshToken을 DB에서 제거(null처리)하여 로그인 세션을 무효화합니다.
                    - **Access Token 형식:** `"Authorization: Bearer <AccessToken>"`
                    - RefreshToken은 DB에서 제거되므로, 이후 `/api/auth/refresh` 요청 시 거부됩니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "성공 응답 예시",
                                            value = """
                                                    {
                                                        "message": "로그아웃 성공",
                                                        "data": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (토큰 누락 또는 형식 오류)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "에러 응답 예시",
                                            value = """
                                                    {
                                                        "message": "잘못된 토큰 형식입니다.",
                                                        "data": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않은 또는 만료된 토큰",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "에러 응답 예시",
                                            value = """
                                                    {
                                                        "message": "유효하지 않은 토큰입니다.",
                                                        "data": null
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @SecurityRequirement(name = "Bearer Authentication")    // ✅ Authorize 버튼과 연동
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @Parameter(hidden = true)   // ✅ Swagger 문서에는 헤더파라미터 중복이므로, 안 뜨게 숨김
            @RequestHeader(name = "Authorization") String authorizationHeader
    ) {
        authService.logout(authorizationHeader);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("로그아웃 성공")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }


//    5. 로그인 상태에서 비밀번호 변경
    @PutMapping("/password")
    @Operation(
            summary = "비밀번호 변경 (로그인 상태)",
            description = """
                    로그인된 사용자가 기존 비밀번호를 검증한 뒤 새 비밀번호로 변경합니다.
                    - Authorization 헤더에 AccessToken을 포함해야 합니다.
                    - 기존 비밀번호가 일치하지 않으면 400 응답을 반환합니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "비밀번호 변경 요청 예시",
                                            value = """
                                                    {
                                                        "oldPassword": "P@ssw0rd!",
                                                        "newPassword": "newP@ss1234!"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 변경 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "성공 응답 예시",
                                            value = """
                                                    {
                                                        "message": "비밀번호가 성공적으로 변경되었습니다.",
                                                        "data": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "현재 비밀번호가 일치하지 않음 또는 요청 형식 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "에러 응답 예시",
                                            value = """
                                                    {
                                                        "message": "현재 비밀번호가 일치하지 않습니다.",
                                                        "data": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 토큰이 없거나 유효하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "에러 응답 예시",
                                            value = """
                                                    {
                                                        "message": "유효하지 않은 토큰입니다.",
                                                        "data": null
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @SecurityRequirement(name="Bearer Authentication") //  Swagger Authorize 버튼과 연동
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> request,
            Authentication authentication
//            Authentication? Spring Security가 현재 로그인된 사용자를 표현하는 객체(인터페이스)
//            이 요청을 보낸 사람이 누구인지(Principal)
//            그 사람이 인증된 상태인지(Authenticated)
//            그 사람의 권한(Role)이 무엇인지(Authorities) 를 모두 담고 있는 객체
//            사용자->JWT토큰 헤더에 담아 요청->JwtAuthenticationFilter{}->SecurityContextHolder->Authentication저장
//            => 저장된 정보는 요청 전역에서 재사용이 가능! 즉, Controller 메서드에서 Authentication 주입 가능
    ) {
        String userId = authentication.getName();    // principal.toString()을 리턴 => 객체 생성 시 설정한 principal(userId)
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        authService.changePassword(userId, oldPassword, newPassword);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("비밀번호가 성공적으로 변경되었습니다.")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }


//    6. 비로그인 상태에서 임시 비밀번호 발급
    @PostMapping("/password/reset")
    @Operation(
            summary = "임시 비밀번호 발급",
            description = "비로그인 상태에서 이메일로 임시 비밀번호를 발급합니다."
    )
    public ResponseEntity<?> resetPassword(@RequestBody EmailRequestDTO request) {
        authService.resetPassword(request.getEmail());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("임시 비밀번호가 이메일로 발송되었습니다.")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }
}