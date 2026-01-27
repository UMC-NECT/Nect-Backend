package com.nect.api.domain.team.chat.service;

import com.nect.api.domain.team.chat.converter.ChatConverter;
import com.nect.api.domain.team.chat.dto.req.ChatMessageDTO;
import com.nect.api.domain.team.chat.dto.res.ChatNoticeResponseDTO;
import com.nect.api.domain.team.chat.enums.ChatErrorCode;
import com.nect.api.domain.team.chat.exeption.ChatException;
import com.nect.core.entity.team.chat.ChatMessage;
import com.nect.core.entity.team.chat.ChatRoom;
import com.nect.core.entity.user.User;
import com.nect.core.repository.team.chat.ChatMessageRepository;
import com.nect.core.repository.team.chat.ChatRoomUserRepository;
import com.nect.core.repository.team.chat.ChatRoomRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository  chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RedisPublisher redisPublisher;
    private final UserRepository userRepository;


    @Transactional
    public ChatMessageDTO sendMessage(Long roomId, Long userId, String content) {

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_MEMBER_NOT_FOUND));

        // 메시지 생성 및 저장
        ChatMessage message = ChatConverter.toTextMessage(chatRoom, user, content);
        chatMessageRepository.save(message);

        ChatMessageDTO messageDto = ChatConverter.toMessageDto(message);

        //Redis 발행
        String channel = "chatroom:" + roomId;
        redisPublisher.publish(channel, messageDto);

        return messageDto;
    }




    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getChatMessages(Long roomId, Long lastMessageId, int size) {

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        //  페이징 설정 (0페이지부터 원하는 size만큼)
        Pageable pageable = PageRequest.of(0, size);

        List<ChatMessage> messages;

        if (lastMessageId == null) {
            messages = chatMessageRepository.findByChatRoomOrderByIdDesc(chatRoom, pageable);
        } else {
            messages = chatMessageRepository.findByChatRoomAndIdLessThanOrderByIdDesc(chatRoom, lastMessageId, pageable);
        }

        Collections.reverse(messages);

        return messages.stream()
                .map(ChatConverter::toMessageDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatNoticeResponseDTO createNotice(Long messageId, Boolean isPinned) {

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_MESSAGE_NOT_FOUND));
        message.setIsPinned(isPinned);

        return ChatConverter.toNoticeResponseDTO(message);
    }


}
