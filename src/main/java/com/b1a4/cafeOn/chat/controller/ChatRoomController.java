package com.b1a4.cafeOn.chat.controller;

import com.b1a4.cafeOn.chat.service.ChatRoomMemberService;
import com.b1a4.cafeOn.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
@Slf4j
public class ChatRoomController {

    private final ChatRoomMemberService chatRoomMemberService;

    // 채팅방 나가기
    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> leaveChatRoom(@AuthenticationPrincipal String userId, @PathVariable Long roomId) {

        try {

            chatRoomMemberService.leaveChatRoom(roomId, userId);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("채팅방 나가기 중 에러발생 roomId:{}, userId:{}", roomId, userId, e);

            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message("채팅방 나가기 중 에러 발생")
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);


        }
    }
}
