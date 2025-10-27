package com.b1a4.cafeOn.chat.repository;

import com.b1a4.cafeOn.chat.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    // 사용자 미읽음 알림 목록
    @Query("""
            SELECT n
            FROM NotificationEntity n
            WHERE n.receiver.userId =:userId
                AND n.read = false
            ORDER BY n.createdAt DESC
            """)
    List<NotificationEntity> findUnreadByUser(@Param("userId") String userId);

    // 방 단위로 읽음 처리
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE NotificationEntity n
                SET n.read = true
            WHERE n.receiver.userId =:userId
                AND n.chatRoom.chatRoomId =:roomId
                AND n.read = false
            """)
    int markRoomNotificationsRead(@Param("userId") String userId, @Param("roomId") Long roomId);


    // 채팅방을 참조하고 있는 알림 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        DELETE FROM NotificationEntity n
         WHERE n.chatRoom.chatRoomId = :roomId
    """)
    int deleteByRoomId(@Param("roomId") Long roomId);


    // 뮤트 제외 + 미읽음 알림 목록 (개별 알림)
    @Query("""
        SELECT n
        FROM NotificationEntity n
        JOIN ChatRoomMemberEntity m
            ON m.chatRoom = n.chatRoom
            AND m.user = n.receiver
        WHERE n.receiver.userId = :userId
            AND n.read = false
            AND m.muted = false
        ORDER BY n.createdAt DESC
        """)
    List<NotificationEntity> findUnreadByUserExcludingMuted(@Param("userId") String userId);


    @Modifying
    @Query("""
        DELETE FROM NotificationEntity n
        WHERE n.receiver.userId =:userId
        AND n.chatRoom.chatRoomId =:roomId
        """)
    int deleteByReceiverIdAndRoomId(@Param("userId") String userId, @Param("roomId") Long roomId);

}
