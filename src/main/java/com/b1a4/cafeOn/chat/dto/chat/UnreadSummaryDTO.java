package com.b1a4.cafeOn.chat.dto.chat;

import java.util.List;

public record UnreadSummaryDTO(
        long total,
        List<UnreadItemDTO> items
) {
}
