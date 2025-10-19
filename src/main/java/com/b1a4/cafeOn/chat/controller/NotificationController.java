package com.b1a4.cafeOn.chat.controller;

import com.b1a4.cafeOn.chat.dto.notification.NotificationPushDTO;
import com.b1a4.cafeOn.chat.service.NotificationService;
import com.b1a4.cafeOn.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/unread")
    public ResponseEntity<?> listUnread(@AuthenticationPrincipal String userId) {

        try {

            List<NotificationPushDTO> responseDTO = notificationService.listUnreadForHeader(userId);

            ApiResponse<List<NotificationPushDTO>> response = ApiResponse.<List<NotificationPushDTO>>builder()
                    .data(responseDTO)
                    .message("사용자의 읽지 않은 채팅 알림 목록 조회 성공")
                    .build();

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            log.error("사용자의 읽지 않은 채팅 알림 목록 조회 실패 userId:{}", userId, e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message("사용자의 읽지 않은 채팅 알림 목록 조회 실패")
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
