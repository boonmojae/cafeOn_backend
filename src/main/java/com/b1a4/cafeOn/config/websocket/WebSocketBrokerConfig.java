package com.b1a4.cafeOn.config.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@EnableWebSocketMessageBroker
@Configuration
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/stomp/chats") // 브라우저가 처음 WebSocket 연결을 시도하는 HTTP URL /예) 클라이언트에서 new SockJS('/stomp/chats')
                .setAllowedOriginPatterns("*"); // CORS 유래의 오리진 허용. 개발중에는 *, 운영중에선 정확한 도메인만 열어두기
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/pub"); // 클라이언트 -> 서버 (메시지 발행)
        registry.enableSimpleBroker("/sub"); // 서버 -> 클라이언트 (단체)
        registry.setUserDestinationPrefix("/user"); // 서버 -> 특정 클라이언트 1명 (1:1 메시지)
    }

}
