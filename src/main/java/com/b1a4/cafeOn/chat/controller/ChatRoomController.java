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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ChatRooms", description = "채팅방 참여/읽음/뮤트 API")
@SecurityRequirement(name = "Bearer Authentication")
public class ChatRoomController {

    private final ChatRoomMemberService chatRoomMemberService;
    private final NotificationService notificationService;

    // 채팅방 나가기
    @Operation(summary = "채팅방 나가기", description = "현재 사용자(me)가 해당 roomId 채팅방에서 나갑니다.")
    @DeleteMapping("/{roomId}/members/me/leave")
    public ResponseEntity<?> leaveChatRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable Long roomId) {

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
    @Operation(summary = "읽음 처리", description = "lastReadChatId 기준으로 해당 방의 마지막 읽은 메시지를 갱신합니다.")
    @PatchMapping("/{roomId}/members/me/read")
    public ResponseEntity<?> markRead(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable Long roomId,
            @RequestBody ReadRequest readRequest) {

        chatRoomMemberService.markRoomRead(roomId, userId, readRequest.lastReadChatId());
        notificationService.markRoomAsRead(userId, roomId);
        return ResponseEntity.noContent().build();
    }

    // 채팅방 뮤트
    @Operation(summary = "뮤트 토글", description = "해당 방의 알림 뮤트 상태를 설정/해제합니다.")
    @PatchMapping("/{roomId}/members/me/mute")
    public ResponseEntity<MuteRes> mute(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable Long roomId,
            @RequestBody MuteReq body) {

        chatRoomMemberService.updateMute(roomId, userId, body.muted());
        String msg = body.muted() ? "뮤트되었습니다." : "뮤트가 해제되었습니다.";
        return ResponseEntity.ok(new MuteRes(body.muted(), msg));
    }

    public record MuteReq(boolean muted) {}
    public record MuteRes(boolean muted, String message) {}
}
