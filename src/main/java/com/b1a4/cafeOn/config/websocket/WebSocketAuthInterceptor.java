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

        if (StompCommand.CONNECT.equals(acc.getCommand())) {
            String bearer = first(acc.getNativeHeader("Authorization"));
            if (bearer == null) bearer = first(acc.getNativeHeader("authorization"));

            String token = stripBearer(bearer);
            if (token == null || token.isBlank()) {
                return null;
            }

            try {
                Map<String, String> claims = tokenProvider.validateAndExtractClaims(token, "access");
                if (claims == null) {
                    return null;
                }
                String userId = claims.get("userId");
                if (userId == null || userId.isBlank()) {
                    throw new AccessDeniedException("NO_USER_ID_IN_TOKEN");
                }
                acc.setUser(new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
            } catch (Exception e) {
                System.out.println("[WS][CONNECT] JWT parse/validate failed: " + e);
                throw new AccessDeniedException("INVALID_TOKEN");
            }
        }

        if (StompCommand.SUBSCRIBE.equals(acc.getCommand())) {
            String dest = acc.getDestination();
            if (dest == null || !dest.startsWith("/sub/rooms/")) {
                throw new AccessDeniedException("INVALID_DESTINATION");
            }
        }

        if (StompCommand.DISCONNECT.equals(acc.getCommand())) {
            var attrs = acc.getSessionAttributes();
            if (attrs != null) attrs.remove("rooms");
        }
        return message;
    }

    private String first(List<String> xs) {
        return (xs != null && !xs.isEmpty()) ? xs.get(0) : null;
    }

    private String stripBearer(String v) {
        if (v == null) return null;
        v = v.trim();
        return v.regionMatches(true, 0, "Bearer ", 0, 7) ? v.substring(7).trim() : v;
    }
}
