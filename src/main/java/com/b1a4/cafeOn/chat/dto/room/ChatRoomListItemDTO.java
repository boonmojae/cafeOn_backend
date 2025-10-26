package com.b1a4.cafeOn.chat.dto.room;

import com.b1a4.cafeOn.chat.enums.RoomType;

import java.time.LocalDateTime;

public record ChatRoomListItemDTO(
        Long roomId,
        String displayName,
        RoomType type,
        Long cafeId,
        int unreadCount,
        String lastMessage,
        LocalDateTime lastMessageAt,
        long memberCount
) {
}
