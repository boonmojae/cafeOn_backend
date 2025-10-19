package com.b1a4.cafeOn.config.websocket;

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

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final TokenProvider tokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc == null) return message;

        StompCommand cmd = acc.getCommand();
        if (cmd == null) return message;

        // CONNECT에서만 JWT 파싱/Principal 세팅
        if (StompCommand.CONNECT.equals(cmd)) {
            String bearer = first(acc.getNativeHeader("Authorization"));
            if (bearer == null) bearer = first(acc.getNativeHeader("authorization"));

            String token = stripBearer(bearer);
            if (token == null || token.isBlank()) {
                throw new AccessDeniedException("MISSING_AUTHORIZATION");
            }
            try {
                Map<String, String> claims = tokenProvider.validateAndExtractClaims(token, "access");
                if (claims == null) throw new AccessDeniedException("INVALID_TOKEN");
                String userId = claims.get("userId");
                if (userId == null || userId.isBlank()) throw new AccessDeniedException("NO_USER_ID_IN_TOKEN");

                acc.setUser(new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
            } catch (Exception e) {
                throw new AccessDeniedException("INVALID_TOKEN");
            }
            return message;
        }

        // SUBSCRIBE 목적지 검증: 방(/sub/rooms/..), 개인큐(/user/queue/..)
        if (StompCommand.SUBSCRIBE.equals(cmd)) {
            String dest = acc.getDestination();
            if (dest == null) throw new AccessDeniedException("MISSING_DESTINATION");

            if (dest.startsWith("/sub/rooms/")) {

                return message;
            }
            if (dest.startsWith("/user/queue/")) {
                return message; // 알림 구독 허용
            }
            throw new AccessDeniedException("INVALID_DESTINATION: " + dest);
        }

        // DISCONNECT 정리(옵션)
        if (StompCommand.DISCONNECT.equals(cmd)) {
            var attrs = acc.getSessionAttributes();
            if (attrs != null) attrs.remove("rooms");
            return message;
        }

        // SEND/ACK 등은 기본 통과(Principal 있는지만 간단 확인)
        if (acc.getUser() == null) {
            throw new AccessDeniedException("UNAUTHENTICATED_FRAME");
        }
        return message;
    }

    private String first(List<String> xs) { return (xs != null && !xs.isEmpty()) ? xs.get(0) : null; }
    private String stripBearer(String v) {
        if (v == null) return null;
        v = v.trim();
        return v.regionMatches(true, 0, "Bearer ", 0, 7) ? v.substring(7).trim() : v;
    }
}