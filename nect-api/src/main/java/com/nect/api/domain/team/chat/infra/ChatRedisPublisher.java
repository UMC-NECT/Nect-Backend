package com.nect.api.domain.team.chat.infra;

import com.nect.api.domain.team.chat.dto.req.ChatMessageDto;
import com.nect.api.global.infra.redis.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRedisPublisher {
    private static final String CHANNEL_PREFIX = "chatroom:";

    private final RedisPublisher redisPublisher;

    public void publish(Long roomId, ChatMessageDto message) {
        redisPublisher.publish(CHANNEL_PREFIX + roomId, message);
    }
}
