package com.b1a4.cafeOn.controllers;

// ✅ Spring MVC
import com.b1a4.cafeOn.entity.UserEntity;
import com.b1a4.cafeOn.services.UserService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

// ===== Swagger/OpenAPI =====
import io.swagger.v3.oas.annotations.tags.Tag;

// 로그인된 사용자 전용 API
// 접근 가능 권한: USER, ADMIN
// 예: 프로필 조회, 북마크 관리, 카페 찜하기, 마이페이지 등
@Slf4j
@RestController
@RequestMapping("/api/user")
@Tag(name = "User", description = "일반 사용자 전용 API")
public class UserController {
    @Autowired
    private UserService userService;
    
//    @GetMapping("/profile")
//    public ResponseEntity<?> getProfile(Authentication authentication) {
//        String userid = (String) authentication.getPrincipal(); // JwtAuthenticationFilter에서 넣어준 userId
//        UserEntity user = userService.getById(userId);
//        return ResponseEntity.ok().body(user);
//    }
    
//    예: 카페 찜하기, 내 북마크 보기 같은 API 들이 여기에
}