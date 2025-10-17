package com.b1a4.cafeOn.chat.service;

import com.b1a4.cafeOn.chat.dto.chat.ChatRequestDTO;
import com.b1a4.cafeOn.chat.dto.chat.ChatResponseDTO;
import com.b1a4.cafeOn.chat.dto.chat.CursorPage;
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
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate template;


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

        log.info("[SVC][SAVE] roomId={}, chatId={}, senderId={}, type={}",
                roomId, saveChat.getChatId(), saveChat.getSender().getUserId(), saveChat.getMessageType());

        // DTO 반환
        return toDto(saveChat, null, null);

    }

    // 메시지 조회
    @Transactional(readOnly = true)
    public CursorPage<ChatResponseDTO> getHistory(Long roomId, Long beforeId, int size, String viewerId, boolean includeSystem) {

        // 채팅방 검증
        ChatRoomEntity room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));

        // 채팅방 멤버인지 검증
        boolean isMember = chatRoomMemberRepository.existsByChatRoom_ChatRoomIdAndUser_UserId(roomId, viewerId);
        if (!isMember) {
            throw new NotChatRoomMemberException();
        }

        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "chatId"));

        Slice<ChatEntity> slice;

        // 시스템 메세지 포함/미포함
        if (includeSystem) {
            slice = (beforeId == null)
                    ? chatRepository.findByChatRoom_ChatRoomIdOrderByChatIdDesc(roomId, pageable)
                    : chatRepository.findByChatRoom_ChatRoomIdAndChatIdLessThanOrderByChatIdDesc(roomId, beforeId, pageable);
        } else {
            slice = (beforeId == null)
                    ? chatRepository.findByChatRoom_ChatRoomIdAndMessageTypeOrderByChatIdDesc(roomId, ChatMessageType.TEXT, pageable)
                    : chatRepository.findByChatRoom_ChatRoomIdAndMessageTypeAndChatIdLessThanOrderByChatIdDesc(roomId, ChatMessageType.TEXT, beforeId, pageable);
        }

        List<ChatResponseDTO> items = slice.getContent().stream()
                .map(e -> toDto(e, viewerId, null))
                .toList();

        Long nextCursor = null;
        if (slice.hasNext() && !items.isEmpty()) {
            nextCursor = items.get(items.size() - 1).getChatId();
        }

        return new CursorPage<>(items, nextCursor, slice.hasNext());

    }

    // 시스템 메세지 mine=false, 탈퇴자는 알수없음
    // ResponseDTO에서 b -> B로 수정(b상태이면 false/true중 하나가 나가서 브로드캐스트 시 문제가 재발)
    private ChatResponseDTO toDto(ChatEntity e, String viewerId, String timeLabel) {
        boolean isSystem = e.getMessageType() != ChatMessageType.TEXT;

        String senderIdReal = (e.getSender() != null ? e.getSender().getUserId() : null);

        String nickname   = DisplayMasking.nicknameOf(e.getSender());
        String profileUrl = DisplayMasking.profileUrlOf(e.getSender());

        Boolean mine = null;
        if (!isSystem && e.getSender() != null && viewerId != null) {
            mine = viewerId.equals(e.getSender().getUserId());
        }

        return ChatResponseDTO.builder()
                .chatId(e.getChatId())
                .roomId(e.getChatRoom().getChatRoomId())
                .senderId(senderIdReal)
                .message(e.getMessage())
                .createdAt(e.getCreatedAt())
                .timeLabel(timeLabel)
                .senderNickname(nickname)
                .senderProfileImageUrl(profileUrl)
                .mine(mine)
                .messageType(e.getMessageType())
                .build();
    }

    // 시스템 입장 메시지
    @Transactional
    public void publishSystemJoin(Long roomId, String actorUserId) {
        ChatRoomEntity room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));
        UserEntity actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 유저"));

        // 30초 내 동일 이벤트 있으면 무시(선택)
        var cutoff = java.time.LocalDateTime.now().minusSeconds(30);
        if (chatRepository.existsByChatRoom_ChatRoomIdAndSender_UserIdAndMessageTypeAndCreatedAtAfter(
                roomId, actorUserId, ChatMessageType.SYSTEM_JOIN, cutoff)) {
            return;
        }

        ChatEntity saved = chatRepository.save(ChatEntity.systemJoin(room, actor));

        // DTO 만들어 브로드캐스트 (시스템 메시지는 mine=false 고정)
        ChatResponseDTO dto = ChatResponseDTO.builder()
                .chatId(saved.getChatId())
                .roomId(roomId)
                .senderId(null) // 시스템은 발신자 없음
                .senderNickname(null)
                .senderProfileImageUrl(null)
                .message(saved.getMessage())
                .createdAt(saved.getCreatedAt())
                .mine(null) // false -> 브로드캐스트에서는 mine 세팅 X
                .messageType(saved.getMessageType())
                .build();

        template.convertAndSend("/sub/rooms/" + roomId, dto);
    }


}
