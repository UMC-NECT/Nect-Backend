package com.nect.api.domain.team.chat.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.chat.dto.req.ChatMessageDto;
import com.nect.api.global.infra.redis.RedisMessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRedisMessageHandler implements RedisMessageHandler {
    private static final String CHANNEL_PREFIX = "chatroom:";

    @Override
    public String channelPrefix() {
        return CHANNEL_PREFIX;
    }

    @Override
    public void handle(String channel, String payload, ObjectMapper objectMapper, SimpMessageSendingOperations messagingTemplate) throws Exception {
        ChatMessageDto chatMessage = objectMapper.readValue(payload, ChatMessageDto.class);
        String destination = "/topic/chatroom/" + chatMessage.getRoomId();
        messagingTemplate.convertAndSend(destination, chatMessage);
        log.info(" WebSocket 전송 완료 - Destination: {}", destination);
    }
}
