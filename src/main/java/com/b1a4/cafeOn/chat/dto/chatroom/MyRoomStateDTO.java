package com.b1a4.cafeOn.chat.dto.chatroom;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyRoomStateDTO {

    private Long roomId;
    private boolean muted; // 내 알람 상태
    private Long lastReadChatId; // 내 마지막 읽은 포인터

}
