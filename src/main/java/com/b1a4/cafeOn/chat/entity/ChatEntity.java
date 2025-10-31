package com.b1a4.cafeOn.chat.entity;

import com.b1a4.cafeOn.chat.enums.ChatMessageType;
import com.b1a4.cafeOn.image.entity.ImageEntity;
import com.b1a4.cafeOn.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "chats",
        indexes = {
                @Index(name = "idx_chats_room_chatid", columnList = "chatroom_id, chat_id")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long chatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private ChatMessageType messageType;

    @Column(name = "message", length = 1000, nullable = false)
    private String message;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ImageEntity> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chatroom_id", nullable = false)
    private ChatRoomEntity chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false, columnDefinition = "CHAR(36)")
    private UserEntity sender;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<NotificationEntity> notifications = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        }
    }

    public void addImage(ImageEntity image) {
        images.add(image);
        image.setChat(this);
    }

    public void removeImage(ImageEntity image) {
        images.remove(image);
        image.setChat(null);
    }

    // 채팅
    public static ChatEntity text(ChatRoomEntity room, UserEntity sender, String message) {
        return ChatEntity.builder()
                .chatRoom(room)
                .sender(sender)
                .messageType(ChatMessageType.TEXT)
                .message(message)
                .build();
    }

    // 이미지 메시지를 생성
    public static ChatEntity imageMessage(ChatRoomEntity room, UserEntity sender, String caption ) {
        return ChatEntity.builder()
                .chatRoom(room)
                .sender(sender)
                .messageType(ChatMessageType.IMAGE)
                .message(caption != null ? caption : "")
                .build();
    }

    // 단체 채팅방 새로운 유저 입장시 시스템 메시지
    public static ChatEntity systemJoin(ChatRoomEntity room, UserEntity user) {
        return ChatEntity.builder()
                .chatRoom(room)
                .sender(user)
                .messageType(ChatMessageType.SYSTEM_JOIN)
                .message(user.getNickname() + "님이 입장했습니다.")
                .build();
    }

    // 퇴장 시스템 메시지
    public static ChatEntity systemLeave(ChatRoomEntity room, UserEntity user) {
        return ChatEntity.builder()
                .chatRoom(room)
                .sender(user)
                .messageType(ChatMessageType.SYSTEM_LEAVE)
                .message(user.getNickname() + "님이 퇴장했습니다.")
                .build();
    }

    
    // 날짜 시스템 메시지
    public static ChatEntity systemDate(ChatRoomEntity room, UserEntity user, String dateLabel) {
        return ChatEntity.builder()
                .chatRoom(room)
                .sender(user)
                .messageType(ChatMessageType.SYSTEM_DATE)
                .message(dateLabel)
                .build();
    }

}