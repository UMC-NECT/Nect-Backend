package com.nect.api.domain.team.chat.service;

import com.nect.api.domain.team.chat.converter.ChatConverter;
import com.nect.api.domain.team.chat.dto.req.ChatRoomCreateRequestDTO;
import com.nect.api.domain.team.chat.dto.req.GroupChatRoomCreateRequestDTO;
import com.nect.api.domain.team.chat.dto.res.ChatRoomResponseDTO;
import com.nect.api.domain.team.chat.dto.res.ProjectMemberResponseDTO;
import com.nect.api.domain.team.chat.enums.ChatErrorCode;
import com.nect.api.domain.team.chat.exeption.ChatException;
import com.nect.core.entity.team.chat.ChatRoom;
import com.nect.core.entity.team.chat.ChatRoomUser;
import com.nect.core.entity.user.User;
import com.nect.core.entity.team.chat.enums.ChatRoomType;
import com.nect.core.repository.team.chat.ChatRoomUserRepository;
import com.nect.core.repository.team.chat.ChatRoomRepository;
import com.nect.core.repository.team.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // 추가

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final UserRepository userRepository;
    //TODO 프로젝트 관련 엔티티에 따라 수정
    public List<ProjectMemberResponseDTO>getProjectMembers(Long projectId){
        List<User> members = userRepository.findAllByProjectId(projectId);
        return ChatConverter.toProjectMemberResponseDTOList(members);
    }


    // 1:1 채팅방 생성
    @Transactional
    public ChatRoomResponseDTO createOneOnOneChatRoom(Long currentUserId, ChatRoomCreateRequestDTO request) {

        User me = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_FOUND, "현재 사용자를 찾을 수 없습니다."));

        User targetUser = userRepository.findById(request.getTarget_user_id())
                .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_FOUND, "상대방 사용자를 찾을 수 없습니다."));


        Optional<Long> existingRoomId = chatRoomRepository.findExistingOneOnOneRoomId(
                request.getProject_id(),
                currentUserId,
                request.getTarget_user_id()
        );
        // 방 존재 시 -> 해당 방 조회
        if (existingRoomId.isPresent()) {
            ChatRoom existingRoom = chatRoomRepository.findById(existingRoomId.get())
                    .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND, "존재하는 채팅방 ID를 찾을 수 없습니다."));

            return ChatConverter.toResponseDTO(existingRoom, targetUser);
        }

     if (me.getProjectId() != request.getProject_id() || targetUser.getProjectId() != request.getProject_id()) {
            throw new ChatException(ChatErrorCode.CHAT_MEMBER_NOT_FOUND, "해당 프로젝트의 팀원이 아닙니다.");
        }

        ChatRoom chatRoom = ChatConverter.toChatRoomEntity(
                request.getProject_id(),
                null, // 1:1은 이름 없음 (프론트에서 상대방 이름 렌더링)
                ChatRoomType.DIRECT
        );

        chatRoomRepository.save(chatRoom);

        ChatRoomUser myMember = ChatConverter.toChatRoomMemberEntity(chatRoom, me, LocalDateTime.now());
        ChatRoomUser targetMember = ChatConverter.toChatRoomMemberEntity(chatRoom, targetUser, null);

        chatRoomUserRepository.saveAll(List.of(myMember, targetMember));

        return ChatConverter.toResponseDTO(chatRoom, targetUser);
    }

    // 팀 채팅방 생성
    @Transactional
    public ChatRoomResponseDTO createGroupChatRoom(Long currentUserId, GroupChatRoomCreateRequestDTO request) {


        if (!StringUtils.hasText(request.getRoomName())) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_ALREADY_EXISTS, "채팅방 이름을 입력해야 합니다."); // 적절한 에러코드로 변경 필요 (예: INVALID_INPUT)
        }


        User me = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_FOUND, "현재 사용자를 찾을 수 없습니다."));

        List<User> targetUsers = userRepository.findAllById(request.getTargetUserIds());
        if (targetUsers.size() != request.getTargetUserIds().size()) {
            throw new ChatException(ChatErrorCode.USER_NOT_FOUND, "요청한 사용자 중 일부를 찾을 수 없습니다.");
        }

        for (User user : targetUsers) {
            if (user.getProjectId() != request.getProjectId()) {
                throw new ChatException(ChatErrorCode.CHAT_MEMBER_NOT_FOUND, "다른 프로젝트의 팀원이 포함되어 있습니다.");
            }
        }


        ChatRoom chatRoom = ChatConverter.toChatRoomEntity(
                request.getProjectId(),
                request.getRoomName(), // 그룹 채팅은 입력받은 방 이름 사용
                ChatRoomType.GROUP
        );

        chatRoomRepository.save(chatRoom);


        List<ChatRoomUser> members = new ArrayList<>();
        members.add(ChatConverter.toChatRoomMemberEntity(chatRoom, me, LocalDateTime.now()));

        for (User user : targetUsers) {
            members.add(ChatConverter.toChatRoomMemberEntity(chatRoom, user, null));
        }

        chatRoomUserRepository.saveAll(members);

        return ChatConverter.toResponseDTO(chatRoom, null);
    }
}