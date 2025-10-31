package com.b1a4.cafeOn.admin.user.controller;

import com.b1a4.cafeOn.admin.user.dto.AdminUserDetailDTO;
import com.b1a4.cafeOn.admin.user.dto.AdminUserListItemDTO;
import com.b1a4.cafeOn.admin.user.service.AdminUserService;
import com.b1a4.cafeOn.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/admin/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User (Admin)", description = "관리자 회원 목록/상세")
public class AdminUserController {

    private final AdminUserService adminUserService;

    //회원 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminUserListItemDTO>>> getUsers(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(required = false) String search,  // 이름만 검색
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(adminUserService.list(status, search, pageable));
    }

    // 회원 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminUserDetailDTO>> getUserDetail(
            @PathVariable("id") String userId
    ) {
        return ResponseEntity.ok(adminUserService.detail(userId));
    }
}
