package com.b1a4.cafeOn.chat.controller;

import com.b1a4.cafeOn.chat.dto.member.ChatRoomMemberResponseDTO;
import com.b1a4.cafeOn.chat.dto.member.ChatRoomMemberSummaryDTO;
import com.b1a4.cafeOn.chat.service.ChatRoomMemberService;
import com.b1a4.cafeOn.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat/rooms")
@Slf4j
@RequiredArgsConstructor
public class ChatRoomMemberController {

    private final ChatRoomMemberService chatRoomMemberService;

    // 1:1 생성+가입
    @PostMapping("/dm/join")
    public ResponseEntity<?> joinDM(@AuthenticationPrincipal String userId,
                                    @RequestParam String counterpartId) {
        try {

            ChatRoomMemberResponseDTO responseDTO = chatRoomMemberService.joinDM(userId, counterpartId);

            ApiResponse<ChatRoomMemberResponseDTO> response = ApiResponse.<ChatRoomMemberResponseDTO>builder()
                    .data(responseDTO)
                    .message("1:1 채팅방 가입 성공")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("DM 가입 실패: userId={}, counterpartId={}", userId, counterpartId, e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder().message(e.getMessage()).build()
            );
        }
    }


    // 카페 단체 채팅 생성+가입
    @PostMapping("group/{cafeId}/join")
    public ResponseEntity<?> joinGroup(@AuthenticationPrincipal String userId, @PathVariable Long cafeId) {

        try {

            ChatRoomMemberResponseDTO responseDTO = chatRoomMemberService.joinGroup(cafeId, userId);
            ApiResponse<ChatRoomMemberResponseDTO> response = ApiResponse.<ChatRoomMemberResponseDTO>builder()
                    .data(responseDTO)
                    .message("단체 채팅방 가입 성공")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("단체 채팅방 가입 실패: cafeId={}, userId={}", cafeId, userId, e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


    // 방 멤버 목록
    @GetMapping("/{roomId}/members")
    public ResponseEntity<?> listMembers(@AuthenticationPrincipal String userId, @PathVariable Long roomId) {

        try {

            List<ChatRoomMemberSummaryDTO> responseDTO = chatRoomMemberService.listMembers(roomId, userId);

            ApiResponse<List<ChatRoomMemberSummaryDTO>> response = ApiResponse.<List<ChatRoomMemberSummaryDTO>>builder()
                    .data(responseDTO)
                    .message("채팅방 참여 목록 멤버 조회 성공")
                    .build();

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            log.error("채팅방 참여 목록 멤버 조회 실패 roomId:{}, userId:{}", roomId, userId, e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message("채팅방 참여 목록 멤버 조회 실패")
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


}
