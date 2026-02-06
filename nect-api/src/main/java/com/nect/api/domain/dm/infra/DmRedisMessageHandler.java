package com.nect.api.domain.dm.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.dm.dto.DirectMessageDto;
import com.nect.api.global.infra.redis.RedisMessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DmRedisMessageHandler implements RedisMessageHandler {
    private static final String CHANNEL_PREFIX = "dm:";

    @Override
    public String channelPrefix() {
        return CHANNEL_PREFIX;
    }

    @Override
    public void handle(String channel, String payload, ObjectMapper objectMapper, SimpMessageSendingOperations messagingTemplate) throws Exception {
        DirectMessageDto dmMessage = objectMapper.readValue(payload, DirectMessageDto.class);
        String channelId = channel.substring(CHANNEL_PREFIX.length());
        String destination = "/topic/dm/" + channelId;
        messagingTemplate.convertAndSend(destination, dmMessage);
        log.info(" WebSocket 전송 완료 - Destination: {}", destination);
    }
}
