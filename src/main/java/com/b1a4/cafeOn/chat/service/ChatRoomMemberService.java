package com.b1a4.cafeOn.chat.service;

import com.b1a4.cafeOn.chat.dto.member.ChatRoomMemberResponseDTO;
import com.b1a4.cafeOn.chat.entity.ChatRoomEntity;
import com.b1a4.cafeOn.chat.entity.ChatRoomMemberEntity;
import com.b1a4.cafeOn.chat.exception.AlreadyInChatRoomException;
import com.b1a4.cafeOn.chat.exception.ChatRoomFullException;
import com.b1a4.cafeOn.chat.repository.ChatRoomMemberRepository;
import com.b1a4.cafeOn.chat.repository.ChatRoomRepository;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatRoomMemberService {

    private final ChatRoomService chatRoomService;
    private final ChatService chatService;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;

    // 1:1 채팅
    @Transactional
    public ChatRoomMemberResponseDTO joinDM(String me, String counterpartId) {
        // 방 없으면 생성
        ChatRoomEntity dm = chatRoomService.getOrCreateDMEntity(me, counterpartId);

        // 이미 멤버면 넘김 아니면 저장
        for (String userId : new String[]{me, counterpartId}) {
            // 멤버인지 확인
            boolean exists = chatRoomMemberRepository
                    .existsByChatRoom_ChatRoomIdAndUser_UserId(dm.getChatRoomId(), userId);
            if (exists) continue;

            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 유저: " + userId));

            try {
                chatRoomMemberRepository.save(
                        ChatRoomMemberEntity.builder()
                                .chatRoom(dm)
                                .user(user)
                                .muted(false)
                                .build()
                );
            } catch (org.springframework.dao.DataIntegrityViolationException ignore) {

            }
        }

        // 내 멤버십 응답
        ChatRoomMemberEntity myMembership = chatRoomMemberRepository
                .findByChatRoom_ChatRoomIdAndUser_UserId(dm.getChatRoomId(), me)
                .orElseThrow(() -> new IllegalStateException("DM 멤버십 생성 실패"));

        return ChatRoomMemberResponseDTO.forDMJoin(myMembership);
    }


    // 카페 단체 채팅방 가입
    @Transactional
    public ChatRoomMemberResponseDTO joinGroup(Long cafeId, String userId) {

        // 유저 검증
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));

        // 방 생성
        ChatRoomEntity room = chatRoomService.getOrCreateGroupEntity(cafeId);

        // 중복 입장 금지
        boolean alreadyIn = chatRoomMemberRepository.existsByChatRoom_ChatRoomIdAndUser_UserId(room.getChatRoomId(), userId);
        if (alreadyIn) {
            throw new AlreadyInChatRoomException();
        }

        // 현재 인원 & 정원 검사
        int current = chatRoomMemberRepository.countByChatRoom_ChatRoomId(room.getChatRoomId());
        if (current >= room.getMaxCapacity()) {
            throw new ChatRoomFullException(room.getChatRoomId(), room.getMaxCapacity());
        }

        // 멤버 저장
        ChatRoomMemberEntity saved = chatRoomMemberRepository.save(
                ChatRoomMemberEntity.builder()
                        .chatRoom(room)
                        .user(user)
                        .muted(false)
                        .build()
        );

        // 단체 채팅방 멤버십 가입 -> 최초 입장시 시스템 메시지
        chatService.publishSystemJoin(room.getChatRoomId(), userId);

        return ChatRoomMemberResponseDTO.forGroupJoin(saved, current + 1, alreadyIn);

    }
    
    // 방-유저 멤버 여부 단순 확인
    @Transactional(readOnly = true)
    public boolean isMember(Long roomId, String userId) {
        return chatRoomMemberRepository.existsByChatRoom_ChatRoomIdAndUser_UserId(roomId, userId);
    }
    
    // 멤버 아니면 예외
    public void assertMember(Long roomId, String userId) {
        if (!isMember(roomId, userId)) {
            throw new AccessDeniedException("채팅방 멤버가 아닙니다.");
        }
    }

}
