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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate template;

    // 시간 포맷(Asia/Seoul 기준)
    private static final ZoneId Z_SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FMT_TODAY   = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN);
    private static final DateTimeFormatter FMT_THISYR  = DateTimeFormatter.ofPattern("M월 d일 a h:mm", Locale.KOREAN);
    private static final DateTimeFormatter FMT_OTHERYR = DateTimeFormatter.ofPattern("yyyy.MM.dd a h:mm", Locale.KOREAN);

    private String buildTimeLabel(LocalDateTime createdAt) {
        ZonedDateTime zdt = createdAt.atZone(Z_SEOUL);
        LocalDate today = LocalDate.now(Z_SEOUL);
        LocalDate d = zdt.toLocalDate();

        if (d.isEqual(today)) {
            return zdt.format(FMT_TODAY);
        }
        if (d.getYear() == today.getYear()) {
            return zdt.format(FMT_THISYR);
        }
        return zdt.format(FMT_OTHERYR);
    }

    // 메시지 저장
    @Transactional
    public ChatResponseDTO saveChat(Long roomId, String senderId, ChatRequestDTO chatRequestDTO) {
        // 유효성
        if (chatRequestDTO == null || chatRequestDTO.message() == null || chatRequestDTO.message().isBlank()) {
            throw new EmptyMessageException();
        }

        // 방/멤버 검증
        ChatRoomEntity room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));

        boolean isMember = chatRoomMemberRepository.existsByChatRoom_ChatRoomIdAndUser_UserId(roomId, senderId);
        if (!isMember) throw new NotChatRoomMemberException();

        // 프록시 대신 실체 엔티티로 조회
        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        // 저장
        ChatEntity chat = chatRepository.save(
                ChatEntity.text(room, sender, chatRequestDTO.message().trim())
        );
        
        // 보낸 사람은 즉시 읽음 처리(내 unread=0, LastReadChatId 최신화)
        chatRoomMemberRepository.markRoomRead(roomId, senderId, chat.getChatId());

        // 보낸 사람의 해당 방 알림 모두 read=true
        notificationService.markRoomAsRead(senderId, roomId);

        // 메시지에만 unread/알림
        if (chat.getMessageType() == ChatMessageType.TEXT) {
            chatRoomMemberRepository.bulkIncreaseUnread(roomId, senderId);

            List<String> targets = chatRoomMemberRepository.findNotificationTargets(roomId, senderId);
            notificationService.createNewChatNotifications(targets, room, chat);
        }

        log.info("[CHAT][SAVE] roomId={}, chatId={}, senderId={}, type={}",
                roomId, chat.getChatId(), chat.getSender().getUserId(), chat.getMessageType());

        // 커밋 후 방 브로드캐스트
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                template.convertAndSend("/sub/rooms/" + roomId, toDto(chat, null));
            }
        });

        // 보낸 당사자에게는 mine=true로 응답
        return toDto(chat, senderId);
    }


    @Transactional
    public void markRoomReadToLatest(Long roomId, String userId) {
        if (!chatRoomMemberRepository.existsByChatRoom_ChatRoomIdAndUser_UserId(roomId, userId)) {
            throw new NotChatRoomMemberException();
        }
        Long maxId = chatRepository.findMaxChatIdByRoomId(roomId); // 없으면 0
        chatRoomMemberRepository.markRoomRead(roomId, userId, maxId == null ? 0L : maxId);

        notificationService.markRoomAsRead(userId, roomId);
    }


    // 이전 메시지 조회(커서+슬라이스)
    @Transactional(readOnly = true)
    public CursorPage<ChatResponseDTO> getHistory(Long roomId, Long beforeId, int size, String viewerId, boolean includeSystem) {

        chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));

        boolean isMember = chatRoomMemberRepository.existsByChatRoom_ChatRoomIdAndUser_UserId(roomId, viewerId);
        if (!isMember) throw new NotChatRoomMemberException();

        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "chatId"));
        Slice<ChatEntity> slice;

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
                .map(e -> toDto(e, viewerId))
                .toList();

        Long nextCursor = null;
        if (slice.hasNext() && !items.isEmpty()) {
            nextCursor = items.get(items.size() - 1).getChatId();
        }

        return new CursorPage<>(items, nextCursor, slice.hasNext());
    }

    // SYSTEM: 입장 메시지
    @Transactional
    public void publishSystemJoin(Long roomId, String actorUserId) {
        ChatRoomEntity room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));
        UserEntity actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 유저"));

        var cutoff = LocalDateTime.now().minusSeconds(30);
        if (chatRepository.existsByChatRoom_ChatRoomIdAndSender_UserIdAndMessageTypeAndCreatedAtAfter(
                roomId, actorUserId, ChatMessageType.SYSTEM_JOIN, cutoff)) {
            return;
        }

        ChatEntity saved = chatRepository.save(ChatEntity.systemJoin(room, actor));
        ChatResponseDTO dto = ChatResponseDTO.builder()
                .chatId(saved.getChatId())
                .roomId(roomId)
                .senderId(null)
                .senderNickname(null)
                .senderProfileImageUrl(null)
                .message(saved.getMessage())
                .createdAt(saved.getCreatedAt())
                .mine(null)
                .messageType(saved.getMessageType())
                .build();

        template.convertAndSend("/sub/rooms/" + roomId, dto);
    }

    // SYSTEM: 퇴장 메시지
    @Transactional
    public void publishSystemLeave(Long roomId, String actorUserId) {
        ChatRoomEntity room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));
        UserEntity actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 유저"));

        var cutoff = LocalDateTime.now().minusSeconds(30);
        if (chatRepository.existsByChatRoom_ChatRoomIdAndSender_UserIdAndMessageTypeAndCreatedAtAfter(
                roomId, actorUserId, ChatMessageType.SYSTEM_LEAVE, cutoff)) {
            return;
        }

        ChatEntity saved = chatRepository.save(ChatEntity.systemLeave(room, actor));
        ChatResponseDTO dto = ChatResponseDTO.builder()
                .chatId(saved.getChatId())
                .roomId(roomId)
                .senderId(null)
                .senderNickname(null)
                .senderProfileImageUrl(null)
                .message(saved.getMessage())
                .createdAt(saved.getCreatedAt())
                .mine(null)
                .messageType(saved.getMessageType())
                .build();

        template.convertAndSend("/sub/rooms/" + roomId, dto);
    }

    // 시스템 메시지면 timeLabel = null / TEXT만 timeLabel 표시
    private ChatResponseDTO toDto(ChatEntity e, String viewerId) {
        boolean isSystem = e.getMessageType() != ChatMessageType.TEXT;

        String senderIdReal = (e.getSender() != null ? e.getSender().getUserId() : null);
        String nickname   = DisplayMasking.nicknameOf(e.getSender());
        String profileUrl = DisplayMasking.profileUrlOf(e.getSender());

        Boolean mine = null;
        if (!isSystem && e.getSender() != null && viewerId != null) {
            mine = viewerId.equals(e.getSender().getUserId());
        }

        String timeLabel = isSystem ? null : buildTimeLabel(e.getCreatedAt());

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
}
