package com.b1a4.cafeOn.chat.controller;

import com.b1a4.cafeOn.chat.dto.chat.ChatRequestDTO;
import com.b1a4.cafeOn.chat.dto.chat.ChatResponseDTO;
import com.b1a4.cafeOn.chat.service.ChatRoomMemberService;
import com.b1a4.cafeOn.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate template;
    private final ChatService chatService;

    @MessageMapping("/rooms/{roomId}")
    public void send(@DestinationVariable Long roomId,
                     @Valid @Payload ChatRequestDTO chatRequestDTO, Principal principal) {

        String senderId = principal.getName();

        ChatResponseDTO save = chatService.saveChat(roomId, senderId, chatRequestDTO);

        // 구독자한테 브로드캐스트
        template.convertAndSend("/sub/rooms/" + roomId, save);
    }

}
