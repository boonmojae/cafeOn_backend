package com.b1a4.cafeOn.chat.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ChatRoomMemberSummaryDTO", description = "채팅방 멤버 요약 정보 DTO")
public class ChatRoomMemberSummaryDTO {

    @Schema(description = "사용자 ID", example = "userA")
    private String userId;

    @Schema(description = "닉네임", example = "모짜")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg", nullable = true)
    private String profileImage;

    @Schema(description = "요청자 본인 여부", example = "true")
    private boolean isMe;
}
