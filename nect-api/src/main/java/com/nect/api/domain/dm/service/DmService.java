package com.nect.api.domain.dm.service;

import com.nect.api.domain.dm.dto.DirectMessageDto;
import com.nect.api.domain.dm.dto.DmMessageListResponse;
import com.nect.api.domain.dm.dto.DmRoomListResponse;
import com.nect.api.domain.dm.dto.DmRoomSummaryDto;
import com.nect.api.domain.dm.infra.DmRedisPublisher;
import com.nect.api.domain.user.exception.UserNotFoundException;
import com.nect.api.global.infra.S3Service;
import com.nect.core.entity.dm.DirectMessage;
import com.nect.core.entity.user.User;
import com.nect.core.repository.dm.DmRepository;
import com.nect.core.repository.user.UserRepository;
import com.nect.core.repository.user.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DmService {

    private final DmRepository dmRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final DmRedisPublisher dmRedisPublisher;
    private final DmPresenceRegistry dmPresenceRegistry;
    private final S3Service s3Service;

    @Transactional
    public DirectMessageDto sendMessage(Long senderId, Long receiverId, String content) {

        // 검증
        User sender = userRepository.findById(senderId).orElseThrow(UserNotFoundException::new);
        User receiver = userRepository.findById(receiverId).orElseThrow(UserNotFoundException::new);

        // 채팅방 정보
        String roomId = buildChannelId(senderId, receiverId);
        boolean isRead = dmPresenceRegistry.bothPresent(roomId, senderId, receiverId);

        // DM 생성
        DirectMessage message = DirectMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .isRead(isRead)
                .build();

        // DM 저장
        DirectMessage saved = dmRepository.save(message);

        // DM -> DTO
        DirectMessageDto dto = DirectMessageDto.fromDm(saved);
        dto.setImageUrl(s3Service.getPresignedGetUrl(saved.getSender().getProfileImageName()));

        // 상대에게 전송
        dmRedisPublisher.publish(roomId, dto);

        return dto;
    }

    @Transactional
    public DmMessageListResponse getMessages(Long userId, Long otherUserId, Long cursor, int size) {

        // 검증
        userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        userRepository.findById(otherUserId).orElseThrow(UserNotFoundException::new);

        // 채팅방 정보
        String roomId = buildChannelId(userId, otherUserId);
        dmPresenceRegistry.enter(roomId, userId);

        // 채팅방 채팅 읽음처리
        dmRepository.markAsRead(userId, otherUserId, null);

        int safeSize = Math.max(1, size);

        // 채팅 조회
        List<DirectMessage> messages = dmRepository.findConversation(
                userId,
                otherUserId,
                cursor,
                PageRequest.of(0, safeSize)
        );

        // 커서 정보
        Long nextCursor = (messages.size() == safeSize)
                ? messages.getLast().getId()
                : null;

        // 정렬
        Collections.reverse(messages);

        // DM -> Dto
        List<DirectMessageDto> list = messages.stream()
                .map(DirectMessageDto::fromDm)
                .toList();

        // 응답값 생성 후 반환
        return DmMessageListResponse.builder()
                .messages(list)
                .nextCursor(nextCursor)
                .build();
    }

    @Transactional(readOnly = true)
    public DmRoomListResponse getRooms(Long userId, Long cursor, int size) {

        // 검증
        userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        int safeSize = Math.max(1, size);

        // 각 개인 채팅에 대한 마지막 메시지
        List<DirectMessage> latest = dmRepository.findLatestMessagesByUser(userId, cursor, PageRequest.of(0, safeSize));

        // 채팅룸 커서 정보
        Long nextCursor = (latest.size() == safeSize) ? latest.getLast().getId() : null;

        // List<DM> -> List<DTO>
        List<DmRoomSummaryDto> messages = latest.stream()
                .map(message -> {
                    DmRoomSummaryDto dto = DmRoomSummaryDto.fromOtherUser(userId, message);
                    dto.setImageUrl(s3Service.getPresignedGetUrl(message.getSender().getProfileImageName()));
                    return dto;
                })
                .toList();

        // 응답값 생성 후 반환
        return DmRoomListResponse.builder()
                .messages(messages)
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
