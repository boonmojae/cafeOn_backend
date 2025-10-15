package com.b1a4.cafeOn.chat.dto.member;

import com.b1a4.cafeOn.chat.entity.ChatRoomMemberEntity;
import com.b1a4.cafeOn.chat.enums.RoomType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatRoomMemberResponseDTO {

    private String userId;
    private Long memberId;
    private Long cafeId;
    private Long roomId;
    private String roomName;
    private RoomType type;
    private boolean muted;
    private Long lastReadChatId;
    private Integer maxCapacity;
    private Integer currentMembers;
    private LocalDateTime joinedAt;
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
    public static ChatRoomMemberResponseDTO forGroupJoin(ChatRoomMemberEntity m, int currentMembersAfterJoin, boolean alreadyJoined) {
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
