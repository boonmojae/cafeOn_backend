package com.b1a4.cafeOn.chat.dto.notification;

import java.time.LocalDateTime;

public record NotificationPushDTO(
        Long notificationId,
        Long roomId,
        Long chatId,
        String title,          // DM: 상대 닉네임, GROUP: 방 이름
        String preview,        // 새로운 메시지가 있습니다 또는 실제 본문
        String deeplink,       // "/chats/{roomId}?jump={chatId}"
        boolean read,
        LocalDateTime createdAt
) {
}
