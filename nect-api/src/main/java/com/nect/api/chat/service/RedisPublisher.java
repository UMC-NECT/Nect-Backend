package com.nect.api.chat.service;

import com.nect.api.chat.dto.ChatMessageDto;
import com.nect.api.chat.enums.ChatErrorCode;
import com.nect.api.chat.exeption.ChatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisPublisher {

    //RedisTemplate==템플릿 클래스
    private final RedisTemplate<String, Object> objectRedisTemplate;

    // 메시지 발행
    public void publish(String channel, ChatMessageDto message) {
        try {
            // convertAndSend : Redis에 메시지 전송
            objectRedisTemplate.convertAndSend(channel, message);
            log.info("Redis 메시지 발행 성공 - Channel: {}, Sender: {}",
                    channel, message.getSenderName());
        } catch (Exception e) {
            throw new ChatException(
                    ChatErrorCode.REDIS_PUBLISH_FAILED,
                    "Channel: " + channel,
                    e
            );
        }
    }
}