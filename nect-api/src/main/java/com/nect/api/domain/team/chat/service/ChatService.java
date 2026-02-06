package com.nect.api.domain.team.chat.service;

import com.nect.api.domain.team.chat.converter.ChatConverter;
import com.nect.api.domain.team.chat.converter.FileConverter;
import com.nect.api.domain.team.chat.dto.req.ChatMessageDto;
import com.nect.api.domain.team.chat.dto.res.ChatMessageSearchResponseDto;
import com.nect.api.domain.team.chat.dto.res.ChatNoticeResponseDto;
import com.nect.api.domain.team.chat.dto.res.ChatRoomMessagesResponseDto;
import com.nect.api.domain.team.chat.enums.ChatErrorCode;
import com.nect.api.domain.team.chat.exeption.ChatException;
import com.nect.api.domain.team.chat.infra.ChatRedisPublisher;
import com.nect.core.entity.team.chat.ChatFile;
import com.nect.core.entity.team.chat.ChatMessage;
import com.nect.core.entity.team.chat.ChatRoom;
import com.nect.core.entity.team.chat.ChatRoomUser;
import com.nect.core.entity.team.chat.enums.MessageType;
import com.nect.core.entity.user.User;
import com.nect.core.repository.team.chat.ChatFileRepository;
import com.nect.core.repository.team.chat.ChatMessageRepository;
import com.nect.core.repository.team.chat.ChatRoomUserRepository;
import com.nect.core.repository.team.chat.ChatRoomRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository  chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRedisPublisher redisPublisher;
    private final UserRepository userRepository;
    private final ChatFileRepository chatFileRepository;

    @Transactional
    public ChatMessageDto sendMessage(Long roomId, Long userId, String content) {

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_MEMBER_NOT_FOUND));

        // 메시지 생성 및 저장
        ChatMessage message = ChatConverter.toTextMessage(chatRoom, user, content);
        chatMessageRepository.save(message);

        // 발신자의 lastReadMessageId 업데이트 (본인은 이미 읽음)
        ChatRoomUser senderRoomUser = chatRoomUserRepository
                .findByChatRoom_IdAndUser_UserId(roomId, userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED));

        senderRoomUser.setLastReadMessageId(message.getId());
        senderRoomUser.setLastReadAt(LocalDateTime.now());

        // DTO 변환
        ChatMessageDto messageDto = ChatConverter.toMessageDto(message);

        //Redis 발행
        redisPublisher.publish(roomId, messageDto);
        // readCount = 안 읽은 사람 수 (전체 인원 - 1(본인))
        int totalMembers = chatRoomUserRepository.countByChatRoomId(roomId);
        messageDto.setReadCount(totalMembers - 1);

        // 메시지 발행
        redisPublisher.publish(roomId, messageDto);

        return messageDto;
    }




    @Transactional
    public ChatRoomMessagesResponseDto getChatMessages(Long roomId, Long userId,Long lastMessageId, int size) {

        // 1. 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        // 2.  권한 확인: 채팅방 멤버인지 확인
        ChatRoomUser chatRoomUser = chatRoomUserRepository
                .findByChatRoom_IdAndUser_UserId(roomId, userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED));


        // 3. 인원 수 조회
        long memberCount = chatRoomUserRepository.countByChatRoomId(roomId);

        // 4. 메시지 조회
        Pageable pageable = PageRequest.of(0, size);
        List<ChatMessage> messages;

        if (lastMessageId == null) {
            messages = chatMessageRepository.findByChatRoomOrderByIdDesc(chatRoom, pageable);
        } else {
            messages = chatMessageRepository.findByChatRoomAndIdLessThanOrderByIdDesc(
                    chatRoom, lastMessageId, pageable);
        }

        Collections.reverse(messages);

        if (!messages.isEmpty()) {
            Long latestMessageId = messages.get(messages.size() - 1).getId();

            if (chatRoomUser.getLastReadMessageId() == null
                    || latestMessageId > chatRoomUser.getLastReadMessageId()) {

                chatRoomUser.setLastReadMessageId(latestMessageId);
                chatRoomUser.setLastReadAt(LocalDateTime.now());

                log.info("자동 읽음 처리 - roomId: {}, userId: {}, messageId: {}",
                        roomId, userId, latestMessageId);
            }
        }

        // 메시지 DTO 변환 및 readCount 계산
        List<ChatMessageDto> messageDtos = messages.stream()
                .map(message -> {
//                    ChatMessageDto dto = ChatConverter.toMessageDto(message);
                    ChatMessageDto dto;
                    if (message.getMessageType() == MessageType.FILE) {
                        Optional<ChatFile> fileOpt = chatFileRepository
                                .findByChatMessageId(message.getId());
                        if (fileOpt.isPresent()) {
                            dto = FileConverter.toFileMessageDto(message, fileOpt.get());
                        } else {
                            dto = ChatConverter.toMessageDto(message);
                        }
                    } else {
                        dto = ChatConverter.toMessageDto(message);
                    }

                    // readCount = 안 읽은 사람 수
                    int readCount = calculateUnreadCount(roomId, message.getId(), memberCount);
                    dto.setReadCount(readCount);

                    return dto;
                })
                .collect(Collectors.toList());

        return ChatRoomMessagesResponseDto.builder()
                .roomId(roomId)
                .roomName(chatRoom.getName())
                .memberCount((int) memberCount)
                .messages(messageDtos)
                .hasNext(messages.size() == size)
                .build();
    }

    @Transactional
    public ChatNoticeResponseDto createNotice(Long messageId, Boolean isPinned,Long userId) {

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_MESSAGE_NOT_FOUND));


        Long roomId = message.getChatRoom().getId();


        chatRoomUserRepository.findMemberInRoom(roomId, userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_FOUND));


        message.setIsPinned(isPinned);

        return ChatConverter.toNoticeResponseDTO(message);
    }


    @Transactional(readOnly = true)
    public ChatMessageSearchResponseDto searchMessages(
            Long roomId,
            Long userId,
            String keyword,
            int page,
            int size
    ) {
        // 1. 권한 확인
        boolean isMember = chatRoomUserRepository
                .existsByChatRoomIdAndUserUserId(roomId, userId);
        if (!isMember) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }



        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> messagesPage = chatMessageRepository.searchByKeyword(
                roomId, keyword, pageable);


        List<ChatMessageDto> messageDtos = messagesPage.getContent().stream()
                .map(ChatConverter::toMessageDto)
                .collect(Collectors.toList());


        return ChatMessageSearchResponseDto.builder()
                .keyword(keyword)
                .totalCount((int) messagesPage.getTotalElements())
                .messages(messageDtos)
                .build();
    }


    private int calculateUnreadCount(Long roomId, Long messageId, long totalMembers) {
        int readMembers = chatRoomUserRepository.countUsersWhoReadMessage(roomId, messageId);
        return (int) (totalMembers - readMembers);
    }

}
