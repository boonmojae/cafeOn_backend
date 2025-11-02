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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/rooms")
@Tag(name = "ChatImages", description = "채팅 이미지 전송 API")
    @SecurityRequirement(name = "Bearer Authentication")
    public class ChatImageController {

    private final ChatService chatService;

    @Operation(
            summary = "이미지 메시지 전송",
            description = "특정 채팅방(roomId)으로 이미지(여러 장 가능)를 전송합니다. caption은 선택입니다."
    )
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = ImageMessageUpload.class)
            )
    )
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

    @Schema(name = "ImageMessageUpload", description = "이미지 전송용 multipart/form-data 폼")
    static class ImageMessageUpload {
        @Schema(description = "캡션(선택)", example = "사진 보냅니다")
        public String caption;

        @ArraySchema(
                arraySchema = @Schema(description = "전송할 이미지 파일들(1개 이상)"),
                schema = @Schema(type = "string", format = "binary")
        )
        public List<MultipartFile> files;

        public ImageMessageUpload() {}
    }
}
