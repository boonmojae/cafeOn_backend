package com.b1a4.cafeOn.chat.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(name = "NotificationPushDTO", description = "읽지 않은 채팅 알림 요약 DTO")
public record NotificationPushDTO(
        @Schema(description = "알림 ID", example = "12")
        Long notificationId,

        @Schema(description = "채팅방 ID", example = "5")
        Long roomId,

        @Schema(description = "채팅 메시지 ID", example = "321")
        Long chatId,

        @Schema(description = "제목", example = "모짜")
        String title,

        @Schema(description = "미리보기", example = "새로운 메시지가 있습니다")
        String preview,

        @Schema(description = "딥링크", example = "/chats/5?jump=321")
        String deeplink,

        @Schema(description = "읽음 여부", example = "false")
        boolean read,

        @Schema(description = "생성 시각(ISO-8601)", example = "2025-11-02T13:20:00")
        LocalDateTime createdAt
) {}
