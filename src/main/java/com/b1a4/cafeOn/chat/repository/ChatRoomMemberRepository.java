package com.b1a4.cafeOn.chat.repository;

import com.b1a4.cafeOn.chat.dto.member.ChatRoomMemberSummaryDTO;
import com.b1a4.cafeOn.chat.entity.ChatRoomMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMemberEntity, Long> {

    // 카페 채팅방 가입 첫 멤버 -> 채팅방 생성/ 가입되더있으면 채팅방 정보 응답
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        INSERT IGNORE INTO chat_room_members (chatroom_id, user_id, is_muted, joined_at)
        VALUES (:roomId, :userId, :muted, NOW())
        """, nativeQuery = true)
    int insertIgnore(@Param("roomId") Long roomId, @Param("userId") String userId, @Param("muted") boolean muted);


    // 현재 채팅방 인원 카운트
    long countByChatRoom_ChatRoomId(Long roomId);

    // 내가 참여하고 있는 채팅방인지
    boolean existsByChatRoom_ChatRoomIdAndUser_UserId(Long roomId, String userId);

    // 채팅방에 존재하고 있는 유저
    Optional<ChatRoomMemberEntity> findByChatRoom_ChatRoomIdAndUser_UserId(Long roomId, String userId);

    // 방 멤버 목록 조회
    @Query("""
            SELECT new com.b1a4.cafeOn.chat.dto.member.ChatRoomMemberSummaryDTO(
                u.userId,
                u.nickname,
                u.profileImage,
                false
            )
            FROM ChatRoomMemberEntity m
            JOIN m.user u
            WHERE m.chatRoom.chatRoomId = :roomId
            ORDER BY m.joinedAt ASC
            """)
    List<ChatRoomMemberSummaryDTO> findMemberSummaries(@Param("roomId") Long roomId);

    // 안읽음 메시지 증가(메시지 저장시 +1, 발신자 제외 전원)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ChatRoomMemberEntity m
                SET m.unreadCount = m.unreadCount + 1
            WHERE m.chatRoom.chatRoomId = :roomId
                AND m.user.userId <> :senderId
            """)
    int bulkIncreaseUnread(@Param("roomId") Long roomId, @Param("senderId") String senderId);


    // 방 읽은 처리(lastRead 최신화 + unreadCount 0)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ChatRoomMemberEntity m
                SET m.lastReadChatId = CASE
                    WHEN m.lastReadChatId IS NULL OR :lastRead > m.lastReadChatId
                    THEN :lastRead ELSE m.lastReadChatId END,
                   m.unreadCount = 0
                 WHERE m.chatRoom.chatRoomId =:roomId
                    AND m.user.userId =:userId
            """)
    int markRoomRead(@Param("roomId") Long roomId, @Param("userId") String userId, @Param("lastRead") Long lastReadChatId);


    // 메시지를 안 읽은 멤버 수
    @Query("""
            SELECT count(m)
            FROM ChatRoomMemberEntity m
            WHERE m.chatRoom.chatRoomId =:roomId
            AND m.user.userId <> :excludeUserId
            AND (m.lastReadChatId is NULL or m.lastReadChatId <:chatId)
            """)
    long countMembersNotReadThisChat(@Param("roomId") Long roomId, @Param("excludeUserId") String userId, @Param("chatId") Long chatId);


    // 메시지 히스토리 안 읽음 카운트
    @Query("""
            SELECT m.user.userId as userId, m.lastReadChatId as lastReadChatId
            FROM ChatRoomMemberEntity m
            WHERE m.chatRoom.chatRoomId =:roomId
            """)
    List<LastReadView> findLastReads(@Param("roomId") Long roomId);


    // 마지막 읽음 메시지
    @Query("""
            SELECT m.lastReadChatId
            FROM ChatRoomMemberEntity m
            WHERE m.chatRoom.chatRoomId = :roomId and m.user.userId = :userId
            """)
    Long findLastReadChatId(@Param("roomId") Long roomId, @Param("userId") String userId);


    // 알림 전송 대상 조회(mute=false, 발신자 제외)
    @Query("""
            SELECT m.user.userId
                FROM ChatRoomMemberEntity m
            WHERE m.chatRoom.chatRoomId =:roomId
                AND m.user.userId <> :senderId
                AND m.muted = false
            """)
    List<String> findNotificationTargets(@Param("roomId") Long roomId, @Param("senderId") String senderId);


    // 헤더 총합 알림 계산용(무트 포함)
    @Query("""
            SELECT COALESCE(SUM(m.unreadCount), 0)
             FROM ChatRoomMemberEntity m
            WHERE m.user.userId = :userId
            """)
    int sumUnreadByUser(@Param("userId") String userId);


    // mute 토글
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ChatRoomMemberEntity m
               SET m.muted = :muted
             WHERE m.chatRoom.chatRoomId = :roomId
               AND m.user.userId = :userId
            """)
    int updateMute(@Param("roomId") Long roomId, @Param("userId") String userId, @Param("muted") boolean muted);


}
