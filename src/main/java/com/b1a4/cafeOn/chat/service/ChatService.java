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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        // 메시지 공백 예외
        if (chatRequestDTO == null || chatRequestDTO.message() == null || chatRequestDTO.message().isBlank()) {
            throw new EmptyMessageException();
        }

        // 채팅방 검증
        ChatRoomEntity room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));

        // 채팅방 멤버인지 검증
        boolean isMember = chatRoomMemberRepository.existsByChatRoom_ChatRoomIdAndUser_UserId(roomId, senderId);
        if (!isMember) {
            throw new NotChatRoomMemberException();
        }

        // 유저 검증
        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        // 메시지 저장
        ChatEntity saveChat = chatRepository.save(ChatEntity.text(room, sender, chatRequestDTO.message()));

        // DTO 반환
        return toDto(saveChat, senderId, null);

    }

    // 메시지 조회
    @Transactional(readOnly = true)
    public Page<ChatResponseDTO> getHistory(Long roomId, Long beforeId, Pageable pageable, String viewerId, boolean includeSystem) {

        // 채팅방 검증
        ChatRoomEntity room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));

        // 채팅방 멤버인지 검증
        boolean isMember = chatRoomMemberRepository.existsByChatRoom_ChatRoomIdAndUser_UserId(roomId, viewerId);
        if (!isMember) {
            throw new NotChatRoomMemberException();
        }

        Page<ChatEntity> page;


        // 시스템 메세지 포함/미포함
        if (includeSystem) {
            page = (beforeId == null)
                    ? chatRepository.findByChatRoom_ChatRoomIdOrderByChatIdDesc(roomId, pageable)
                    : chatRepository.findByChatRoom_ChatRoomIdAndChatIdLessThanOrderByChatIdDesc(roomId, beforeId, pageable);
        } else {
            page = (beforeId == null)
                    ? chatRepository.findByChatRoom_ChatRoomIdAndMessageTypeOrderByChatIdDesc(roomId, ChatMessageType.TEXT, pageable)
                    : chatRepository.findByChatRoom_ChatRoomIdAndMessageTypeAndChatIdLessThanOrderByChatIdDesc(roomId, ChatMessageType.TEXT, beforeId, pageable);
        }
        return page.map(e -> toDto(e, viewerId, null));

    }

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
