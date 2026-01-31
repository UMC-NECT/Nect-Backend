package com.nect.api.domain.team.chat.converter;

import com.nect.api.domain.team.chat.dto.req.ChatMessageDto;
import com.nect.api.domain.team.chat.dto.res.ChatFileResponseDto;
import com.nect.api.domain.team.chat.dto.res.ChatFileUploadResponseDto;
import com.nect.api.domain.team.chat.dto.res.ChatRoomAlbumResponseDto;
import com.nect.core.entity.team.chat.ChatFile;
import com.nect.core.entity.team.chat.ChatMessage;
import com.nect.core.entity.team.chat.ChatRoom;
import com.nect.core.entity.team.chat.enums.MessageType;
import com.nect.core.entity.user.User;

import java.util.List;
import java.util.stream.Collectors;

import static com.nect.api.domain.team.chat.converter.ChatConverter.toMessageDto;

public class FileConverter {



// ChatFile Entity -> ChatFileUploadResponseDTO
    public static ChatFileUploadResponseDto toFileUploadResponseDTO(ChatFile chatFile) {
        return ChatFileUploadResponseDto.builder()
                .fileId(chatFile.getId())
                .fileName(chatFile.getFileName())
                .fileUrl(chatFile.getFileUrl())
                .fileSize(chatFile.getFileSize())
                .fileType(chatFile.getFileType())
                .build();
    }


    public static ChatMessageDto toFileMessageDto(ChatMessage message, ChatFile chatFile) {
        // 기본 메시지 정보 변환
        ChatMessageDto dto = toMessageDto(message);
        //  파일 정보 추가
        dto.setFileInfo(FileConverter.toFileUploadResponseDTO(chatFile));
        return dto;
    }

    //파일 메시지 엔티티 생성
    public static ChatMessage toFileMessage(ChatRoom chatRoom, User user) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .user(user)
                .messageType(MessageType.FILE)
                .content("파일 전송") // 클라이언트에서는 fileInfo를 보고 렌더링하므로 텍스트는 단순 안내용
                .isPinned(false)
                .build();
    }

    // ChatFile 엔티티 생성
    public static ChatFile toFileEntity(String fileName, String fileUrl, Long fileSize, String fileType, ChatRoom chatRoom) {
        return ChatFile.builder()
                .fileName(fileName)
                .fileUrl(fileUrl)
                .fileSize(fileSize)
                .fileType(fileType)
                .chatRoom(chatRoom)
                .chatMessage(null)
                .build();
    }

    // 사진첩 조회를 위한 단건 DTO 변환
    public static ChatFileResponseDto toFileResponseDto(ChatFile chatFile) {
        return ChatFileResponseDto.builder()
                .fileName(chatFile.getFileName())
                .fileUrl(chatFile.getFileUrl())
                .createdAt(chatFile.getCreatedAt())
                .build();
    }

    // 파일 조회를 위한 리스트 DTO 변환 (스트림 로직 이동)
    public static List<ChatFileResponseDto> toFileResponseDtoList(List<ChatFile> chatFiles) {
        return chatFiles.stream()
                .map(FileConverter::toFileResponseDto)
                .collect(Collectors.toList());
    }

    public static ChatRoomAlbumResponseDto toChatRoomAlbumDto(ChatRoom room, List<ChatFile> chatFiles) {
        return ChatRoomAlbumResponseDto.builder()
                .roomId(room.getId())
                .roomName(room.getName())
                .files(toFileResponseDtoList(chatFiles))
                .build();
    }

}
