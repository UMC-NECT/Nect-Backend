package com.nect.api.domain.team.chat.converter;

import com.nect.api.domain.team.chat.dto.req.ChatMessageDto;
import com.nect.api.domain.team.chat.dto.res.ChatFileResponseDto;
import com.nect.api.domain.team.chat.dto.res.ChatFileUploadResponseDto;
import com.nect.api.domain.team.chat.dto.res.ChatRoomAlbumDetailDto;
import com.nect.api.domain.team.chat.dto.res.ChatRoomAlbumResponseDto;
import com.nect.core.entity.team.chat.ChatFile;
import com.nect.core.entity.team.chat.ChatMessage;
import com.nect.core.entity.team.chat.ChatRoom;
import com.nect.core.entity.team.chat.enums.MessageType;
import com.nect.core.entity.user.User;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FileConverter {

    private final ChatConverter chatConverter;


    // ChatFile Entity -> ChatFileUploadResponseDTO
    public ChatFileUploadResponseDto toFileUploadResponseDTO(ChatFile chatFile) {
        return new ChatFileUploadResponseDto(
                chatFile.getId(),
                chatFile.getOriginalFileName(),
                chatFile.getFileUrl(),
                chatFile.getFileSize(),
                chatFile.getFileType()
        );
    }


    public ChatMessageDto toFileMessageDto(ChatMessage message, ChatFile chatFile) {
        // 기본 메시지 정보 변환
        ChatMessageDto dto = chatConverter.toMessageDto(message);
        //  파일 정보 추가
        dto.setFileInfo(toFileUploadResponseDTO(chatFile));
        return dto;
    }

    //파일 메시지 엔티티 생성
    public ChatMessage toFileMessage(ChatRoom chatRoom, User user) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .user(user)
                .messageType(MessageType.FILE)
                .content("파일 전송") // 클라이언트에서는 fileInfo를 보고 렌더링하므로 텍스트는 단순 안내용
                .isPinned(false)
                .build();
    }

    // ChatFile 엔티티 생성
    public ChatFile toFileEntity(
            String originalFileName,
            String storedFileName,
            String fileUrl,
            Long fileSize,
            String fileType,
            ChatRoom chatRoom
    ) {
        return ChatFile.builder()
                .originalFileName(originalFileName)
                .storedFileName(storedFileName)
                .fileUrl(fileUrl)
                .fileSize(fileSize)
                .fileType(fileType)
                .chatRoom(chatRoom)
                .build();
    }

    // 사진첩 조회를 위한 단건 DTO 변환
    public ChatFileResponseDto toFileResponseDto(ChatFile chatFile) {
        return new ChatFileResponseDto(
                chatFile.getId(),
                chatFile.getOriginalFileName(),
                chatFile.getFileUrl(),
                chatFile.getCreatedAt()
        );
    }

    // 파일 조회를 위한 리스트 DTO 변환 (스트림 로직 이동)
    public List<ChatFileResponseDto> toFileResponseDtoList(List<ChatFile> chatFiles) {
        return chatFiles.stream()
                .map(this::toFileResponseDto)
                .collect(Collectors.toList());
    }

    public ChatRoomAlbumResponseDto toChatRoomAlbumDto(
            ChatRoom room,
            List<ChatFile> files,
            int totalFileCount) {

        return new ChatRoomAlbumResponseDto(
                room.getId(),
                room.getName(),
                room.getType().name(),
                totalFileCount,
                toFileResponseDtoList(files)
        );
    }
    public ChatRoomAlbumDetailDto toChatRoomAlbumDetailDto(
            ChatRoom room,
            List<ChatFile> files,
            int totalCount,
            int currentPage,
            int totalPages,
            boolean hasNext) {

        return new ChatRoomAlbumDetailDto(
                room.getId(),
                room.getName(),
                toFileResponseDtoList(files),
                totalCount,
                currentPage,
                totalPages,
                hasNext
        );
    }

}
