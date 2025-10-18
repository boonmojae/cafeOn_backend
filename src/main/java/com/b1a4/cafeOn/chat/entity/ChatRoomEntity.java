package com.b1a4.cafeOn.chat.entity;

import com.b1a4.cafeOn.chat.enums.RoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "chat_rooms",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_dm_unique", columnNames = {"type", "user_small", "user_big"}),
                @UniqueConstraint(name = "uk_cafe_one_group", columnNames = {"type", "cafe_id"})
        },
        indexes = {
                @Index(name = "idx_chat_rooms_type", columnList = "type"),
                @Index(name = "idx_chat_rooms_cafe", columnList = "cafe_id")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatroom_id")
    private Long chatRoomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private RoomType type;

    // 1:1 전용
    @Column(name = "user_small", length=36, columnDefinition="CHAR(36)")
    private String userSmall;

    @Column(name = "user_big", length=36, columnDefinition="CHAR(36)")
    private String userBig;

    // 그룹 전용
    @Column(name = "cafe_id")
    private Long cafeId;

    @Column(name = "room_name", length = 100)
    private String roomName;

    // 기본 30 (1:1은 서비스에서 2로 넣기 권장)
    @Builder.Default
    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity = 30;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<ChatRoomMemberEntity> chatRoomMembers = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<ChatEntity> chats = new ArrayList<>();


    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // 타입별 기본 정원
        if (this.maxCapacity == null || this.maxCapacity <= 0) {
            this.maxCapacity = (this.type == RoomType.PRIVATE) ? 2 : 30;
        }
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
