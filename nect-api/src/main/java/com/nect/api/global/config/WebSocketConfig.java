package com.nect.api.global.config;

import com.nect.api.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(
                            ServerHttpRequest request,
                            ServerHttpResponse response,
                            WebSocketHandler wsHandler,
                            Map<String, Object> attributes) throws Exception {

                        log.info(" WebSocket 핸드셰이크 시작");

                        if (request instanceof ServletServerHttpRequest) {
                            ServletServerHttpRequest servletRequest =
                                    (ServletServerHttpRequest) request;

                            String token = servletRequest.getServletRequest()
                                    .getParameter("token");

                            log.info(" 토큰 확인: {}", token != null ? "있음" : "없음");

                            if (token != null && !token.isEmpty()) {
                                try {
                                    jwtUtil.validateToken(token);
                                    Long userId = jwtUtil.getUserIdFromToken(token);
                                    attributes.put("userId", userId.toString());
                                    log.info("WebSocket 인증 성공 - userId: {}", userId);
                                    return true;
                                } catch (Exception e) {
                                    log.error(" WebSocket 인증 실패: {}", e.getMessage(), e);
                                    return false;
                                }
                            } else {
                                log.warn(" 토큰이 없습니다");
                                return false;
                            }
                        }

                        return false;
                    }

                    @Override
                    public void afterHandshake(
                            ServerHttpRequest request,
                            ServerHttpResponse response,
                            WebSocketHandler wsHandler,
                            Exception exception) {
                        log.info(" WebSocket 핸드셰이크 완료");
                    }
                })
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    Object userId = accessor.getSessionAttributes().get("userId");

                    if (userId != null) {
                        accessor.setUser(new Principal() {
                            @Override
                            public String getName() {
                                return userId.toString();
                            }
                        });

                        log.info(" Principal 설정 완료 - userId: {}", userId);
                    }
                }

                return message;
            }
        });
    }
}