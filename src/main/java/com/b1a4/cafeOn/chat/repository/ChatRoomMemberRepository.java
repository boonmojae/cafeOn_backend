package com.b1a4.cafeOn.chat.repository;

import com.b1a4.cafeOn.chat.entity.ChatRoomMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMemberEntity, Long> {
    
    // 현재 채팅방 인원 카운트
    long countByChatRoom_ChatRoomId(Long roomId);

    // 내가 참여하고 있는 채팅방인지
    boolean existsByChatRoom_ChatRoomIdAndUser_UserId(Long roomId, String userId);

    // 채팅방에 존재하고 있는 유저
    Optional<ChatRoomMemberEntity> findByChatRoom_ChatRoomIdAndUser_UserId(Long roomId, String userId);


}
