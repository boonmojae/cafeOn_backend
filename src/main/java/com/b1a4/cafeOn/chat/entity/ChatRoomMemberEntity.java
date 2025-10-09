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
        name = "chat_room_members",
        uniqueConstraints = {
          @UniqueConstraint(
                  name = "uk_crm_room_user",
                  columnNames = {"chatroom_id", "user_id"} // 같은 방에 같은 유저 중복 방지
          )
        },
        indexes = {
                @Index(name = "idx_crm_user", columnList = "user_id"),
                @Index(name = "idx_chat_room_members_room_lastread", columnList = "chatroom_id, last_read_chat_id")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatroom_member_id")
    private Long chatRoomMemberId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chatroom_id", nullable = false)
    private ChatRoomEntity chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "CHAR(36)")
    private UserEntity user;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Builder.Default
    @Column(name = "is_muted", nullable = false)
    private boolean muted = false;

    @Column(name = "last_read_chat_id")
    private  Long lastReadChatId;

    @PrePersist
    private void onCreate() {
        this.joinedAt = LocalDateTime.now();
    }
}
