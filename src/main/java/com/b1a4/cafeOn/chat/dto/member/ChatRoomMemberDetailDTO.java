package com.b1a4.cafeOn.chat.dto.member;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomMemberDetailDTO {

    private String userId;
    private  String nickname;
    private String profileImageUrl;

    private boolean muted;
    private LocalDateTime joinedAt;
    private Long lastReadChatId;

}
