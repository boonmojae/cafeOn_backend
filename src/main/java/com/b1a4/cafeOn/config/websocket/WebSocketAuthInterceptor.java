package com.b1a4.cafeOn.config.websocket;

import com.b1a4.cafeOn.chat.service.ChatRoomMemberService;
import com.b1a4.cafeOn.config.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final TokenProvider tokenProvider;
    private final ChatRoomMemberService chatRoomMemberService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc == null) return message;

        // CONNECT: JWT 인증 → Principal(userId) 세팅 + 형식 가드 추가
        if (StompCommand.CONNECT.equals(acc.getCommand())) {
            String bearer = first(acc.getNativeHeader("Authorization"));
            String token  = stripBearer(bearer);

            if (token == null || token.isBlank()) {
                throw new AccessDeniedException("MISSING_TOKEN");
            }
            long dots = token.chars().filter(ch -> ch == '.').count();
            if (dots != 2) {
                throw new AccessDeniedException("MALFORMED_JWT");
            }

            var claims = tokenProvider.validateAndExtractClaims(token, "access");
            if (claims == null) throw new AccessDeniedException("INVALID_TOKEN");

            String userId = claims.get("userId");
            acc.setUser(new UsernamePasswordAuthenticationToken(
                    userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        }

        // SUBSCRIBE: 방 구독 권한 확인 (+ 통과하면 세션에 roomId 저장)
        if (StompCommand.SUBSCRIBE.equals(acc.getCommand())) {
            String dest = acc.getDestination();
            String userId = acc.getUser() != null ? acc.getUser().getName() : null;

            if (dest != null && dest.startsWith("/sub/rooms/")) {
                Long roomId = parseRoomId(dest);
                if (roomId == null || userId == null ||
                        !chatRoomMemberService.isMember(roomId, userId)) {
                    throw new AccessDeniedException("NOT_A_ROOM_MEMBER");
                }
                // 통과했으면 이 세션이 접근 가능한 roomId를 기록
                @SuppressWarnings("unchecked")
                Set<Long> rooms = (Set<Long>) acc.getSessionAttributes()
                        .computeIfAbsent("rooms", k -> new HashSet<Long>());
                rooms.add(roomId);
            }
        }

        // DISCONNECT: 세션 속성 정리
        if (StompCommand.DISCONNECT.equals(acc.getCommand())) {
            var attrs = acc.getSessionAttributes();
            if (attrs != null) {
                attrs.remove("rooms");
            }
        }

        return message;
    }

    private String first(List<String> xs) {
        return (xs != null && !xs.isEmpty()) ? xs.get(0) : null;
    }

    private String stripBearer(String bearerOrToken) {
        if (bearerOrToken == null) return null;
        String b = bearerOrToken.trim();
        return (b.regionMatches(true, 0, "Bearer ", 0, 7)) ? b.substring(7).trim() : b;
    }

    private Long parseRoomId(String dest) {
        try { return Long.valueOf(dest.substring(dest.lastIndexOf('/') + 1)); }
        catch (Exception e) { return null; }
    }
}
