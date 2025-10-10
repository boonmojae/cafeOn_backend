package com.b1a4.cafeOn.chat.dto.member;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMemberSummaryDTO {

    private String userId;
    private String nickname;
    private String profileImageUrl;

}
