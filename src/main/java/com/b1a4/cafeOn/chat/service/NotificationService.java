package com.b1a4.cafeOn.chat.service;

import com.b1a4.cafeOn.chat.dto.chat.UnreadItemDTO;
import com.b1a4.cafeOn.chat.dto.chat.UnreadSummaryDTO;
import com.b1a4.cafeOn.chat.dto.notification.NotificationPushDTO;
import com.b1a4.cafeOn.chat.entity.ChatEntity;
import com.b1a4.cafeOn.chat.entity.ChatRoomEntity;
import com.b1a4.cafeOn.chat.entity.ChatRoomMemberEntity;
import com.b1a4.cafeOn.chat.entity.NotificationEntity;
import com.b1a4.cafeOn.chat.repository.ChatRoomMemberRepository;
import com.b1a4.cafeOn.chat.repository.NotificationRepository;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Transactional
    public void createNewChatNotifications(List<String> targetUserIds, ChatRoomEntity room, ChatEntity chat) {
        if (targetUserIds == null || targetUserIds.isEmpty()) return;

        for (String uid : targetUserIds) {
            UserEntity receiver = userRepository.getReferenceById(uid);

            // DB 저장
            NotificationEntity notification = notificationRepository.save(
                    NotificationEntity.builder()
                            .receiver(receiver)
                            .chatRoom(room)
                            .chat(chat)
                            .content(chat.getMessage())
                            .build()
            );

            // 채팅방 이름, 링크
            String title = switch (room.getType()) {
                case PRIVATE -> chat.getSender().getNickname();
                case GROUP -> room.getRoomName() != null ? room.getRoomName() : "카페 채팅방";
            };
            String deeplink = "/chats/" + room.getChatRoomId() + "?jump=" + chat.getChatId();

            // 실시간 푸시(개인 큐)
            NotificationPushDTO payload = new NotificationPushDTO(
                    notification.getNotificationId(),
                    room.getChatRoomId(),
                    chat.getChatId(),
                    title,
                    chat.getMessage() != null && !chat.getMessage().isBlank() ? chat.getMessage() : "새로운 메시지가 있습니다.",
                    deeplink,
                    notification.isRead(),
                    notification.getCreatedAt()
            );
            simpMessagingTemplate.convertAndSendToUser(uid, "/queue/notifications", payload);
        }
    }


    @Transactional(readOnly = true)
    public List<NotificationPushDTO> listUnreadForHeader(String userId) {
        // N+1 방지: 필요시 fetch join 쿼리로 교체
        return notificationRepository.findUnreadByUserExcludingMuted(userId).stream()
                .map(n -> {
                    Long roomId = n.getChatRoom() != null ? n.getChatRoom().getChatRoomId() : null;
                    Long chatId = n.getChat() != null ? n.getChat().getChatId() : null;
                    String title = (n.getChatRoom() != null && n.getChatRoom().getType().name().equals("GROUP"))
                            ? (n.getChatRoom().getRoomName() != null ? n.getChatRoom().getRoomName() : "그룹 채팅")
                            : (n.getChat() != null && n.getChat().getSender() != null
                            ? n.getChat().getSender().getNickname()
                            : "1:1 채팅");
                    String deeplink = (roomId != null && chatId != null)
                            ? "/chats/" + roomId + "?jump=" + chatId
                            : (roomId != null ? "/chats/" + roomId : null);

                    return new NotificationPushDTO(
                            n.getNotificationId(),
                            roomId,
                            chatId,
                            title,
                            n.getContent(),
                            deeplink,
                            n.isRead(),
                            n.getCreatedAt()
                    );
                })
                .toList();
    }



    @Transactional
    public void markRoomAsRead(String userId, Long roomId) {
        notificationRepository.markRoomNotificationsRead(userId, roomId);
    }
}
