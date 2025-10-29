package com.b1a4.cafeOn.chat.service;

import com.b1a4.cafeOn.chat.dto.chat.*;
import com.b1a4.cafeOn.chat.entity.ChatEntity;
import com.b1a4.cafeOn.chat.entity.ChatRoomEntity;
import com.b1a4.cafeOn.chat.enums.ChatMessageType;
import com.b1a4.cafeOn.chat.exception.ChatRoomNotFoundException;
import com.b1a4.cafeOn.chat.exception.EmptyMessageException;
import com.b1a4.cafeOn.chat.exception.NotChatRoomMemberException;
import com.b1a4.cafeOn.chat.repository.ChatRepository;
import com.b1a4.cafeOn.chat.repository.ChatRoomMemberRepository;
import com.b1a4.cafeOn.chat.repository.ChatRoomRepository;
import com.b1a4.cafeOn.chat.repository.LastReadView;
import com.b1a4.cafeOn.common.DisplayMasking;
import com.b1a4.cafeOn.image.dto.ImageResponseDTO;
import com.b1a4.cafeOn.image.entity.ImageEntity;
import com.b1a4.cafeOn.image.enums.ImageCategory;
import com.b1a4.cafeOn.image.service.S3Service;
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
import org.springframework.web.multipart.MultipartFile;

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
    private final S3Service s3Service;

    // 시간 포맷(Asia/Seoul 기준)
    private static final ZoneId Z_SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FMT_TODAY = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN);
    private static final DateTimeFormatter FMT_THISYR = DateTimeFormatter.ofPattern("M월 d일 a h:mm", Locale.KOREAN);
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

    // 안읽음 메시지 읽음
    private void publishReadReceiptAfterCommit(Long roomId, String readerId, long lastReadChatId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                template.convertAndSend("/sub/rooms/" + roomId + "/read",
                        new ReadReceiptDTO(roomId, readerId, lastReadChatId));
            }
        });
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

        // 유저 검증
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

        // 메시지를 안 읽은 멤버 수(보낸 사람 제외) -> 1:1, 단체 채팅 공통
        int unreadOthers = (int) chatRoomMemberRepository
                .countMembersNotReadThisChat(roomId, senderId, chat.getChatId());

        // 브로드캐스트/응답 DTO 구성
        ChatResponseDTO dtoForBroadcast = toDto(chat, null, unreadOthers);
        ChatResponseDTO dtoForSender = toDto(chat, senderId, unreadOthers);



        // 커밋 후 방 브로드캐스트
        ChatResponseDTO finalBroadcast = dtoForBroadcast;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                template.convertAndSend("/sub/rooms/" + roomId, finalBroadcast);
            }
        });

        // 보낸 당사자에게는 mine=true + othersUnreadUsers 포함으로 응답
        return dtoForSender;
    }


    @Transactional
    public ChatResponseDTO sendImageMessage(Long roomId, String senderId, String caption, List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 없습니다.");
        }

        ChatRoomEntity room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));

        boolean isMember = chatRoomMemberRepository.existsByChatRoom_ChatRoomIdAndUser_UserId(roomId, senderId);
        if (!isMember) throw new NotChatRoomMemberException();

        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        // 이미지 타입 채팅 생성
        ChatEntity chat = ChatEntity.imageMessage(room, sender, caption);

        for (MultipartFile file : files) {
            S3Service.UploadedImageInfo info = s3Service.uploadImage(file, ImageCategory.CHAT);

            ImageEntity img = ImageEntity.builder()
                    .originalFileName(info.getOriginalFileName())
                    .s3Key(info.getS3Key())
                    .publicUrl(info.getPublicUrl())
                    .contentType(info.getContentType())
                    .sizeBytes(info.getSizeBytes())
                    .build();

            chat.addImage(img);
        }

        ChatEntity saved = chatRepository.save(chat);

        // 낸 사람은 즉시 읽은 상태로
        chatRoomMemberRepository.markRoomRead(roomId, senderId, saved.getChatId());
        notificationService.markRoomAsRead(senderId, roomId);

        // 안 읽은 인원수 계산
        int unreadOthers = (int) chatRoomMemberRepository
                .countMembersNotReadThisChat(roomId, senderId, saved.getChatId());

        // 알림 정책
        chatRoomMemberRepository.bulkIncreaseUnread(roomId, senderId);
        List<String> targets = chatRoomMemberRepository.findNotificationTargets(roomId, senderId);
        notificationService.createNewChatNotifications(targets, room, saved);

        ChatResponseDTO dtoForBroadcast = toDto(saved, null, unreadOthers);
        ChatResponseDTO dtoForSender    = toDto(saved, senderId, unreadOthers);

        // 브로드캐스트
        ChatResponseDTO finalBroadcast = dtoForBroadcast;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                template.convertAndSend("/sub/rooms/" + roomId, finalBroadcast);
            }
        });

        return dtoForSender;

    }


    @Transactional
    public void markRoomReadToLatest(Long roomId, String userId) {
        if (!chatRoomMemberRepository.existsByChatRoom_ChatRoomIdAndUser_UserId(roomId, userId)) {
            throw new NotChatRoomMemberException();
        }

        // 현재 사용자 lastRead 가져오기 (없으면 0)
        Long cur = chatRoomMemberRepository.findLastReadChatId(roomId, userId);
        long currentLastRead = (cur == null ? 0L : cur);

        // 방의 최신 chatId
        Long maxId = chatRepository.findMaxChatIdByRoomId(roomId); // 없으면 0
        long newLastRead = (maxId == null ? 0L : maxId);

        // 이미 최신이면 아무것도 하지 않음 (중복 브로드캐스트 방지)
        if (newLastRead <= currentLastRead) {
            return;
        }

        // 실제 갱신 발생
        chatRoomMemberRepository.markRoomRead(roomId, userId, newLastRead);
        notificationService.markRoomAsRead(userId, roomId);

        // 갱신된 경우에만 읽음 브로드캐스트
        publishReadReceiptAfterCommit(roomId, userId, newLastRead);
    }


    // 이전 메시지 조회(커서+슬라이스)
    @Transactional(readOnly = true)
    public CursorPage<ChatResponseDTO> getHistory(Long roomId, Long beforeId, int size, String viewerId, boolean includeSystem) {

        chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));

        if (!chatRoomMemberRepository.existsByChatRoom_ChatRoomIdAndUser_UserId(roomId, viewerId)) {
            throw new NotChatRoomMemberException();
        }

        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "chatId"));
        Slice<ChatEntity> slice;

        if (includeSystem) {
            slice = (beforeId == null)
                    ? chatRepository.findByChatRoom_ChatRoomIdOrderByChatIdDesc(roomId, pageable)
                    : chatRepository.findByChatRoom_ChatRoomIdAndChatIdLessThanOrderByChatIdDesc(roomId, beforeId, pageable);
        } else {
            List<ChatMessageType> normalTypes = List.of(
                    ChatMessageType.TEXT,
                    ChatMessageType.IMAGE
            );

            slice = (beforeId == null)
                    ? chatRepository.findByChatRoom_ChatRoomIdAndMessageTypeInOrderByChatIdDesc(
                    roomId, normalTypes, pageable
            )
                    : chatRepository.findByChatRoom_ChatRoomIdAndMessageTypeInAndChatIdLessThanOrderByChatIdDesc(
                    roomId, normalTypes, beforeId, pageable
            );
        }

        // 방 멤버들의 lastReadChatId 한 번에
        List<LastReadView> lastReads = chatRoomMemberRepository.findLastReads(roomId);

        List<ChatResponseDTO> items = slice.getContent().stream()
                .map(e -> {
                    Integer unread = null;

                    if (e.getMessageType() == ChatMessageType.TEXT
                            || e.getMessageType() == ChatMessageType.IMAGE) {

                        String senderId = (e.getSender() != null ? e.getSender().getUserId() : null);
                        long chatId = e.getChatId();

                        int cnt = 0;
                        for (LastReadView lr : lastReads) {
                            // 보낸 사람 제외
                            if (senderId != null && senderId.equals(lr.getUserId())) continue;
                            Long lrid = lr.getLastReadChatId();
                            if (lrid == null || lrid < chatId) cnt++;
                        }
                        unread = cnt;
                    }

                    return toDto(e, viewerId, unread);
                })
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


    // 메시지 옆 안읽은 인원 수까지 포함해 DTO 생성
    private ChatResponseDTO toDto(ChatEntity e, String viewerId, Integer unreadOthersCount) {

        boolean isSystem = e.getMessageType() == ChatMessageType.SYSTEM_JOIN || e.getMessageType() == ChatMessageType.SYSTEM_LEAVE;

        String senderIdReal = (e.getSender() != null ? e.getSender().getUserId() : null);
        String nickname = DisplayMasking.nicknameOf(e.getSender());
        String profileUrl = DisplayMasking.profileUrlOf(e.getSender());

        Boolean mine = null;
        if (!isSystem && e.getSender() != null && viewerId != null) {
            mine = viewerId.equals(e.getSender().getUserId());
        }

        String timeLabel = isSystem ? null : buildTimeLabel(e.getCreatedAt());

        List<ImageResponseDTO> imageDTOS = e.getImages().stream()
                .map(ImageResponseDTO::from)
                .toList();
        List<ImageResponseDTO> imagesForDto = imageDTOS.isEmpty() ? null : imageDTOS;

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
                .othersUnreadUsers(unreadOthersCount)
                .images(imagesForDto)
                .build();
    }

}
