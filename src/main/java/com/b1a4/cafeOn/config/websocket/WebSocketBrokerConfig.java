package com.b1a4.cafeOn.config.websocket;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@EnableWebSocketMessageBroker
@Configuration
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

    // 기본 messageBrokerTaskScheduler 과 겹치지 않도록 커스텀 이름 사용
    @Bean(name = "customBrokerTaskScheduler")
    public ThreadPoolTaskScheduler customBrokerTaskScheduler() {
        ThreadPoolTaskScheduler ts = new ThreadPoolTaskScheduler();
        ts.setPoolSize(4); // 2~4를 권장
        ts.setThreadNamePrefix("ws-heartbeat-");
        ts.initialize();
        return ts;
    }

    @Bean
    public ServletServerContainerFactoryBean webSocketContainer() {
        var c = new ServletServerContainerFactoryBean();
        c.setMaxTextMessageBufferSize(512 * 1024);
        c.setMaxBinaryMessageBufferSize(512 * 1024);
        c.setAsyncSendTimeout(30_000L);
        c.setMaxSessionIdleTimeout(30 * 60_000L);
        return c;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/stomp/chats").setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/pub");
        registry.setUserDestinationPrefix("/user");

        registry.enableSimpleBroker("/sub", "/queue") // 방 브로드캐스트, 개인 큐
                .setTaskScheduler(customBrokerTaskScheduler())
                // .setHeartbeatValue(new long[]{10_000, 10_000}); 엄격한 설정으로 주석처리
                .setHeartbeatValue(new long[]{10_000, 0}); // 서버 -> 클라만 보내고, 클라 -> 서버 기대는 해제
    }
}
