package com.b1a4.cafeOn.admin.penalty.controller;

import com.b1a4.cafeOn.admin.penalty.dto.PenaltyRequestDTO;
import com.b1a4.cafeOn.admin.penalty.dto.PenaltyResponseDTO;
import com.b1a4.cafeOn.admin.penalty.service.PenaltyService;
import com.b1a4.cafeOn.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Penalty (Admin)")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class PenaltyController {

    private final PenaltyService penaltyService;

    // 회원 경고 부여
    @Operation(summary = "회원 제재 (벌점 / 경고)", description = "관리자가 회원에게 경고(WARNING) 패널티를 부여합니다.")
    @PostMapping("/{userId}/penalty")
    public ResponseEntity<ApiResponse<PenaltyResponseDTO>> giveWarning(
            @AuthenticationPrincipal String adminId,
            @PathVariable("userId") String userId,
            @RequestBody PenaltyRequestDTO request
    ) {
        PenaltyResponseDTO response = penaltyService.giveWarning(userId, adminId, request, null);
        return ResponseEntity.ok(
                ApiResponse.<PenaltyResponseDTO>builder()
                        .message("회원에게 경고가 부여되었습니다.")
                        .data(response)
                        .build()
        );
    }

    // 회원 정지 부여
    @Operation(summary = "회원 정지", description = "관리자가 회원에게 일정 기간 정지(SUSPEND) 패널티를 부여합니다.")
    @PostMapping("/{userId}/suspend")
    public ResponseEntity<ApiResponse<PenaltyResponseDTO>> suspendUser(
            @AuthenticationPrincipal String adminId,
            @PathVariable("userId") String userId,
            @RequestBody PenaltyRequestDTO request
    ) {
        PenaltyResponseDTO response = penaltyService.suspend(userId, adminId, request, null);
        return ResponseEntity.ok(
                ApiResponse.<PenaltyResponseDTO>builder()
                        .message("회원이 정지되었습니다.")
                        .data(response)
                        .build()
        );
    }
}
