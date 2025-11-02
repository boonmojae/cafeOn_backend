package com.b1a4.cafeOn.chat.controller;

import com.b1a4.cafeOn.chat.service.ChatService;
import com.b1a4.cafeOn.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
@Tag(name = "ChatRooms", description = "채팅 읽음 처리 API")
@SecurityRequirement(name = "Bearer Authentication")
public class ChatReadController {

    private final ChatService chatService;

    // 채팅방 입장 시 -> 안읽음 메시지 일괄 읽음 처리
    @Operation(summary = "최신까지 읽음 처리", description = "해당 채팅방의 안읽은 메시지를 최신 메시지까지 일괄 읽음 처리합니다.")
    @PostMapping("/{roomId}/members/me/read-latest")
    public ResponseEntity<ApiResponse<Void>> readLatest(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable Long roomId
    ) {
        chatService.markRoomReadToLatest(roomId, userId);
        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .message("최신까지 읽음 처리 완료")
                .build();
        return ResponseEntity.ok(body);
    }
}
