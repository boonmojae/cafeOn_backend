package com.b1a4.cafeOn.chat.controller;

import com.b1a4.cafeOn.chat.dto.chatroom.ChatRoomResponseDTO;
import com.b1a4.cafeOn.chat.service.ChatRoomService;
import com.b1a4.cafeOn.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat/rooms")
@Slf4j
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    // 1:1
    @PostMapping("/dm")
    public ResponseEntity<?> getOrCreateDm(@AuthenticationPrincipal String userId, @RequestParam String counterpartId) {

        try {
            ChatRoomResponseDTO responseDTO = chatRoomService.getOrCreateDM(userId, counterpartId);
            ApiResponse<ChatRoomResponseDTO> response = ApiResponse.<ChatRoomResponseDTO>builder()
                    .data(responseDTO)
                    .message("1:1 채팅방 조회, 생성 성공")
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("1:1 채팅방 생성 에러", e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }

    }


    // 단체
    @PostMapping("/group/{cafeId}")
    public ResponseEntity<?> getOrCreateGroup(@AuthenticationPrincipal String userId, @PathVariable Long cafeId) {
        try {

            ChatRoomResponseDTO responseDTO = chatRoomService.getOrCreateGroup(cafeId);
            ApiResponse<ChatRoomResponseDTO> response = ApiResponse.<ChatRoomResponseDTO>builder()
                    .data(responseDTO)
                    .message("카페 단체 채팅방 조회, 생성 성공")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("카페 단체 채팅방 생성 에러", e);
            ApiResponse<?> errorResponse = ApiResponse.builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


}
