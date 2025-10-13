package com.b1a4.cafeOn.chat.controller;

import com.b1a4.cafeOn.chat.dto.chat.ChatRequestDTO;
import com.b1a4.cafeOn.chat.service.ChatRoomMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate template;
    private final ChatRoomMemberService chatRoomMemberService;

    @MessageMapping("/rooms/{roomId}")
    public void send(@DestinationVariable Long roomId,
                     @Valid @Payload ChatRequestDTO req,
                     Principal principal) {

        if (principal == null) throw new AccessDeniedException("NO_PRINCIPAL");
        final String userId = principal.getName();

        log.info("[WS] SEND called: roomId={}, userId={}, msg='{}', img='{}'",
                roomId, userId, req.getMessage(), req.getImageUrl());

        chatRoomMemberService.assertMember(roomId, userId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("roomId", roomId);
        payload.put("senderId", userId);
        if (req.getMessage() != null && !req.getMessage().isBlank()) {
            payload.put("message", req.getMessage());
        }
        if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) {
            payload.put("imageUrl", req.getImageUrl());
        }

        template.convertAndSend("/sub/rooms/" + roomId, payload);
    }
}
