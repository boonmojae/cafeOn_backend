package com.b1a4.cafeOn.chat.dto.notification;

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
public class NotificationResponseDTO {

    private Long notificationId;
    private Long roomId;
    private Long chatId;

    private String content;
    private boolean read;
    private LocalDateTime createdAt;
    private String timeLabel;

    private String displayName;
    private String deeplink;


}
