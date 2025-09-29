package com.b1a4.cafeOn.controllers;

import com.b1a4.cafeOn.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 관리자 전용 API
// 접근 가능 권한: ADMIN
// 예: 모든 회원 조회, 특정 유저 차단, 통계 조회, 신고처리 해결, QNA답변 등
@Slf4j
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "관리자 전용 API")
public class AdminController {
    @Autowired
    private UserService userService;
    
//    관리자가 하는 API들이 여기에
}
