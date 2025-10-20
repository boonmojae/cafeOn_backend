package com.b1a4.cafeOn.chat.dto.chat;

import com.b1a4.cafeOn.chat.enums.RoomType;

public record UnreadItemDTO(
        Long roomId,
        int unreadCount,
        RoomType type
) {
}
