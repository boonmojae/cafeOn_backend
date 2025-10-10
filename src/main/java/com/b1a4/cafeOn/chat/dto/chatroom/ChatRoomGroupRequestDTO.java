package com.b1a4.cafeOn.chat.dto.chatroom;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomGroupRequestDTO { // 그룹 방 생성

    @NotNull
    private Long cafeId;

}
