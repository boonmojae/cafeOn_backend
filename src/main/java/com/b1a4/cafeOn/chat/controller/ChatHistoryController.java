package com.b1a4.cafeOn.chat.controller;

import com.b1a4.cafeOn.chat.dto.chat.ChatResponseDTO;
import com.b1a4.cafeOn.chat.dto.chat.CursorPage;
import com.b1a4.cafeOn.chat.service.ChatService;
import com.b1a4.cafeOn.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Slf4j
public class ChatHistoryController {

    private final ChatService chatService;

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<?> history(@PathVariable Long roomId,
                                     @RequestParam(required = false) Long beforeId,
                                     @RequestParam(defaultValue = "50") int size,
                                     @RequestParam(defaultValue = "false") boolean includeSystem,
                                     Principal principal) {
        try {
            String viewerId = principal.getName();
            log.info("[REST][HISTORY-IN ] roomId={}, viewerId={}, beforeId={}, includeSystem={}, size={}",
                    roomId, viewerId, beforeId, includeSystem, size);

            CursorPage<ChatResponseDTO> page =
                    chatService.getHistory(roomId, beforeId, size, viewerId, includeSystem);

            ApiResponse<CursorPage<ChatResponseDTO>> response = ApiResponse.<CursorPage<ChatResponseDTO>>builder()
                    .data(page)
                    .message("채팅방 메시지 목록 조회 성공")
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("채팅방 메시지 목록 조회 실패 roomId:{}", roomId, e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder().message("채팅방 메시지 목록 조회 실패").build()
            );
        }
    }
}
