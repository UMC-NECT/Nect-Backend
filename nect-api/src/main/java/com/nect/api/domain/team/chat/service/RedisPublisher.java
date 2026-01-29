package com.nect.api.domain.team.chat.service;

import com.nect.api.domain.team.chat.dto.req.ChatMessageDTO;
import com.nect.api.domain.team.chat.enums.ChatErrorCode;
import com.nect.api.domain.team.chat.exeption.ChatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisPublisher {


    private final RedisTemplate<String, Object> objectRedisTemplate;

    // 메시지 발행
    public void publish(String channel, ChatMessageDTO message) {
        try {

            objectRedisTemplate.convertAndSend(channel, message);
            log.info("Redis 메시지 발행 성공 - Channel: {}, Sender: {}",
                    channel, message.getUserName());
        } catch (Exception e) {
            throw new ChatException(
                    ChatErrorCode.REDIS_PUBLISH_FAILED,
                    "Channel: " + channel,
                    e
            );
        }
    }
}