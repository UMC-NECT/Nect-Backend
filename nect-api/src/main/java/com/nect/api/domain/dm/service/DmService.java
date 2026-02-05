package com.nect.api.domain.dm.service;

import com.nect.api.domain.dm.dto.DirectMessageDto;
import com.nect.api.domain.dm.dto.DmMessageListResponse;
import com.nect.api.domain.dm.dto.DmRoomListResponse;
import com.nect.api.domain.dm.dto.DmRoomSummaryDto;
import com.nect.api.domain.dm.infra.DmRedisPublisher;
import com.nect.api.domain.user.exception.UserNotFoundException;
import com.nect.core.entity.dm.DirectMessage;
import com.nect.core.entity.user.User;
import com.nect.core.repository.dm.DmRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DmService {

    private final DmRepository dmRepository;
    private final UserRepository userRepository;
    private final DmRedisPublisher dmRedisPublisher;
    private final DmPresenceRegistry dmPresenceRegistry;

    @Transactional
    public DirectMessageDto sendMessage(Long senderId, Long receiverId, String content) {

        User sender = userRepository.findById(senderId).orElseThrow(UserNotFoundException::new);
        User receiver = userRepository.findById(receiverId).orElseThrow(UserNotFoundException::new);

        String roomId = buildChannelId(senderId, receiverId);
        boolean isRead = dmPresenceRegistry.bothPresent(roomId, senderId, receiverId);

        DirectMessage message = DirectMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .isRead(isRead)
                .build();

        DirectMessage saved = dmRepository.save(message);

        DirectMessageDto dto = DirectMessageDto.builder()
                .messageId(saved.getId())
                .senderId(senderId)
                .receiverId(receiverId)
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt())
                .isRead(saved.getIsRead())
                .build();

        dmRedisPublisher.publish(roomId, dto);

        return dto;
    }

    @Transactional
    public DmMessageListResponse getMessages(Long userId, Long otherUserId, Long cursor, int size) {
        userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        userRepository.findById(otherUserId).orElseThrow(UserNotFoundException::new);

        String roomId = buildChannelId(userId, otherUserId);
        dmPresenceRegistry.enter(roomId, userId);

        dmRepository.markAsRead(userId, otherUserId, null);

        int safeSize = Math.max(1, size);

        List<DirectMessage> messages = dmRepository.findConversation(
                userId,
                otherUserId,
                cursor,
                PageRequest.of(0, safeSize)
        );

        Long nextCursor = (messages.size() == safeSize)
                ? messages.get(messages.size() - 1).getId()
                : null;

        Collections.reverse(messages);

        List<DirectMessageDto> list = messages.stream()
                .map(message -> DirectMessageDto.builder()
                        .messageId(message.getId())
                        .senderId(message.getSender().getUserId())
                        .receiverId(message.getReceiver().getUserId())
                        .content(message.getContent())
                        .createdAt(message.getCreatedAt())
                        .isRead(message.getIsRead())
                        .build())
                .toList();

        return DmMessageListResponse.builder()
                .messages(list)
                .nextCursor(nextCursor)
                .build();
    }

    @Transactional(readOnly = true)
    public DmRoomListResponse getRooms(Long userId, Long cursor, int size) {
        userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        int safeSize = Math.max(1, size);

        List<DirectMessage> latest = dmRepository.findLatestMessagesByUser(
                userId,
                cursor,
                PageRequest.of(0, safeSize)
        );

        Long nextCursor = (latest.size() == safeSize)
                ? latest.get(latest.size() - 1).getId()
                : null;

        Map<Long, Long> unreadBySender = dmRepository.countUnreadBySender(userId).stream()
                .collect(Collectors.toMap(
                        DmRepository.UnreadCountRow::getSenderId,
                        DmRepository.UnreadCountRow::getUnreadCount,
                        (a, b) -> a
                ));

        List<DmRoomSummaryDto> rooms = latest.stream()
                .map(message -> {
                    Long senderId = message.getSender().getUserId();
                    Long receiverId = message.getReceiver().getUserId();
                    Long otherUserId = userId.equals(senderId) ? receiverId : senderId;
                    long unread = unreadBySender.getOrDefault(otherUserId, 0L);
                    return DmRoomSummaryDto.builder()
                            .otherUserId(otherUserId)
                            .lastMessageId(message.getId())
                            .lastMessage(message.getContent())
                            .lastMessageAt(message.getCreatedAt())
                            .unreadCount(unread)
                            .build();
                })
                .toList();

        return DmRoomListResponse.builder()
                .rooms(rooms)
                .nextCursor(nextCursor)
                .build();
    }

    private String buildChannelId(Long senderId, Long receiverId) {
        long a = Math.min(senderId, receiverId);
        long b = Math.max(senderId, receiverId);
        return a + "_" + b;
    }

    public void leaveRoom(Long userId, Long otherUserId) {
        String roomId = buildChannelId(userId, otherUserId);
        dmPresenceRegistry.leave(roomId, userId);
    }
}
