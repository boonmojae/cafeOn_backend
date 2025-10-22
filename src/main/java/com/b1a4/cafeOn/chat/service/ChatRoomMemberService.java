package com.b1a4.cafeOn.chat.service;

import com.b1a4.cafeOn.chat.dto.member.ChatRoomMemberResponseDTO;
import com.b1a4.cafeOn.chat.dto.member.ChatRoomMemberSummaryDTO;
import com.b1a4.cafeOn.chat.entity.ChatRoomEntity;
import com.b1a4.cafeOn.chat.entity.ChatRoomMemberEntity;
import com.b1a4.cafeOn.chat.exception.AlreadyInChatRoomException;
import com.b1a4.cafeOn.chat.exception.ChatRoomFullException;
import com.b1a4.cafeOn.chat.exception.ChatRoomNotFoundException;
import com.b1a4.cafeOn.chat.exception.NotChatRoomMemberException;
import com.b1a4.cafeOn.chat.repository.ChatRoomMemberRepository;
import com.b1a4.cafeOn.chat.repository.ChatRoomRepository;
import com.b1a4.cafeOn.chat.repository.NotificationRepository;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatRoomMemberService {

    private final ChatRoomService chatRoomService;
    private final ChatService chatService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // 1:1 채팅방 생성(or 조회) + 가입(멱등)
    @Transactional
    public ChatRoomMemberResponseDTO joinDM(String me, String counterpartId) {
        // 방 생성 or 가져오기
        ChatRoomEntity dm = chatRoomService.getOrCreateDMEntity(me, counterpartId);

        // 두 사용자 멤버십 보장(멱등)
        for (String userId : new String[]{me, counterpartId}) {
            boolean exists = chatRoomMemberRepository
                    .existsByChatRoom_ChatRoomIdAndUser_UserId(dm.getChatRoomId(), userId);
            if (exists) continue;

            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 유저: " + userId));

            chatRoomMemberRepository.save(
                    ChatRoomMemberEntity.builder()
                            .chatRoom(dm)
                            .user(user)
                            .muted(false)
                            .build()
            );
        }

        // 내 멤버십 응답
        ChatRoomMemberEntity myMembership = chatRoomMemberRepository
                .findByChatRoom_ChatRoomIdAndUser_UserId(dm.getChatRoomId(), me)
                .orElseThrow(() -> new IllegalStateException("DM 멤버십 생성 실패"));

        return ChatRoomMemberResponseDTO.forDMJoin(myMembership);
    }

    // 카페 단체 채팅방 생성(or 조회) + 가입
    @Transactional
    public ChatRoomMemberResponseDTO joinGroup(Long cafeId, String userId) {
        // 유저 검증
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));

        // 방 생성 or 조회
        ChatRoomEntity room = chatRoomService.getOrCreateGroupEntity(cafeId);

        // 이미 멤버면 예외
        boolean alreadyIn = chatRoomMemberRepository
                .existsByChatRoom_ChatRoomIdAndUser_UserId(room.getChatRoomId(), userId);
        if (alreadyIn) {
            throw new AlreadyInChatRoomException();
        }

        // 인원/정원 검사
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

        // 최초 입장 시스템 메시지(커밋 후 브로드캐스트는 ChatService 내부에서 처리 권장한다 함
        chatService.publishSystemJoin(room.getChatRoomId(), userId);

        return ChatRoomMemberResponseDTO.forGroupJoin(saved, current + 1, false);
    }


    // 방 멤버 목록 조회
    @Transactional(readOnly = true)
    public List<ChatRoomMemberSummaryDTO> listMembers(Long roomId, String viewerId) {
        
        // 채팅방과 멤버 검증
        boolean isMember = chatRoomMemberRepository.existsByChatRoom_ChatRoomIdAndUser_UserId(roomId, viewerId);
        if (!isMember) {
            throw new NotChatRoomMemberException();
        }

        List<ChatRoomMemberSummaryDTO> listMembers = chatRoomMemberRepository.findMemberSummaries(roomId);

        if (viewerId != null) {
            listMembers.forEach(m -> m.setMe(viewerId.equals(m.getUserId())));
        }

        return listMembers;
    }


    // 채팅방 나가기 (마지막 1명일 경우 방 삭제)
    @Transactional
    public void leaveChatRoom(Long roomId, String userId) {
        // 멤버 검증
        ChatRoomMemberEntity member = chatRoomMemberRepository
                .findByChatRoom_ChatRoomIdAndUser_UserId(roomId, userId)
                .orElseThrow(NotChatRoomMemberException::new);

        long count = chatRoomMemberRepository.countByChatRoom_ChatRoomId(roomId);

        if (count > 1) {
            // 퇴장 시스템 메시지
            chatService.publishSystemLeave(roomId, userId);

            // 멤버 삭제
            chatRoomMemberRepository.delete(member);

            // 동시 퇴장 방지: 남은 인원 0이면 방 정리(존재 체크)
            long remain = chatRoomMemberRepository.countByChatRoom_ChatRoomId(roomId);
            if (remain == 0 && chatRoomRepository.existsById(roomId)) {
                chatRoomRepository.deleteById(roomId);
            }
            return;
        }

        if (count == 1) {
            // 마지막 1명: 방 삭제(존재 체크)
            notificationRepository.deleteByRoomId(roomId);

            chatRoomRepository.deleteById(roomId);
            return;
        }

        // count == 0 이면 비정상 경로
        throw new ChatRoomNotFoundException(roomId);
    }

    // 읽음 처리: lastReadChatId를 최댓값으로 갱신 + unreadCount=0
    @Transactional
    public void markRoomRead(Long roomId, String userId, Long lastReadChatId) {
        // 방 멤버 검증
        boolean isMember = chatRoomMemberRepository
                .existsByChatRoom_ChatRoomIdAndUser_UserId(roomId, userId);
        if (!isMember) throw new NotChatRoomMemberException();

        // 역주행 방지 + 0으로 초기화(레포 쿼리 내부 GREATEST)
        chatRoomMemberRepository.markRoomRead(roomId, userId, lastReadChatId);
        log.debug("[READ] roomId={}, userId={}, lastReadChatId={}", roomId, userId, lastReadChatId);
    }

    // 방 알림 on/off
    @Transactional
    public void updateMute(Long roomId, String userId, boolean muted) {
        // 방 멤버 검증
        boolean isMember = chatRoomMemberRepository
                .existsByChatRoom_ChatRoomIdAndUser_UserId(roomId, userId);
        if (!isMember) throw new NotChatRoomMemberException();

        chatRoomMemberRepository.updateMute(roomId, userId, muted);
        log.debug("[MUTE] roomId={}, userId={}, muted={}", roomId, userId, muted);
    }
}
