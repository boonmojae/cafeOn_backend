package com.b1a4.cafeOn.chat.service;

import com.b1a4.cafeOn.chat.dto.chat.ChatRequestDTO;
import com.b1a4.cafeOn.chat.dto.chat.ChatResponseDTO;
import com.b1a4.cafeOn.chat.entity.ChatEntity;
import com.b1a4.cafeOn.chat.entity.ChatRoomEntity;
import com.b1a4.cafeOn.chat.enums.ChatMessageType;
import com.b1a4.cafeOn.chat.exception.ChatRoomNotFoundException;
import com.b1a4.cafeOn.chat.exception.EmptyMessageException;
import com.b1a4.cafeOn.chat.exception.NotChatRoomMemberException;
import com.b1a4.cafeOn.chat.repository.ChatRepository;
import com.b1a4.cafeOn.chat.repository.ChatRoomMemberRepository;
import com.b1a4.cafeOn.chat.repository.ChatRoomRepository;
import com.b1a4.cafeOn.common.DisplayMasking;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;


    // 메시지 저장
    @Transactional
    public ChatResponseDTO saveChat(Long roomId, String senderId, ChatRequestDTO chatRequestDTO) {

        if (chatRequestDTO == null || chatRequestDTO.message() == null || chatRequestDTO.message().isBlank()) {
            throw new EmptyMessageException();
        }

        ChatRoomEntity room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));

        boolean isMember = chatRoomMemberRepository.existsByChatRoom_ChatRoomIdAndUser_UserId(roomId, senderId);
        if (!isMember) {
            throw new NotChatRoomMemberException();
        }

        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        ChatEntity saveChat = chatRepository.save(ChatEntity.text(room, sender, chatRequestDTO.message()));

        return toDto(saveChat, senderId, null);

    }

    // 메시지 조회

    // 시스템 메세지 mine=false, 탈퇴자는 알수없음
    private ChatResponseDTO toDto(ChatEntity e, String viewerId, String timeLabel) {
        boolean isSystem = e.getMessageType() != ChatMessageType.TEXT;

        String senderId = DisplayMasking.safeUserId(e.getSender(), true);
        String nickname = DisplayMasking.nicknameOf(e.getSender());
        String profileUrl = DisplayMasking.profileUrlOf(e.getSender());

        boolean mine = !isSystem
                && e.getSender() != null
                && viewerId != null
                && viewerId.equals(e.getSender().getUserId());

        return ChatResponseDTO.builder()
                .chatId(e.getChatId())
                .roomId(e.getChatRoom().getChatRoomId())
                .senderId(senderId)
                .message(e.getMessage())
                .createdAt(e.getCreatedAt())
                .timeLabel(timeLabel)
                .senderNickname(nickname)
                .senderProfileImageUrl(profileUrl)
                .mine(mine)
                .messageType(e.getMessageType())
                .build();
    }

}
