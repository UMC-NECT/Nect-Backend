package com.nect.api.domain.team.chat.service;
import com.nect.api.domain.team.chat.converter.FileConverter;
import com.nect.api.domain.team.chat.dto.req.ChatMessageDto;
import com.nect.api.domain.team.chat.dto.res.*;
import com.nect.api.domain.team.chat.infra.ChatRedisPublisher;
import com.nect.api.domain.team.chat.util.FileValidator;
import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.code.StorageErrorCode;
import com.nect.api.global.infra.S3Service;
import com.nect.api.global.infra.exception.StorageException;
import com.nect.core.entity.team.chat.ChatFile;
import com.nect.core.entity.team.chat.ChatMessage;
import com.nect.core.entity.team.chat.ChatRoom;
import com.nect.core.entity.team.chat.ChatRoomUser;
import com.nect.core.entity.user.User;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.chat.ChatFileRepository;
import com.nect.core.repository.team.chat.ChatMessageRepository;
import com.nect.core.repository.team.chat.ChatRoomRepository;
import com.nect.core.repository.team.chat.ChatRoomUserRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.io.IOException;
import java.util.List;

import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ChatFileService {

    private final ChatFileRepository chatFileRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ChatRedisPublisher redisPublisher;
    private final S3Service s3Service;
    private final ProjectUserRepository projectUserRepository;


    private String uploadDir;

    @Transactional
    public ChatMessageDto uploadAndSendFile(Long roomId, MultipartFile file, Long userId) {

        ChatRoomUser chatRoomUser = chatRoomUserRepository.findMemberInRoom(roomId, userId)
                .orElseThrow(() -> new StorageException(StorageErrorCode.NOT_CHAT_ROOM_MEMBER));

        ChatRoom chatRoom = chatRoomUser.getChatRoom();
        User user = chatRoomUser.getUser();


        FileValidator.validateFile(file);

        try {
            String storedFileName = s3Service.uploadFile(file);
            String fileUrl = s3Service.getPresignedGetUrl(storedFileName);

            ChatMessage message = FileConverter.toFileMessage(chatRoom, user);
            chatMessageRepository.save(message);

            ChatFile chatFile = FileConverter.toFileEntity(
                    file.getOriginalFilename(),
                    storedFileName,
                    fileUrl,
                    file.getSize(),
                    file.getContentType(),
                    chatRoom
            );
            chatFile.setChatMessage(message);
            chatFileRepository.save(chatFile);

            chatRoomUser.setLastReadMessageId(message.getId());
            chatRoomUser.setLastReadAt(LocalDateTime.now());

            // 7. DTO 변환
            ChatMessageDto messageDto = FileConverter.toFileMessageDto(message, chatFile);

            int totalMembers = chatRoomUserRepository.countByChatRoomId(roomId);
            messageDto.setReadCount(totalMembers - 1);

            String channel = "chatroom:" + roomId;
            redisPublisher.publish(roomId, messageDto);

            return messageDto;

        } catch (IOException e) {
            throw new StorageException(StorageErrorCode.FILE_UPLOAD_FAILED);
        }
    }


    @Transactional
    public void deleteFile(Long fileId, Long userId) {
        ChatFile chatFile = chatFileRepository.findById(fileId)
                .orElseThrow(() -> new StorageException(StorageErrorCode.FILE_NOT_FOUND));
        validateRoomMember(chatFile.getChatRoom().getId(), userId);
        s3Service.deleteByFileName(chatFile.getStoredFileName());
        chatFileRepository.delete(chatFile);

    }


    @Transactional(readOnly = true)
    public List<ChatRoomAlbumResponseDto> getChatAlbum(Long projectId, int limitPerRoom,Long userId) {

        validateProjectMember(projectId, userId);

        LocalDateTime fifteenDaysAgo = LocalDateTime.now().minusDays(15);

        List<ChatRoom> chatRooms = chatRoomRepository.findAllByProject_Id(projectId);

        return chatRooms.stream()
                .map(room -> {

                    int totalFileCount = chatFileRepository
                            .countImageFilesByChatRoomIdAndCreatedAtAfter(
                                    room.getId(), fifteenDaysAgo);


                    List<ChatFile> chatFiles = chatFileRepository
                            .findImageFilesByChatRoomIdAndCreatedAtAfter(
                                    room.getId(),
                                    fifteenDaysAgo,
                                    PageRequest.of(0, limitPerRoom));


                    List<ChatFile> filesWithRefreshedUrls = refreshPresignedUrls(chatFiles);

                    return FileConverter.toChatRoomAlbumDto(
                            room,
                            filesWithRefreshedUrls,
                            totalFileCount);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChatRoomAlbumDetailDto getChatRoomAlbumDetail(Long roomId, int page, int size, Long userId) {
validateRoomMember(roomId, userId);

        LocalDateTime fifteenDaysAgo = LocalDateTime.now().minusDays(15);


        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new StorageException(StorageErrorCode.CHAT_ROOM_NOT_FOUND));


        int totalCount = chatFileRepository
                .countImageFilesByChatRoomIdAndCreatedAtAfter(roomId, fifteenDaysAgo);

        Pageable pageable = PageRequest.of(page, size);

        List<ChatFile> chatFiles = chatFileRepository
                .findImageFilesByChatRoomIdAndCreatedAtAfter(
                        roomId, fifteenDaysAgo, pageable);


        List<ChatFile> filesWithRefreshedUrls = refreshPresignedUrls(chatFiles);


        int totalPages = (int) Math.ceil((double) totalCount / size);
        boolean hasNext = page < totalPages - 1;

        return FileConverter.toChatRoomAlbumDetailDto(
                chatRoom,
                filesWithRefreshedUrls,
                totalCount,
                page,
                totalPages,
                hasNext
        );
    }

    private List<ChatFile> refreshPresignedUrls(List<ChatFile> chatFiles) {
        return chatFiles.stream()
                .peek(file -> {
                    try {

                        String newUrl = s3Service.getPresignedGetUrl(file.getStoredFileName());
                        file.updateFileUrl(newUrl);
                    } catch (Exception e) {

                    }
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChatFileDetailDto getFileDetail(Long fileId,Long userId) {

        ChatFile chatFile = chatFileRepository.findById(fileId)
                .orElseThrow(() -> new StorageException(StorageErrorCode.FILE_NOT_FOUND));
        validateRoomMember(chatFile.getChatRoom().getId(), userId);
        String viewUrl = s3Service.getPresignedGetUrl(chatFile.getStoredFileName());


        return ChatFileDetailDto.builder()
                .fileId(chatFile.getId())
                .fileName(chatFile.getOriginalFileName())
                .fileUrl(viewUrl)  // 이미지 표시용
                .fileSize(chatFile.getFileSize())
                .fileType(chatFile.getFileType())
                .createdAt(chatFile.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public String getDownloadUrl(Long fileId,Long userId) {

        ChatFile chatFile = chatFileRepository.findById(fileId)
                .orElseThrow(() -> new StorageException(StorageErrorCode.FILE_NOT_FOUND));
        validateRoomMember(chatFile.getChatRoom().getId(), userId);
        String downloadUrl = s3Service.getPresignedGetUrl(chatFile.getStoredFileName());


        return downloadUrl;
    }

    private void validateProjectMember(Long projectId, Long userId) {
        boolean isMember = projectUserRepository.existsByProjectIdAndUserId(projectId, userId);
        if (!isMember) {
            throw new StorageException(UserErrorCode.USER_NOT_FOUND);
        }
    }

    private void validateRoomMember(Long roomId, Long userId) {
        boolean isMember = chatRoomUserRepository.existsByChatRoomIdAndUserUserId(roomId, userId);
        if (!isMember) {
            throw new StorageException(StorageErrorCode.NOT_CHAT_ROOM_MEMBER);
        }
    }

}
