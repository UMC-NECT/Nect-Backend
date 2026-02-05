package com.nect.api.domain.team.chat.service;

import com.nect.api.domain.team.chat.converter.ChatConverter;
import com.nect.api.domain.team.chat.dto.res.ChatRoomLeaveResponseDto;
import com.nect.api.domain.team.chat.dto.res.ChatRoomListDto;
import com.nect.api.domain.team.chat.enums.ChatErrorCode;
import com.nect.api.domain.team.chat.exeption.ChatException;
import com.nect.core.entity.team.chat.ChatMessage;
import com.nect.core.entity.team.chat.ChatRoom;
import com.nect.core.entity.team.chat.ChatRoomUser;
import com.nect.core.entity.team.chat.enums.MessageType;
import com.nect.core.entity.user.User;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.chat.ChatMessageRepository;
import com.nect.core.repository.team.chat.ChatRoomUserRepository;
import com.nect.core.repository.team.chat.ChatRoomRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
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
    private final ProjectUserRepository projectUserRepository;
    private final UserRepository userRepository;
    private final ChatService chatService;
    private  final ChatConverter chatConverter;

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

        //  TODO: 프로필 이미지 (User 엔티티에 profileImage 필드 추가 후 활성화)
        List<String> profileImages = getProfileImages(chatRoom.getId());


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
    /**
     * TODO: 프로필 이미지 목록 조회
     */
    private List<String> getProfileImages(Long chatRoomId) {
        // TODO: User 엔티티에 profileImage 필드 추가 후 주석 해제
        return Collections.emptyList();

    /*
    List<ChatRoomUser> roomUsers = chatRoomUserRepository
            .findAllByChatRoomId(chatRoomId);

    return roomUsers.stream()
            .map(ChatRoomUser::getUser)
            .map(User::getProfileImage)
            .filter(StringUtils::hasText)  // null, 빈 문자열 필터링
            .limit(4)
            .collect(Collectors.toList());
    */
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
