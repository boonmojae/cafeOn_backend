package com.b1a4.cafeOn.chat.dto.notification;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NotificationReadBulkRequestDTO {

    @NotEmpty
    private List<@NotNull Long> notificationIds;
}
