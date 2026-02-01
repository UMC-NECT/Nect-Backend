package com.nect.api.domain.team.chat.service;

import com.nect.api.domain.team.chat.converter.ChatConverter;
import com.nect.api.domain.team.chat.dto.req.ChatRoomCreateRequestDto;
import com.nect.api.domain.team.chat.dto.req.GroupChatRoomCreateRequestDto;
import com.nect.api.domain.team.chat.dto.res.ChatRoomResponseDto;
import com.nect.api.domain.team.chat.dto.res.ProjectMemberResponseDto;
import com.nect.api.domain.team.chat.enums.ChatErrorCode;
import com.nect.api.domain.team.chat.exeption.ChatException;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.chat.ChatRoom;
import com.nect.core.entity.team.chat.ChatRoomUser;
import com.nect.core.entity.user.User;
import com.nect.core.entity.team.chat.enums.ChatRoomType;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.chat.ChatRoomUserRepository;
import com.nect.core.repository.team.chat.ChatRoomRepository;
import com.nect.core.repository.user.UserRepository;
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
    private final ProjectUserRepository projectUserRepository;
    private final ProjectRepository projectRepository;

    public List<ProjectMemberResponseDto> getProjectMembers(Long projectId) {
        List<User> members = projectUserRepository.findAllUsersByProjectId(projectId);
        return ChatConverter.toProjectMemberResponseDTOList(members);
    }


    // 1:1 채팅방 생성
    @Transactional
    public ChatRoomResponseDto createOneOnOneChatRoom(Long currentUserId, ChatRoomCreateRequestDto request) {


        boolean isMeInProject = projectUserRepository.existsByProjectIdAndUserId(request.getProject_id(), currentUserId);
        boolean isTargetInProject = projectUserRepository.existsByProjectIdAndUserId(request.getProject_id(), request.getTarget_user_id());

        if (!isMeInProject || !isTargetInProject) {
            throw new ChatException(ChatErrorCode.CHAT_MEMBER_NOT_FOUND, "서로 같은 팀원이 아닙니다.");
        }

        Project project = projectRepository.findById(request.getProject_id())
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));

        User me = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_FOUND, "현재 사용자를 찾을 수 없습니다."));

        User targetUser = userRepository.findById(request.getTarget_user_id())
                .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_FOUND, "상대방 사용자를 찾을 수 없습니다."));


        Optional<Long> existingRoomId = chatRoomRepository.findExistingOneOnOneRoomId(
                request.getProject_id(),
                currentUserId,
                request.getTarget_user_id()
        );

        if (existingRoomId.isPresent()) {
            ChatRoom existingRoom = chatRoomRepository.findById(existingRoomId.get())
                    .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND, "존재하는 채팅방 ID를 찾을 수 없습니다."));

            return ChatConverter.toResponseDTO(existingRoom, targetUser);
        }


        ChatRoom chatRoom = ChatConverter.toChatRoomEntity(
                project,
                null,
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
    public ChatRoomResponseDto createGroupChatRoom(Long currentUserId, GroupChatRoomCreateRequestDto request) {

    
        if (!StringUtils.hasText(request.getRoomName())) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_ALREADY_EXISTS, "채팅방 이름을 입력해야 합니다.");
        }
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));
        
        boolean isCreatorInProject = projectUserRepository.existsByProjectIdAndUserId(request.getProjectId(), currentUserId);
        if (!isCreatorInProject) {
            throw new ChatException(ChatErrorCode.USER_NOT_FOUND, "현재 사용자는 해당 프로젝트의 멤버가 아닙니다.");
        }

        User me = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_FOUND, "현재 사용자를 찾을 수 없습니다."));


        List<User> targetUsers = projectUserRepository.findAllUsersByProjectIdAndUserIds(
                request.getProjectId(),
                request.getTargetUserIds()
        );

        if (targetUsers.size() != request.getTargetUserIds().size()) {
            throw new ChatException(ChatErrorCode.USER_NOT_FOUND, "유저가 프로젝트에 속하지 않습니다");
        }

     
        ChatRoom chatRoom = ChatConverter.toChatRoomEntity(
                project,
                request.getRoomName(),
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