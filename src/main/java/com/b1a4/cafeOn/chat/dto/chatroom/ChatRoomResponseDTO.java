package com.b1a4.cafeOn.chat.dto.chatroom;

import com.b1a4.cafeOn.chat.dto.member.ChatMemberSummaryDTO;
import com.b1a4.cafeOn.chat.entity.ChatRoomEntity;
import com.b1a4.cafeOn.chat.enums.RoomType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드 응답에서 숨김
public class ChatRoomResponseDTO {

    private Long roomId;
    private RoomType type;

    // 공통 표기용
    private String displayName; // DM=상대 닉네임, GROUP=roomName

    // 1:1 전용 표시
    private ChatMemberSummaryDTO counterpart;

    // 그룹 전용
    private Long cafeId;
    private String roomName;
    private Integer memberCount;
    private List<ChatMemberSummaryDTO> membersPreview;

    // 최근 메시지 미리보기
    private Long lastChatId;
    private String lastMessage;
    private LocalDateTime lastAt;

    // 안읽은 알림 개수
    private Long unreadCount;

    public static ChatRoomResponseDTO ofPrivate(ChatRoomEntity chatRoom, String counterpartDisplay) {
        return ChatRoomResponseDTO.builder()
                .roomId(chatRoom.getChatRoomId())
                .type(chatRoom.getType())
                .displayName(counterpartDisplay)
                .build();
    }

    public static ChatRoomResponseDTO ofGroup(ChatRoomEntity chatRoom) {
        return ChatRoomResponseDTO.builder()
                .type(chatRoom.getType())
                .cafeId(chatRoom.getCafeId())
                .roomName(chatRoom.getRoomName())
                .roomId(chatRoom.getChatRoomId())
                .build();
    }

}
