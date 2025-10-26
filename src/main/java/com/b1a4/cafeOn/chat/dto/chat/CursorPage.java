package com.b1a4.cafeOn.chat.dto.chat;

import java.util.List;

public record CursorPage<T>(List<T> content, Long nextCursor, boolean hasNext) {
}
