package com.nect.api.domain.team.chat.service;

import com.nect.api.domain.team.chat.dto.res.ChatRoomLeaveResponseDTO;
import com.nect.api.domain.team.chat.dto.res.ChatRoomListDTO;
import com.nect.api.domain.team.chat.enums.ChatErrorCode;
import com.nect.api.domain.team.chat.exeption.ChatException;
import com.nect.core.entity.team.chat.ChatMessage;
import com.nect.core.entity.team.chat.ChatRoom;
import com.nect.core.entity.team.chat.ChatRoomUser;
import com.nect.core.entity.user.User;
import com.nect.core.repository.team.chat.ChatMessageRepository;
import com.nect.core.repository.team.chat.ChatRoomUserRepository;
import com.nect.core.repository.team.chat.ChatRoomRepository;
import com.nect.core.repository.team.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserRepository userRepository;
    private final ChatService chatService;


    public List<ChatRoomListDTO> getMyChatRooms(Long user_id) {

        //  내가 소속된 방 멤버 정보 다 가져오기
        List<ChatRoomUser> myMemberships = chatRoomUserRepository.findAllByUserId(user_id);

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

            return ChatRoomListDTO.builder()
                    .room_id(room.getId())
                    .room_name(room.getName())
                    .last_message(lastMessage != null ? lastMessage.getContent() : "")
                    .has_new_message(hasNewMessage)
                    .last_message_time(lastMessage != null ? lastMessage.getCreatedAt() : room.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }



    @Transactional
    public ChatRoomLeaveResponseDTO leaveChatRoom(Long roomId, Long userId) {

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));


        User user = userRepository.findById(userId) //TODO User엔티티 생성 후 수정필요
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_MEMBER_NOT_FOUND));

        // 멤버인지 확인하고 삭제
        ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED));

        chatRoomUserRepository.delete(chatRoomUser); // 방에서 내보내기

        // 나갔습니다 메시지 전송
        chatService.sendMessage(roomId, userId,user.getUsername() + "님이 채팅방을 나갔습니다.");


        //TODO 추후에 리팩토링 할 때 컨버터로 넣겠습니다.
        return ChatRoomLeaveResponseDTO.builder()
                .roomId(roomId)
                .userId(userId)
                .userName(user.getUsername())
                .message("채팅방을 나갔습니다.")
                .leftAt(LocalDateTime.now())
                .build();
    }

}
