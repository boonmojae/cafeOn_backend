package com.b1a4.cafeOn.chat.dto.chatroom;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDMRequestDTO { // 1:1 방 생성

    @NotBlank
    private String counterpartId; // 상대 유저 ID (서버가 user_small/user_big 계산)
    // 1:1 채팅에서는 roomName이 상대의 이름이 돼서 요청 받지 않는다

}
