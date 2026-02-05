package com.nect.api.domain.team.chat.service;
import com.nect.api.domain.team.chat.converter.FileConverter;
import com.nect.api.domain.team.chat.dto.req.ChatMessageDto;
import com.nect.api.domain.team.chat.dto.res.ChatFileResponseDto;
import com.nect.api.domain.team.chat.dto.res.ChatFileUploadResponseDto;
import com.nect.api.domain.team.chat.dto.res.ChatRoomAlbumResponseDto;
import com.nect.api.domain.team.chat.infra.ChatRedisPublisher;
import com.nect.core.entity.team.chat.ChatFile;
import com.nect.core.entity.team.chat.ChatMessage;
import com.nect.core.entity.team.chat.ChatRoom;
import com.nect.core.entity.team.chat.ChatRoomUser;
import com.nect.core.entity.user.User;
import com.nect.core.repository.team.chat.ChatFileRepository;
import com.nect.core.repository.team.chat.ChatMessageRepository;
import com.nect.core.repository.team.chat.ChatRoomRepository;
import com.nect.core.repository.team.chat.ChatRoomUserRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatFileService {

    private final ChatFileRepository chatFileRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ChatRedisPublisher redisPublisher;

    //application.yml에 설정 x  ./uploads/'에 저장 (상대 경로)
    @Value("${file.upload-dir:${user.home}/nect-uploads/}")
    private String uploadDir;

    @Transactional
    public ChatFileUploadResponseDto uploadFile(Long roomId,MultipartFile file) {

        if (file.isEmpty()) {
            throw new RuntimeException("업로드할 파일이 존재하지 않습니다.");
        }

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 채팅방입니다. ID: " + roomId));

        try{
            // 원본 파일명
            String originalFilename = file.getOriginalFilename();
            //UUID 파일명
            String storeFileName = createStoreFileName(originalFilename);

            // 저장할 디렉토리 생성
            File directory = new File(uploadDir);

            if (!directory.exists()) {
                //uploads 폴더 없을 때 생성 //TODO 파일경로 수정필요
                boolean created = directory.mkdirs(); // 폴더가 없으면 자동 생성
                if (!created) {
                }
            }

            //전체 경로
            String fullPath = new File(uploadDir, storeFileName).getPath();
            // 로컬 스토리지에 실제 파일 저장
            file.transferTo(new File(fullPath));
            // 웹 접근 URL
            String fileUrl = "/files/" + storeFileName;
            //엔티티 생성
            ChatFile chatFile = FileConverter.toFileEntity(
                    originalFilename,
                    fileUrl,
                    file.getSize(),
                    file.getContentType(),
                    chatRoom
            );

            chatFileRepository.save(chatFile);
            return FileConverter.toFileUploadResponseDTO(chatFile);
        }catch(IOException e){
            log.error("파일 저장 실제 에러: {}", e.getMessage(), e);
            throw new RuntimeException("파일 저장에 실패했습니다.");
        }
    }

    @Transactional
    public ChatMessageDto sendFileMessage(Long roomId, Long userId, Long fileId) {


        ChatRoomUser chatRoomUser = chatRoomUserRepository.findMemberInRoom(roomId, userId)
                .orElseThrow(() -> new RuntimeException("해당 채팅방에 참여 중인 사용자가 아닙니다."));

        ChatRoom chatRoom = chatRoomUser.getChatRoom();
        User user = chatRoomUser.getUser();

        ChatFile chatFile = chatFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일이 존재하지 않습니다. ID: " + fileId));

        //메시지 엔티티
        ChatMessage message = FileConverter.toFileMessage(
                chatRoomUser.getChatRoom(),
                chatRoomUser.getUser()
        );

        chatMessageRepository.save(message);

        // 파일과 메시지 연결
        chatFile.setChatMessage(message);
        ChatMessageDto messageDto = FileConverter.toFileMessageDto(message, chatFile);
       
        //Redis 발행
        redisPublisher.publish(roomId, messageDto);
        return messageDto;

    }


    // UUID를 붙여서 중복되지 않는 파일명 생성
    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        // 확장자가 있으면 붙이고, 없으면 UUID만 반환
        return ext.isEmpty() ? uuid : uuid + "." + ext;
    }

    // 파일명에서 확장자 추출
    private String extractExt(String originalFilename) {
        if (originalFilename == null) return "";
        int pos = originalFilename.lastIndexOf(".");
        if (pos == -1) return "";
        return originalFilename.substring(pos + 1);
    }

    //  채팅방 파일 조회
    @Transactional(readOnly = true)
    public List<ChatRoomAlbumResponseDto> getChatAlbum(Long projectId) {
        LocalDateTime fifteenDaysAgo = LocalDateTime.now().minusDays(15);

        List<ChatRoom> chatRooms = chatRoomRepository.findAllByProject_Id(projectId);

        return chatRooms.stream()
                .map(room -> {
                    List<ChatFile> chatFiles = chatFileRepository
                            .findAllByChatRoomIdAndCreatedAtAfterOrderByCreatedAtDesc(room.getId(), fifteenDaysAgo);

                    return FileConverter.toChatRoomAlbumDto(room, chatFiles);
                })
                .collect(Collectors.toList());
    }

    // 만료된 파일 물리 삭제 롤직
    @Transactional
    public void cleanupExpiredFiles() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(15);
        List<ChatFile> expiredFiles = chatFileRepository.findAllByCreatedAtBefore(threshold);

        for (ChatFile file : expiredFiles) {
            try {
                // TODO: Cloudflare R2 실제 파일 삭제 로직 연결
                // s3Client.deleteObject(bucketName, file.getFileName());

                chatFileRepository.delete(file);
                log.info("만료 파일 삭제 완료: {}", file.getFileName());
            } catch (Exception e) {
                log.error("파일 삭제 실패: {}", file.getFileName(), e);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<ChatFileResponseDto> getChatRoomDetailAlbum(Long roomId) {
        LocalDateTime fifteenDaysAgo = LocalDateTime.now().minusDays(15);

        List<ChatFile> chatFiles = chatFileRepository
                .findAllByChatRoomIdAndCreatedAtAfterOrderByCreatedAtDesc(roomId, fifteenDaysAgo);

        return FileConverter.toFileResponseDtoList(chatFiles);
    }

}
