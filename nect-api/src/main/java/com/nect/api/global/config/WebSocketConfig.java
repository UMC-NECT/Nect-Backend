package com.nect.api.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry){
        //클라이언트가ㅏ 구독할 prefix
        registry.enableSimpleBroker("/topic");
        //클라이언트가 메시지 보낼 prefix
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        //웹소켓 연결 엔드포인트
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*").withSockJS(); //CORS 설정
    }



}
