package com.b1a4.cafeOn.chat.entity;

import com.b1a4.cafeOn.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Column(name = "message", length = 1000)
    private String message;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chatroom_id", nullable = false)
    private ChatRoomEntity chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "sender_id", nullable = true, columnDefinition = "CHAR(36)")
    private UserEntity sender;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public static ChatEntity text(ChatRoomEntity room, UserEntity sender, String message) {
        return ChatEntity.builder()
                .chatRoom(room)
                .sender(sender)
                .message(message)
                .build();
    }

    public static ChatEntity image(ChatRoomEntity room, UserEntity sender, String imageUrl) {
        return ChatEntity.builder()
                .chatRoom(room)
                .sender(sender)
                .imageUrl(imageUrl)
                .build();
    }

}
