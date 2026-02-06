package com.nect.api.global.infra.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

public interface RedisMessageHandler {
    String channelPrefix();
    void handle(String channel, String payload, ObjectMapper objectMapper, SimpMessageSendingOperations messagingTemplate) throws Exception;
}
