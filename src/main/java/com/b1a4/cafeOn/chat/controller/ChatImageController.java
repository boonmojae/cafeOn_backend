package com.b1a4.cafeOn.chat.controller;

import com.b1a4.cafeOn.chat.dto.chat.ChatResponseDTO;
import com.b1a4.cafeOn.chat.service.ChatService;
import com.b1a4.cafeOn.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/rooms")
public class ChatImageController {

    private final ChatService chatService;

    @PostMapping(
            value = "/{roomId}/messages/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sendImageMessage(@AuthenticationPrincipal String senderId, @PathVariable Long roomId,
                                              @RequestParam(value = "caption", required = false) String caption,
                                              @RequestPart("files") List<MultipartFile> files) {

        ChatResponseDTO savedDto = chatService.sendImageMessage(roomId, senderId, caption, files);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("이미지 메시지가 전송되었습니다.")
                        .data(savedDto)
                        .build()
        );
    }

}
