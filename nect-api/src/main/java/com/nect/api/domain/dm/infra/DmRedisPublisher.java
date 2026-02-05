package com.nect.api.domain.dm.infra;

import com.nect.api.domain.dm.dto.DirectMessageDto;
import com.nect.api.global.infra.redis.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DmRedisPublisher {
    private static final String CHANNEL_PREFIX = "dm:";

    private final RedisPublisher redisPublisher;

    public void publish(String channelId, DirectMessageDto message) {
        redisPublisher.publish(CHANNEL_PREFIX + channelId, message);
    }
}
