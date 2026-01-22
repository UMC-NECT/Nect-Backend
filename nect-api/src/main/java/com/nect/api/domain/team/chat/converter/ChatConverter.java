package com.nect.api.domain.team.chat.converter;

import com.nect.api.domain.team.chat.dto.req.ChatMessageDTO;
import com.nect.api.domain.team.chat.dto.req.ChatRoomDTO;
import com.nect.api.domain.team.chat.dto.res.ChatNoticeResponseDTO;
import com.nect.api.domain.team.chat.dto.res.ChatRoomResponseDTO;
import com.nect.api.domain.team.chat.dto.res.ProjectMemberResponseDTO;
import com.nect.core.entity.team.chat.ChatMessage;
import com.nect.core.entity.team.chat.ChatRoom;
import com.nect.core.entity.team.chat.ChatRoomUser;
import com.nect.core.entity.user.User;
import com.nect.core.entity.team.chat.enums.ChatRoomType;
import com.nect.core.entity.team.chat.enums.MessageType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


public class ChatConverter {



    //ChatMessage  -> ChatMessageDto
    public static ChatMessageDTO toMessageDto(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setMessageId(message.getId());
        dto.setRoomId(message.getChatRoom().getId());

        dto.setUserName("사용자" + message.getUser().getId());  // TODO: User 조회
        dto.setContent(message.getContent());
        dto.setMessageType(message.getMessageType());
        dto.setIsPinned(message.getIsPinned());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }

    //DTO -> ChatMessage
    public static ChatMessage toMessage(ChatMessageDTO dto,User user, ChatRoom chatRoom) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .user(user)
                .content(dto.getContent())
                .messageType(dto.getMessageType())
                .isPinned(dto.getIsPinned())
                .build();
    }

    //텍스트 메시지 생성
    public static ChatMessage toTextMessage(ChatRoom chatRoom, User user, String content) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .user(user)
                .content(content)
                .messageType(MessageType.TEXT)
                .isPinned(false)
                .build();
    }

    //ChatRoom 변환
    //ChatRoom-> DTO
    public static ChatRoomDTO toRoomDto(ChatRoom room) {
        ChatRoomDTO dto = new ChatRoomDTO();
        dto.setRoomId(room.getId());
        dto.setProjectId(room.getProjectId());
        dto.setName(room.getName());
        dto.setType(room.getType());
        dto.setCreatedAt(room.getCreatedAt());
        return dto;
    }
    //ChatRoom -> >DTO(멤버포함)
    public static ChatRoomDTO toRoomDto(ChatRoom room, List<ChatRoomUser> members) {
        ChatRoomDTO dto = toRoomDto(room);

        // ChatRoomMember에서 userId 추출
        List<Long> userIds = members.stream()
                .map(member -> member.getUser().getId())
                .collect(Collectors.toList());

        dto.setUserIds(userIds);
        return dto;
    }

    public static ChatRoom toChatRoomEntity(Long projectId, String roomName, ChatRoomType type) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setProjectId(projectId);
        chatRoom.setName(roomName);
        chatRoom.setType(type);
        return chatRoom;
    }

    public static ChatRoomUser toChatRoomMemberEntity(ChatRoom chatRoom, User user, LocalDateTime lastReadAt) {
        ChatRoomUser member = new ChatRoomUser();
        member.setChatRoom(chatRoom);
        member.setUser(user);

        member.setLastReadAt(lastReadAt);

        member.setLastReadMessageId(0L);
        return member;
    }

    public static ChatRoomResponseDTO toResponseDTO(ChatRoom chatRoom, User targetUser) {

        String roomName = chatRoom.getName();
        String profileImage = null;


        if (chatRoom.getType() == ChatRoomType.DIRECT && targetUser != null) {
            roomName = targetUser.getUsername();
            // profileImage = targetUser.getProfileImage(); // TODO: 나중에 프로필 이미지 생기면 추가
        }

        return ChatRoomResponseDTO.builder()
                .roomId(chatRoom.getId())
                .projectId(chatRoom.getProjectId())
                .roomName(roomName)
                .roomType(chatRoom.getType())
                .profileImage(profileImage)
                .createdAt(chatRoom.getCreatedAt() != null ? chatRoom.getCreatedAt() : LocalDateTime.now())
                .build();
    }

    public static ProjectMemberResponseDTO toProjectMemberResponseDTO(User user) {
        return ProjectMemberResponseDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }

    public static List<ProjectMemberResponseDTO> toProjectMemberResponseDTOList(List<User> users) {
        return users.stream()
                .map(ChatConverter::toProjectMemberResponseDTO)
                .collect(Collectors.toList());
    }


    //ChatMessage -> ChatNoticeResponseDTO
    public static ChatNoticeResponseDTO toNoticeResponseDTO(ChatMessage message) {
        return ChatNoticeResponseDTO.builder()
                .messageId(message.getId())
                .roomId(message.getChatRoom().getId())
                .content(message.getContent())
                .messageType(message.getMessageType())
                // User가 null일 경우 대비 (시스템 메시지 등)
                .senderName(message.getUser() != null ? message.getUser().getUsername() : "알 수 없음")
                .isPinned(message.getIsPinned())
                .registeredAt(LocalDateTime.now())
                .build();
    }



}
