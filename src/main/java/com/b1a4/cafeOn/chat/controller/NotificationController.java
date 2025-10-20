package com.b1a4.cafeOn.chat.controller;

import com.b1a4.cafeOn.chat.dto.chat.UnreadSummaryDTO;
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


    // 사용자의 읽지 않은 메시지(mute=false)
    @GetMapping("/unread")
    public ResponseEntity<?> listUnread(
            @AuthenticationPrincipal String userId
    ) {
        try {
            List<NotificationPushDTO> items = notificationService.listUnreadForHeader(userId);

            ApiResponse<List<NotificationPushDTO>> response = ApiResponse.<List<NotificationPushDTO>>builder()
                    .data(items)
                    .message("사용자의 읽지 않은 채팅 알림 목록 조회 성공")
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("사용자의 읽지 않은 채팅 알림 목록 조회 실패 userId:{}", userId, e);

            ApiResponse<List<NotificationPushDTO>> error = ApiResponse.<List<NotificationPushDTO>>builder()
                    .message("사용자의 읽지 않은 채팅 알림 목록 조회 실패")
                    .build();

            return ResponseEntity.badRequest().body(error);
        }
    }

}
