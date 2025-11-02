package com.b1a4.cafeOn.chat.dto.room;

import com.b1a4.cafeOn.chat.enums.RoomType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "ChatRoomListItemDTO", description = "채팅방 목록 아이템 DTO")
public record ChatRoomListItemDTO(

        @Schema(description = "채팅방 ID", example = "21")
        Long roomId,

        @Schema(description = "목록에 표시할 이름(DM: 상대 닉네임, GROUP: 방 이름)", example = "홍대 카페방")
        String displayName,

        @Schema(description = "방 타입", example = "GROUP", allowableValues = {"DM","GROUP"})
        RoomType type,

        @Schema(description = "카페 ID(GROUP에서만 존재)", example = "5", nullable = true)
        Long cafeId,

        @Schema(description = "내 기준 미읽은 메시지 수", example = "3")
        int unreadCount,

        @Schema(description = "마지막 메시지 미리보기", example = "내일 2시에 만날까요?")
        String lastMessage,

        @Schema(description = "마지막 메시지 시각(ISO-8601)", example = "2025-11-02T13:45:00", nullable = true)
        LocalDateTime lastMessageAt,

        @Schema(description = "현재 방 인원 수(GROUP에서 사용)", example = "7")
        long memberCount
) {}
