package com.b1a4.cafeOn.chat.controller;

import com.b1a4.cafeOn.chat.dto.chat.ChatResponseDTO;
import com.b1a4.cafeOn.chat.service.ChatService;
import com.b1a4.cafeOn.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
                                     @RequestParam(defaultValue = "false") boolean includeSystem,
                                     @PageableDefault(size = 50, sort = "chatId", direction = org.springframework.data.domain.Sort.Direction.DESC)
                                     Pageable pageable, Principal principal) {
        try {

            String viewerId = principal.getName();
            Page<ChatResponseDTO> responseDTOS = chatService.getHistory(roomId, beforeId, pageable, viewerId, includeSystem);

            ApiResponse<Page<ChatResponseDTO>> response = ApiResponse.<Page<ChatResponseDTO>>builder()
                    .data(responseDTOS)
                    .message("채팅방 메시지 목록 조회 성공")
                    .build();

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            log.error("채팅방 메시지 목록 조회 실패 roomId:{}", roomId , e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message("채팅방 메시지 목록 조회 실패")
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
