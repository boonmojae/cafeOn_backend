//package com.b1a4.cafeOn.chat.controller;
//
//import com.b1a4.cafeOn.chat.dto.chat.ChatRequestDTO;
//import com.b1a4.cafeOn.chat.service.ChatRoomMemberService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.messaging.handler.annotation.DestinationVariable;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.stereotype.Controller;
//
//import java.security.Principal;
//import java.util.LinkedHashMap;
//import java.util.Map;
//import java.util.Set;
//
//@Controller
//@RequiredArgsConstructor
//@Slf4j
//public class ChatController {
//
//    private final SimpMessagingTemplate template;
//    private final ChatRoomMemberService chatRoomMemberService;
//
//    @MessageMapping("/rooms/{roomId}")
//    public void send(@DestinationVariable Long roomId,
//                     @Valid @Payload ChatRequestDTO req,
//                     Principal principal,
//                     SimpMessageHeaderAccessor headerAccessor) {
//
//        if (principal == null) throw new AccessDeniedException("NO_PRINCIPAL");
//        final String userId = principal.getName();
//
//        // 세션에 구독 성공했던 roomId가 있으면 DB 재조회 생략
//        @SuppressWarnings("unchecked")
//        Set<Long> rooms = (Set<Long>) headerAccessor.getSessionAttributes().get("rooms");
//        if (rooms == null || !rooms.contains(roomId)) {
//            chatRoomMemberService.assertMember(roomId, userId);
//        }
//
//        final String msg = req.getMessage();
//        if (msg == null || msg.isBlank()) {
//            throw new IllegalArgumentException("EMPTY_MESSAGE");
//        }
//
//        log.info("[WS] SEND: roomId={}, userId={}, msg='{}'", roomId, userId, msg);
//
//        Map<String, Object> payload = new LinkedHashMap<>();
//        payload.put("roomId", roomId);
//        payload.put("senderId", userId);
//        payload.put("message", msg);
//
//        template.convertAndSend("/sub/rooms/" + roomId, payload);
//    }
//}
