package com.b1a4.cafeOn.chat.repository;

import com.b1a4.cafeOn.chat.entity.ChatRoomEntity;
import com.b1a4.cafeOn.chat.enums.RoomType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {

    Optional<ChatRoomEntity> findByTypeAndUserSmallAndUserBig(RoomType type, String userSmall, String userBig);

    // 일반 조회
    Optional<ChatRoomEntity> findByTypeAndCafeId(RoomType type, Long cafeId);

    // 채팅방 잠금
    // 정원 체크/입장
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ChatRoomEntity c where c.type = :type and c.cafeId = :cafeId")
    Optional<ChatRoomEntity> findByTypeAndCafeIdForUpdate(@Param("type") RoomType type,
                                                          @Param("cafeId") Long cafeId);

    // fixme
    // CafeEntity 없음 JPQL -> 네이티브 쿼리로 변경
//    @Query("""
//            SELECT c.name
//            FROM CafeEntity c
//            WHERE c.cafeId =:cafeId
//            """)
    @Query(value = "SELECT c.name FROM cafes c WHERE c.cafe_id = :cafeId", nativeQuery = true)
    Optional<String> findNameById(@Param("cafeId") Long cafeId);



}
