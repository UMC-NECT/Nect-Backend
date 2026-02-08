package com.nect.api.domain.team.chat.service;
import com.nect.api.domain.team.chat.converter.FileConverter;
import com.nect.api.domain.team.chat.dto.req.ChatMessageDto;
import com.nect.api.domain.team.chat.dto.res.*;
import com.nect.api.domain.team.chat.enums.ChatErrorCode;
import com.nect.api.domain.team.chat.exeption.ChatException;
import com.nect.api.domain.team.chat.util.FileValidator;
import com.nect.api.domain.team.file.enums.FileErrorCode;
import com.nect.api.domain.team.file.exception.FileException;
import com.nect.api.domain.team.workspace.enums.BoardsErrorCode;
import com.nect.api.domain.team.workspace.exception.BoardsException;
import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.code.StorageErrorCode;
import com.nect.api.global.infra.S3Service;
import com.nect.api.global.infra.exception.StorageException;
import com.nect.api.global.infra.redis.RedisPublisher;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectUser;
import com.nect.core.entity.team.SharedDocument;
import com.nect.core.entity.team.chat.ChatFile;
import com.nect.core.entity.team.chat.ChatMessage;
import com.nect.core.entity.team.chat.ChatRoom;
import com.nect.core.entity.team.chat.ChatRoomUser;
import com.nect.core.entity.team.enums.FileExt;
import com.nect.core.entity.user.User;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.SharedDocumentRepository;
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
import org.springframework.util.StringUtils;
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
    private final RedisPublisher redisPublisher;
    private final S3Service s3Service;
    private final ProjectUserRepository projectUserRepository;
    private final ProjectRepository projectRepository;
    private final SharedDocumentRepository sharedDocumentRepository;


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
            String fileUrl = getSafePresignedUrl(storedFileName);

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
            redisPublisher.publish(channel, messageDto);

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
                    String newUrl = getSafePresignedUrl(file.getStoredFileName());

                    if (newUrl != null) {
                        file.updateFileUrl(newUrl);
                    }
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChatFileDetailDto getFileDetail(Long fileId,Long userId) {

        ChatFile chatFile = chatFileRepository.findById(fileId)
                .orElseThrow(() -> new StorageException(StorageErrorCode.FILE_NOT_FOUND));
        validateRoomMember(chatFile.getChatRoom().getId(), userId);

        String viewUrl = getSafePresignedUrl(chatFile.getStoredFileName());

        return ChatFileDetailDto.builder()
                .fileId(chatFile.getId())
                .fileName(chatFile.getOriginalFileName())
                .fileUrl(viewUrl)
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

        String downloadUrl = getSafePresignedUrl(chatFile.getStoredFileName());
        if (downloadUrl == null) {
            throw new StorageException(StorageErrorCode.FILE_NOT_FOUND);
        }

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
    private String getSafePresignedUrl(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return null;
        }
        return s3Service.getPresignedGetUrl(fileName);
    }

    @Transactional
    public SharedDocumentCreateResDto createFromChatFile(Long projectId, Long roomId,Long userId, Long chatFileId) {

        ProjectUser projectUser = projectUserRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new BoardsException(BoardsErrorCode.PROJECT_MEMBER_FORBIDDEN));


        User registrar = userRepository.findById(userId)
                .orElseThrow(() -> new BoardsException(BoardsErrorCode.USER_NOT_FOUND));

        Project project = projectUser.getProject();

        ChatFile chatFile = chatFileRepository.findById(chatFileId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_FILE_NOT_FOUND));

        if (!chatFile.getChatRoom().getId().equals(roomId)) {
            throw new ChatException(ChatErrorCode.CHAT_FILE_NOT_FOUND);
        }

        FileExt ext = extractFileExt(chatFile.getOriginalFileName());

        SharedDocument sharedDocument = SharedDocument.ofFile(
                registrar,
                project,
                chatFile.getOriginalFileName(),
                chatFile.getOriginalFileName(),
                ext,
                chatFile.getStoredFileName(),
                chatFile.getFileSize()
        );

        SharedDocument savedDoc = sharedDocumentRepository.save(sharedDocument);

        return new SharedDocumentCreateResDto(
                savedDoc.getId(),
                savedDoc.getTitle(),
                savedDoc.getDocumentType()
        );
    }

    private FileExt extractFileExt(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new FileException(FileErrorCode.UNSUPPORTED_FILE_EXT, "확장자가 없는 파일입니다. fileName=" + fileName);
        }

        String extStr = fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();

        try {
            return FileExt.valueOf(extStr);
        } catch (IllegalArgumentException e) {
            throw new FileException(FileErrorCode.UNSUPPORTED_FILE_EXT, "지원하지 않는 파일 확장자입니다. fileName=" + fileName);
        }
    }
}
