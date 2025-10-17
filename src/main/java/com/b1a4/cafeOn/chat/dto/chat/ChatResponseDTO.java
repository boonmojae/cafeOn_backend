package com.b1a4.cafeOn.chat.dto.chat;

import com.b1a4.cafeOn.chat.enums.ChatMessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatResponseDTO {

    private Long chatId;
    private Long roomId;
    private String senderId;
    private String message;
    private LocalDateTime createdAt;
    private String timeLabel;
    private String senderNickname;
    private String senderProfileImageUrl;
    private Boolean mine; // null이 허용되게 boolean -> B로 수정
    ChatMessageType messageType;

//    private String imageUrl;
}
