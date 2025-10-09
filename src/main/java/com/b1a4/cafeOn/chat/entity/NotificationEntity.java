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
        name = "notifications",
        indexes = {
                @Index(name = "idx_notis_receiver_unread", columnList = "receiver_id, is_read, created_at")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false, columnDefinition = "CHAR(36)")
    private UserEntity receiver;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "chatroom_id")
    private ChatRoomEntity chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "chat_id")
    private ChatEntity chat;

    // 알람 본문
    @Column(name = "content", nullable = false , length = 500)
    private String content;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
