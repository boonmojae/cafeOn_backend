package com.b1a4.cafeOn.chat.controller;

import com.b1a4.cafeOn.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
public class ChatReadController {

    private final ChatService chatService;
    
    // 채팅방 입장 시 -> 안읽음 메시지 일괄 읽음 처리
    @PostMapping("/{roomId}/members/me/read-latest")
    public ResponseEntity<?> readLatest(@AuthenticationPrincipal String userId, @PathVariable Long roomId) {
        chatService.markRoomReadToLatest(roomId, userId);
        com.b1a4.cafeOn.common.api.ApiResponse<Void> body =
                com.b1a4.cafeOn.common.api.ApiResponse.<Void>builder()
                        .message("최신까지 읽음 처리 완료")
                        .build();
        return ResponseEntity.ok(body);
    }
}
