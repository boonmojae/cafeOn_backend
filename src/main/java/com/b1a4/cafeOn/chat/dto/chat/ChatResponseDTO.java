package com.b1a4.cafeOn.chat.dto.chat;

import com.b1a4.cafeOn.chat.enums.ChatMessageType;
import com.b1a4.cafeOn.image.dto.ImageResponseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatResponseDTO", description = "채팅 메시지 응답 DTO")
public class ChatResponseDTO {

    @Schema(description = "메시지 ID", example = "101")
    private Long chatId;

    @Schema(description = "채팅방 ID", example = "1")
    private Long roomId;

    @Schema(description = "보낸 사용자 ID", example = "userA")
    private String senderId;

    @Schema(description = "메시지 내용", example = "안녕하세요!")
    private String message;

    @Schema(description = "생성 시각(ISO-8601)", example = "2025-11-02T12:34:56")
    private LocalDateTime createdAt;

    @Schema(description = "메시지 하단 시간 라벨(선택)", example = "오후 12:34", nullable = true)
    private String timeLabel;

    @Schema(description = "보낸 유저 닉네임", example = "모짜", nullable = true)
    private String senderNickname;

    @Schema(description = "보낸 유저 프로필 이미지 URL", example = "https://.../profile.jpg", nullable = true)
    private String senderProfileImageUrl;

    @Schema(description = "내가 보낸 메시지 여부", example = "true", nullable = true)
    private Boolean mine;

    @Schema(description = "메시지 타입", example = "TEXT", allowableValues = {"TEXT","IMAGE","SYSTEM"})
    private ChatMessageType messageType;

    @Schema(description = "상대 기준 안읽은 사용자 수(선택)", example = "2", nullable = true)
    private Integer othersUnreadUsers;

    @Schema(description = "첨부 이미지 리스트(선택)", nullable = true)
    private List<ImageResponseDTO> images;
}
