package com.b1a4.cafeOn.chat.repository;

import com.b1a4.cafeOn.chat.entity.ChatEntity;
import com.b1a4.cafeOn.chat.enums.ChatMessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
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


    // 텍스트만 조회
    // 채팅화면 스크롤(페이징처리)
    Slice<ChatEntity> findByChatRoom_ChatRoomIdAndMessageTypeOrderByChatIdDesc(Long roomId, ChatMessageType type, Pageable pageable);

    // beforeId 기준으로 이전 메시지만 이어서 로드
    Slice<ChatEntity> findByChatRoom_ChatRoomIdAndMessageTypeAndChatIdLessThanOrderByChatIdDesc(Long roomId, ChatMessageType type, Long beforeChatId, Pageable pageable);

    boolean existsByChatRoom_ChatRoomIdAndSender_UserIdAndMessageTypeAndCreatedAtAfter(
            Long roomId, String userId, ChatMessageType type, java.time.LocalDateTime after);

}
