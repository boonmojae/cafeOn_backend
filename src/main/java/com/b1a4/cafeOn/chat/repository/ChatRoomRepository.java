package com.b1a4.cafeOn.chat.repository;

import com.b1a4.cafeOn.chat.entity.ChatRoomEntity;
import com.b1a4.cafeOn.chat.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {

    // 1:1 채팅방 조회
    Optional<ChatRoomEntity> findByTypeAndUserSmallAndUserBig(RoomType type, String userSmall, String userBig);

    // GROUP 방 조회 (잠금 없이)
    Optional<ChatRoomEntity> findByTypeAndCafeId(RoomType type, Long cafeId);


}
