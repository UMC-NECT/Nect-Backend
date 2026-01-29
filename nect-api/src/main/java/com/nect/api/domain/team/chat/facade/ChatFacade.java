package com.nect.api.domain.team.chat.facade;

import com.nect.api.domain.team.chat.service.ChatRoomService;
import com.nect.api.domain.team.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

//Facade
//TODO Facade 사용 전 
@Component
@RequiredArgsConstructor
public class ChatFacade {
    private final ChatService chatService;
    private final ChatRoomService chatRoomService;
}
