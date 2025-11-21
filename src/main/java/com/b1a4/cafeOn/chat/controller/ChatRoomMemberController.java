package com.b1a4.cafeOn.chat.controller;

import com.b1a4.cafeOn.chat.dto.member.ChatRoomMemberResponseDTO;
import com.b1a4.cafeOn.chat.dto.member.ChatRoomMemberSummaryDTO;
import com.b1a4.cafeOn.chat.service.ChatRoomMemberService;
import com.b1a4.cafeOn.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/chat/rooms")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "ChatRoomMembers", description = "채팅방 멤버 가입/조회 API")
@SecurityRequirement(name = "Bearer Authentication")
public class ChatRoomMemberController {

    private final ChatRoomMemberService chatRoomMemberService;

    @Operation(summary = "1:1 채팅방 생성 및 가입", description = "상대 사용자 ID(counterpartId)와의 1:1 채팅방을 생성하고 현재 사용자를 가입시킵니다.")
    @PostMapping("/dm/join")
    public ResponseEntity<ApiResponse<ChatRoomMemberResponseDTO>> joinDM(
                                                                            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
                                                                            @RequestParam String counterpartId) {
        try {
            ChatRoomMemberResponseDTO dto = chatRoomMemberService.joinDM(userId, counterpartId);
            ApiResponse<ChatRoomMemberResponseDTO> body = ApiResponse.<ChatRoomMemberResponseDTO>builder()
                    .data(dto)
                    .message("1:1 채팅방 가입 성공")
                    .build();
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("DM 가입 실패: userId={}, counterpartId={}", userId, counterpartId, e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.<ChatRoomMemberResponseDTO>builder()
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Operation(summary = "단체 채팅방 생성 및 가입", description = "카페 ID 기준으로 단체 채팅방을 생성하고 현재 사용자를 가입시킵니다.")
    @PostMapping("group/{cafeId}/join")
    public ResponseEntity<ApiResponse<ChatRoomMemberResponseDTO>> joinGroup(
                                                                             @Parameter(hidden = true) @AuthenticationPrincipal String userId,
                                                                             @PathVariable Long cafeId) {
        try {
            ChatRoomMemberResponseDTO dto = chatRoomMemberService.joinGroup(cafeId, userId);
            ApiResponse<ChatRoomMemberResponseDTO> body = ApiResponse.<ChatRoomMemberResponseDTO>builder()
                    .data(dto)
                    .message("단체 채팅방 가입 성공")
                    .build();
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("단체 채팅방 가입 실패: cafeId={}, userId={}", cafeId, userId, e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.<ChatRoomMemberResponseDTO>builder()
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Operation(summary = "채팅방 멤버 목록 조회", description = "특정 roomId의 참여 멤버 목록을 조회합니다.")
    @GetMapping("/{roomId}/members")
    public ResponseEntity<ApiResponse<List<ChatRoomMemberSummaryDTO>>> listMembers(
                                                                                    @Parameter(hidden = true) @AuthenticationPrincipal String userId,
                                                                                    @PathVariable Long roomId) {
        try {
            List<ChatRoomMemberSummaryDTO> items = chatRoomMemberService.listMembers(roomId, userId);
            ApiResponse<List<ChatRoomMemberSummaryDTO>> body = ApiResponse.<List<ChatRoomMemberSummaryDTO>>builder()
                    .data(items)
                    .message("채팅방 참여 목록 멤버 조회 성공")
                    .build();
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("채팅방 참여 목록 멤버 조회 실패 roomId:{}, userId:{}", roomId, userId, e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.<List<ChatRoomMemberSummaryDTO>>builder()
                            .message("채팅방 참여 목록 멤버 조회 실패")
                            .build()
            );
        }
    }
}
