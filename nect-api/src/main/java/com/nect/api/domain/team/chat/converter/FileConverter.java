package com.nect.api.domain.team.chat.converter;

import com.nect.api.domain.team.chat.dto.req.ChatMessageDTO;
import com.nect.api.domain.team.chat.dto.res.ChatFileUploadResponseDTO;
import com.nect.core.entity.team.chat.ChatFile;
import com.nect.core.entity.team.chat.ChatMessage;
import com.nect.core.entity.team.chat.ChatRoom;
import com.nect.core.entity.team.chat.enums.MessageType;
import com.nect.core.entity.user.User;

import static com.nect.api.domain.team.chat.converter.ChatConverter.toMessageDto;

public class FileConverter {

// ChatFile 엔티티 생성
    public static ChatFile toFileEntity(String fileName, String fileUrl, Long fileSize, String fileType) {
        return ChatFile.builder()
                .fileName(fileName)
                .fileUrl(fileUrl)
                .fileSize(fileSize)
                .fileType(fileType)
                .chatMessage(null) // 업로드 시점에는 아직 메시지와 연결되지 않음
                .build();
    }


// ChatFile Entity -> ChatFileUploadResponseDTO
    public static ChatFileUploadResponseDTO toFileUploadResponseDTO(ChatFile chatFile) {
        return ChatFileUploadResponseDTO.builder()
                .fileId(chatFile.getId())
                .fileName(chatFile.getFileName())
                .fileUrl(chatFile.getFileUrl())
                .fileSize(chatFile.getFileSize())
                .fileType(chatFile.getFileType())
                .build();
    }


    public static ChatMessageDTO toFileMessageDto(ChatMessage message, ChatFile chatFile) {
        // 기본 메시지 정보 변환
        ChatMessageDTO dto = toMessageDto(message);
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


}
