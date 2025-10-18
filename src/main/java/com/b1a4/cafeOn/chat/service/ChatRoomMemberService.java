package com.b1a4.cafeOn.chat.service;

import com.b1a4.cafeOn.chat.dto.member.ChatRoomMemberResponseDTO;
import com.b1a4.cafeOn.chat.entity.ChatRoomEntity;
import com.b1a4.cafeOn.chat.entity.ChatRoomMemberEntity;
import com.b1a4.cafeOn.chat.exception.AlreadyInChatRoomException;
import com.b1a4.cafeOn.chat.exception.ChatRoomFullException;
import com.b1a4.cafeOn.chat.exception.ChatRoomNotFoundException;
import com.b1a4.cafeOn.chat.exception.NotChatRoomMemberException;
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
    private final ChatRoomRepository chatRoomRepository;
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
        long current = chatRoomMemberRepository.countByChatRoom_ChatRoomId(room.getChatRoomId());
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


    // 채팅방 나가기
    @Transactional
    public void leaveChatRoom(Long roomId, String userId) {

        // 유저 검증
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));


        // 채팅방 멤버
        ChatRoomMemberEntity member = chatRoomMemberRepository.findByChatRoom_ChatRoomIdAndUser_UserId(roomId, userId)
                .orElseThrow(NotChatRoomMemberException::new); // 람다식으로 에러 생성

        // 채팅방에 존재하는 멤버카운트
        long count = chatRoomMemberRepository.countByChatRoom_ChatRoomId(roomId);

        // 현재 멤버수가 1보다 클때 마지막 1명이 아님 -> 해당 멤버만 삭제
        if (count > 1) {
            chatService.publishSystemLeave(roomId, userId);
            chatRoomMemberRepository.delete(member);

            // 동시 퇴장 방지
            long remain = chatRoomMemberRepository.countByChatRoom_ChatRoomId(roomId);
            if (remain == 0) {
                chatRoomRepository.deleteById(roomId);
            }
            return;
        }
        
        if (count == 1) {
            // cascade 로 멤버 삭제 -> 채팅방 삭제가 아닌 바로 채팅방 삭제 진행
            chatRoomRepository.deleteById(roomId);
            return;
        }

        // count == 0은 도달 X -> 에러 추가
        throw new ChatRoomNotFoundException(roomId);

    }


}
