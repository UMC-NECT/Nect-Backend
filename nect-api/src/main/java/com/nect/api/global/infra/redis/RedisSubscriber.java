package com.nect.api.global.infra.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.global.code.RedisErrorCode;
import com.nect.api.global.infra.exception.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message; // import 주의
import org.springframework.data.redis.connection.MessageListener; // import 주의
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSubscriber implements MessageListener { // MessageListener 인터페이스 구현

    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;
    private final List<RedisMessageHandler> handlers;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        try {
            //  Redis에서 온 메시지 String으로 변환
            String publishMessage = new String(message.getBody());

            log.info(" Redis Subscriber 수신 - Channel: {}, Message: {}", channel, publishMessage);

            for (RedisMessageHandler handler : handlers) {
                if (channel.startsWith(handler.channelPrefix())) {
                    handler.handle(channel, publishMessage, objectMapper, messagingTemplate);
                    return;
                }
            }

            log.warn(" Redis Subscriber 미지원 채널 - Channel: {}", channel);

        } catch (Exception e) {
            log.error(" Redis 메시지 처리 실패", e);
            throw new RedisException(RedisErrorCode.REDIS_SUBSCRIBE_FAILED, "Channel: " + channel, e);
        }
    }
}
