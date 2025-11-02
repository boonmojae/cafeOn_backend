package com.b1a4.cafeOn.chat.controller;

import com.b1a4.cafeOn.chat.dto.notification.NotificationPushDTO;
import com.b1a4.cafeOn.chat.service.NotificationService;
import com.b1a4.cafeOn.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "채팅 알림 API")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "읽지 않은 채팅 알림 목록 조회", description = "현재 사용자의 읽지 않은 채팅 알림(뮤트 제외)을 조회합니다.")
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationPushDTO>>> listUnread(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId
    ) {
        try {
            List<NotificationPushDTO> items = notificationService.listUnreadForHeader(userId);

            ApiResponse<List<NotificationPushDTO>> response = ApiResponse.<List<NotificationPushDTO>>builder()
                    .data(items)
                    .message("사용자의 읽지 않은 채팅 알림 목록 조회 성공")
                    .build();

            return ResponseEntity.ok(response); // 200 OK
        } catch (Exception e) {
            log.error("사용자의 읽지 않은 채팅 알림 목록 조회 실패 userId:{}", userId, e);

            return ResponseEntity.badRequest().body(
                    ApiResponse.<List<NotificationPushDTO>>builder()
                            .message("사용자의 읽지 않은 채팅 알림 목록 조회 실패")
                            .build()
            );
        }
    }

}
