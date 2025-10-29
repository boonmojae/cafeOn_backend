package com.b1a4.cafeOn.chat.repository;

import com.b1a4.cafeOn.chat.entity.ChatEntity;
import com.b1a4.cafeOn.chat.enums.ChatMessageType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.*;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    // Page -> Slice 변경

    // 텍스트 + 시스템
    // 채팅화면 스크롤(페이징처리)
    Slice<ChatEntity> findByChatRoom_ChatRoomIdOrderByChatIdDesc(Long roomId, Pageable pageable);

    // beforeId 기준으로 이전 메시지만 이어서 로드
    Slice<ChatEntity> findByChatRoom_ChatRoomIdAndChatIdLessThanOrderByChatIdDesc(Long roomId, Long beforeChatId, Pageable pageable);

    Slice<ChatEntity> findByChatRoom_ChatRoomIdAndMessageTypeInOrderByChatIdDesc(
            Long chatRoomId, List<ChatMessageType> types, Pageable pageable);

    Slice<ChatEntity> findByChatRoom_ChatRoomIdAndMessageTypeInAndChatIdLessThanOrderByChatIdDesc(
            Long chatRoomId, List<ChatMessageType> types, Long beforeId, Pageable pageable);

    // 텍스트만 조회
    // 채팅화면 스크롤(페이징처리)
    Slice<ChatEntity> findByChatRoom_ChatRoomIdAndMessageTypeOrderByChatIdDesc(Long roomId, ChatMessageType type, Pageable pageable);

    // beforeId 기준으로 이전 메시지만 이어서 로드
    Slice<ChatEntity> findByChatRoom_ChatRoomIdAndMessageTypeAndChatIdLessThanOrderByChatIdDesc(Long roomId, ChatMessageType type, Long beforeChatId, Pageable pageable);

    boolean existsByChatRoom_ChatRoomIdAndSender_UserIdAndMessageTypeAndCreatedAtAfter(
            Long roomId, String userId, ChatMessageType type, java.time.LocalDateTime after);


    @Query("""
              SELECT COALESCE(MAX(c.chatId), 0)
              FROM ChatEntity c
              WHERE c.chatRoom.chatRoomId = :roomId
            """)
    Long findMaxChatIdByRoomId(@Param("roomId") Long roomId);



}
