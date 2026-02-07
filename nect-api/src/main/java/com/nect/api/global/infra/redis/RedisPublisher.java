package com.nect.api.global.infra.redis;

import com.nect.api.global.code.RedisErrorCode;
import com.nect.api.global.infra.exception.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisPublisher {


    private final RedisTemplate<String, Object> objectRedisTemplate;

    public void publish(String channel, Object message) {
        try {
            objectRedisTemplate.convertAndSend(channel, message);
            log.info("Redis 메시지 발행 성공 - Channel: {}", channel);
        } catch (Exception e) {
            throw new RedisException(RedisErrorCode.REDIS_PUBLISH_FAILED, "Channel: " + channel, e);
        }
    }
}
