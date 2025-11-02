package com.b1a4.cafeOn.chat.dto.member;

import com.b1a4.cafeOn.chat.entity.ChatRoomMemberEntity;
import com.b1a4.cafeOn.chat.enums.RoomType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatRoomMemberResponseDTO", description = "채팅방 가입/조회 응답 DTO")
public class ChatRoomMemberResponseDTO {

    @Schema(description = "사용자 ID", example = "userA")
    private String userId;

    @Schema(description = "채팅방 멤버 PK", example = "123")
    private Long memberId;

    @Schema(description = "카페 ID(GROUP에서만 존재)", example = "5", nullable = true)
    private Long cafeId;

    @Schema(description = "채팅방 ID", example = "21")
    private Long roomId;

    @Schema(description = "채팅방 이름(GROUP에서 주로 사용)", example = "홍대 카페방", nullable = true)
    private String roomName;

    @Schema(description = "방 타입", example = "GROUP", allowableValues = {"DM","GROUP"})
    private RoomType type;

    @Schema(description = "알림 뮤트 여부", example = "false")
    private boolean muted;

    @Schema(description = "마지막 읽은 메시지 ID", example = "456", nullable = true)
    private Long lastReadChatId;

    @Schema(description = "방 최대 인원(GROUP에서만 존재)", example = "50", nullable = true)
    private Integer maxCapacity;

    @Schema(description = "현재 인원 수(GROUP에서만 존재)", example = "7", nullable = true)
    private Long currentMembers;

    @Schema(description = "가입 시각(ISO-8601)", example = "2025-11-02T12:34:56")
    private LocalDateTime joinedAt;

    @Schema(description = "이미 가입된 상태로 요청되었는지 여부", example = "false")
    private boolean alreadyJoined;

    // 1:1
    public static ChatRoomMemberResponseDTO forDMJoin(ChatRoomMemberEntity chatRoomMember) {
        return ChatRoomMemberResponseDTO.builder()
                .userId(chatRoomMember.getUser().getUserId())
                .memberId(chatRoomMember.getChatRoomMemberId())
                .roomId(chatRoomMember.getChatRoom().getChatRoomId())
                .type(chatRoomMember.getChatRoom().getType())
                .muted(chatRoomMember.isMuted())
                .lastReadChatId(chatRoomMember.getLastReadChatId())
                .joinedAt(chatRoomMember.getJoinedAt())
                .build();
    }

    // 단체
    public static ChatRoomMemberResponseDTO forGroupJoin(ChatRoomMemberEntity m, long currentMembersAfterJoin, boolean alreadyJoined) {
        return ChatRoomMemberResponseDTO.builder()
                .userId(m.getUser().getUserId())
                .memberId(m.getChatRoomMemberId())
                .cafeId(m.getChatRoom().getCafeId())
                .roomId(m.getChatRoom().getChatRoomId())
                .roomName(m.getChatRoom().getRoomName())
                .type(m.getChatRoom().getType())
                .muted(m.isMuted())
                .lastReadChatId(m.getLastReadChatId())
                .maxCapacity(m.getChatRoom().getMaxCapacity())
                .currentMembers(currentMembersAfterJoin)
                .joinedAt(m.getJoinedAt())
                .alreadyJoined(alreadyJoined)
                .build();
    }
}
