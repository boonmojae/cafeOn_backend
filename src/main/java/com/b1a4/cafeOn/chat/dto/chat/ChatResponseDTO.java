package com.b1a4.cafeOn.chat.dto.chat;

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
    private String imageUrl;
    private LocalDateTime createdAt;
    private String timeLabel;

    private String senderNickname;
    private String senderProfileImageUrl;
    private boolean mine;
}
