package com.nect.api.domain.team.chat.service;

import com.nect.api.domain.team.chat.dto.res.ChatRoomLeaveResponseDto;
import com.nect.api.domain.team.chat.dto.res.ChatRoomListDto;
import com.nect.api.domain.team.chat.enums.ChatErrorCode;
import com.nect.api.domain.team.chat.exeption.ChatException;
import com.nect.api.global.infra.S3Service;
import com.nect.core.entity.team.chat.ChatMessage;
import com.nect.core.entity.team.chat.ChatRoom;
import com.nect.core.entity.team.chat.ChatRoomUser;
import com.nect.core.entity.team.chat.enums.MessageType;
import com.nect.core.entity.user.User;
import com.nect.core.repository.team.chat.ChatMessageRepository;
import com.nect.core.repository.team.chat.ChatRoomUserRepository;
import com.nect.core.repository.team.chat.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatService chatService;
    private final S3Service s3Service;

    public List<ChatRoomListDto> getMyChatRooms(Long user_id) {

        //  내가 소속된 방 멤버 정보 다 가져오기
        List<ChatRoomUser> myMemberships = chatRoomUserRepository.findAllByUserUserId(user_id);

        return myMemberships.stream().map(member -> {

            ChatRoom room = member.getChatRoom();
            // 마지막 메시지 조회
            ChatMessage lastMessage = chatMessageRepository.findTopByChatRoomOrderByIdDesc(room)
                    .orElse(null); // 메시지가 없으면 null

            boolean hasNewMessage = false;
            if (lastMessage != null) {

                Long lastReadId = member.getLastReadMessageId() == null ? 0L : member.getLastReadMessageId();
                if (lastMessage.getId() > lastReadId) {
                    hasNewMessage = true;
                }
            }

            return ChatRoomListDto.builder()
                    .roomId(room.getId())
                    .roomName(room.getName())
                    .lastMessage(lastMessage != null ? lastMessage.getContent() : "")
                    .hasNewMessage(hasNewMessage)
                    .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : room.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }



    @Transactional
    public ChatRoomLeaveResponseDto leaveChatRoom(Long roomId, Long userId) {

        ChatRoomUser chatRoomUser = chatRoomUserRepository
                .findMemberInRoom(roomId, userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED));

        ChatRoom chatRoom = chatRoomUser.getChatRoom();
        User user = chatRoomUser.getUser();
        String userName = user.getNickname();


        long currentMemberCount = chatRoomUserRepository.countByChatRoomId(roomId);


        if (currentMemberCount > 1) {
            chatService.sendMessage(roomId, userId, userName + "님이 채팅방을 나갔습니다.");
        }


        chatRoomUserRepository.delete(chatRoomUser);


        if (currentMemberCount == 1) {
            chatRoomRepository.delete(chatRoom);
        }

        return ChatRoomLeaveResponseDto.builder()
                .roomId(roomId)
                .userId(userId)
                .userName(userName)
                .message("채팅방을 나갔습니다.")
                .leftAt(LocalDateTime.now())
                .build();
    }



    public List<ChatRoomListDto> getProjectChatRooms(Long projectId, Long userId) {

        List<ChatRoom> chatRooms = chatRoomRepository
                .findGroupChatRoomsByProjectAndUser(projectId, userId);

        return chatRooms.stream()
                .map(room -> buildChatRoomListDto(room, userId))
                .collect(Collectors.toList());
    }


    private ChatRoomListDto buildChatRoomListDto(ChatRoom chatRoom, Long userId) {


        int memberCount = chatRoomUserRepository.countByChatRoomId(chatRoom.getId());
        
        // 프로필 4명이하 조회
        List<ChatRoomUser> members = chatRoomUserRepository.findTop4ByChatRoomId(chatRoom.getId());
        
        List<String> profileImages = members.stream()
                .map(member -> member.getUser().getProfileImageName())
                .filter(StringUtils::hasText)
                .map(s3Service::getPresignedGetUrl)
                .limit(4)
                .collect(Collectors.toList());


        ChatMessage lastMessage = chatMessageRepository
                .findTopByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId())
                .orElse(null);


        boolean hasNewMessage = checkHasNewMessage(chatRoom.getId(), userId, lastMessage);

        return ChatRoomListDto.builder()
                .roomId(chatRoom.getId())
                .roomName(chatRoom.getName())
                .memberCount(memberCount)
                .profileImages(profileImages)
                .lastMessage(getLastMessageContent(lastMessage))
                .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : null)
                .hasNewMessage(hasNewMessage)
                .build();
    }

    private List<String> getProfileImages(Long chatRoomId) {
        List<ChatRoomUser> roomUsers = chatRoomUserRepository
                .findAllByChatRoomId(chatRoomId);

        return roomUsers.stream()
                .map(ChatRoomUser::getUser)
                .map(u -> s3Service.getPresignedGetUrl(u.getProfileImageName()))
                .filter(StringUtils::hasText)
                .limit(4)
                .collect(Collectors.toList());
    }

    private boolean checkHasNewMessage(Long chatRoomId, Long userId, ChatMessage lastMessage) {
        if (lastMessage == null) {
            return false;
        }

        return chatRoomUserRepository
                .findByChatRoomIdAndUser_UserId(chatRoomId, userId)
                .map(userRoom -> {
                    Long lastReadMessageId = userRoom.getLastReadMessageId();
                    return lastReadMessageId == null || lastMessage.getId() > lastReadMessageId;
                })
                .orElse(false);
    }
    private String getLastMessageContent(ChatMessage lastMessage) {
        if (lastMessage == null) {
            return null;
        }

        if (lastMessage.getMessageType() == MessageType.FILE) {
            return " 파일을 보냈습니다.";
        }

        return lastMessage.getContent();
    }

}
