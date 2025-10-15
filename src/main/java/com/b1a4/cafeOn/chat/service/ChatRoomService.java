package com.b1a4.cafeOn.chat.service;

import com.b1a4.cafeOn.chat.entity.ChatRoomEntity;
import com.b1a4.cafeOn.chat.enums.RoomType;
import com.b1a4.cafeOn.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    // 1:1 채팅
    @Transactional
    public ChatRoomEntity getOrCreateDMEntity(String me, String counterpartId) {

        if (me.equals(counterpartId)) {
            throw new IllegalArgumentException("나 자신과의 방은 만들 수 없습니다.");
        }

        // 사전순 정렬로 쌍 고정
        String userSmall = (me.compareTo(counterpartId) <= 0) ? me : counterpartId;
        String userBig = (me.compareTo(counterpartId) <= 0) ? counterpartId : me;

        return chatRoomRepository.findByTypeAndUserSmallAndUserBig(RoomType.PRIVATE, userSmall, userBig)
                .orElseGet(() -> {
                    try {
                        return chatRoomRepository.saveAndFlush(
                                ChatRoomEntity.builder()
                                        .type(RoomType.PRIVATE)
                                        .userSmall(userSmall)
                                        .userBig(userBig)
                                        .maxCapacity(2)
                                        .build()
                        );

                    } catch (DataIntegrityViolationException e) {
                        return chatRoomRepository.findByTypeAndUserSmallAndUserBig(RoomType.PRIVATE, userSmall, userBig)
                                .orElseThrow(() -> new IllegalStateException("DM 생성 중 오류"));
                    }
                });
    }


    // 카페 다인원 채팅
    @Transactional
    public ChatRoomEntity getOrCreateGroupEntity(Long cafeId) {
        return chatRoomRepository.findByTypeAndCafeIdForUpdate(RoomType.GROUP, cafeId)
                .orElseGet(() -> {
                    // 방이 없으면 생성
                    String cafeName = chatRoomRepository.findNameById(cafeId)
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카페입니다."));

                    String roomName = buildRoomName(cafeName);

                    try {

                        return chatRoomRepository.saveAndFlush(
                                ChatRoomEntity.builder()
                                        .type(RoomType.GROUP)
                                        .cafeId(cafeId)
                                        .roomName(roomName)
                                        .maxCapacity(4)
                                        .build()
                        );

                    } catch (DataIntegrityViolationException e) {

                        return chatRoomRepository.findByTypeAndCafeIdForUpdate(RoomType.GROUP, cafeId)
                                .orElseThrow(() -> new IllegalStateException("카페 단톡방 생성 중 오류"));
                    }
                });
    }

    private String buildRoomName(String cafeName) {
        String base = (cafeName == null || cafeName.isBlank()) ? "카페" : cafeName.trim();
        String name = base + " 채팅방";
        return name.length() > 100 ? name.substring(0, 100) : name;
    }

}
