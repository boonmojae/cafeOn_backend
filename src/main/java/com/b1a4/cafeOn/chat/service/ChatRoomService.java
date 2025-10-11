package com.b1a4.cafeOn.chat.service;

import com.b1a4.cafeOn.chat.dto.chatroom.ChatRoomResponseDTO;
import com.b1a4.cafeOn.chat.entity.ChatRoomEntity;
import com.b1a4.cafeOn.chat.enums.RoomType;
import com.b1a4.cafeOn.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository  chatRoomRepository;

    // 1:1 채팅
    @Transactional
    public ChatRoomResponseDTO getOrCreateDM(String me, String counterpartId) {

        // 방 생성 전 나 자신과의 방은 생성할 수 없음
        if (me.equals(counterpartId)) {
            throw new IllegalArgumentException("나 자신과의 방은 만들 수 없습니다.");
        }

        // 항상 같은 쌍
        String userSmall = (me.compareTo(counterpartId) <= 0) ? me : counterpartId;
        String userBig = (me.compareTo(counterpartId) <= 0) ? counterpartId : me;

        // 기존 방 조회 후 있으면 반환
        Optional<ChatRoomEntity> existing = chatRoomRepository.findByTypeAndUserSmallAndUserBig(RoomType.PRIVATE, userSmall, userBig);

        if (existing.isPresent()) {
            String other = me.equals(userSmall) ? userBig : userSmall;
            return ChatRoomResponseDTO.ofPrivate(existing.get(), other);
        }

        // 없으면 만들고 저장
        ChatRoomEntity dm = ChatRoomEntity.builder()
                .type(RoomType.PRIVATE)
                .userSmall(userSmall)
                .userBig(userBig)
                .maxCapacity(2)
                .build();

        ChatRoomEntity saved = chatRoomRepository.save(dm);

        String other = me.equals(userSmall) ? userBig : userSmall; // 상대 표시값
        return ChatRoomResponseDTO.ofPrivate(saved, other);

    }
    

    // 카페 다인원 채팅
    @Transactional
    public ChatRoomResponseDTO getOrCreateGroup(Long cafeId) {
        
        // 카페당 하나의 채팅방
        // 있으면 바로 반환 메서드 종료
        Optional<ChatRoomEntity> existing = chatRoomRepository.findByTypeAndCafeId(RoomType.GROUP, cafeId);

        if (existing.isPresent()) {
            return ChatRoomResponseDTO.ofGroup(existing.get()); // <- 여기서 끝남(생성 로직 안감)
        }

        // 채팅방이 없다면 이름 만들고 생성
        String cafeName = chatRoomRepository.findNameById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카페입니다. id = " + cafeId));

        String roomName = (cafeName.isBlank() ? "카페" : cafeName.trim()) + " 채팅방";
        if (roomName.length() > 100) {
            roomName = roomName.substring(0, 100);
        }

        // fixme: 단체 채팅방 인원 제한 및 임시 지정
        ChatRoomEntity group = ChatRoomEntity.builder()
                .type(RoomType.GROUP)
                .cafeId(cafeId)
                .roomName(roomName)
                .maxCapacity(4)
                .build();

        ChatRoomEntity saved = chatRoomRepository.save(group);

        return ChatRoomResponseDTO.ofGroup(saved);
        
    }



}
