package com.b1a4.cafeOn.chat.dto.notification;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationReadRequestDTO {

    @NotNull
    private Long notificationId;

}
