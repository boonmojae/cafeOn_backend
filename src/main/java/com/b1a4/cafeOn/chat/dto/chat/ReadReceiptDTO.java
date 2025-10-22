package com.b1a4.cafeOn.chat.dto.chat;

public record ReadReceiptDTO(
        Long roomId,
        String readerId,
        Long lastReadChatId
) {
}
