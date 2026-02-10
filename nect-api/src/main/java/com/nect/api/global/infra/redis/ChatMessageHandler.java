package com.nect.api.global.infra.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.chat.dto.req.ChatMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatMessageHandler implements RedisMessageHandler {

    @Override
    public String channelPrefix() {
        return "chatroom:";
    }

    @Override
    public void handle(String channel, String payload,
                       ObjectMapper objectMapper,
                       SimpMessageSendingOperations messagingTemplate) throws Exception {

        String roomId = channel.replace(channelPrefix(), "");

        ChatMessageDto messageDto = objectMapper.readValue(payload, ChatMessageDto.class);

        log.info("채팅 메시지 브로드캐스트 - roomId: {}", roomId);

        messagingTemplate.convertAndSend("/topic/chatroom/" + roomId, messageDto);
    }
}