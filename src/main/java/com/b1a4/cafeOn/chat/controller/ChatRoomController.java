package com.b1a4.cafeOn.chat.controller;

import com.b1a4.cafeOn.chat.dto.member.ReadRequest;
import com.b1a4.cafeOn.chat.service.ChatRoomMemberService;
import com.b1a4.cafeOn.chat.service.NotificationService;
import com.b1a4.cafeOn.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
@Slf4j
public class ChatRoomController {

    private final ChatRoomMemberService chatRoomMemberService;
    private final NotificationService notificationService;

    // 채팅방 나가기
    @DeleteMapping("/{roomId}/members/me/leave")
    public ResponseEntity<?> leaveChatRoom(@AuthenticationPrincipal String userId, @PathVariable Long roomId) {

        try {

            chatRoomMemberService.leaveChatRoom(roomId, userId);
            notificationService.markRoomAsRead(userId, roomId);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("채팅방 나가기 중 에러발생 roomId:{}, userId:{}", roomId, userId, e);

            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message("채팅방 나가기 중 에러 발생")
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // 채팅 읽음 처리
    @PatchMapping("/{roomId}/members/me/read")
    public ResponseEntity<?> markRead(@AuthenticationPrincipal String userId, @PathVariable Long roomId, @RequestBody ReadRequest readRequest) {

        chatRoomMemberService.markRoomRead(roomId, userId, readRequest.lastReadChatId());
        notificationService.markRoomAsRead(userId, roomId);
        return ResponseEntity.noContent().build();

    }

    // 채팅방 뮤트
    @PatchMapping("/{roomId}/members/me/mute")
    public ResponseEntity<Void> mute(@AuthenticationPrincipal String userId,
                                     @PathVariable Long roomId,
                                     @RequestBody MuteReq body) {
        chatRoomMemberService.updateMute(roomId, userId, body.muted());
        return ResponseEntity.noContent().build();
    }
    public record MuteReq(boolean muted) {}

}
