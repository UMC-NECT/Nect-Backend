package com.nect.api.domain.team.chat.service;

import com.nect.api.domain.team.chat.converter.ChatConverter;
import com.nect.api.domain.team.chat.dto.req.ChatRoomInviteRequestDto;
import com.nect.api.domain.team.chat.dto.req.GroupChatRoomCreateRequestDto;
import com.nect.api.domain.team.chat.dto.res.ChatRoomInviteResponseDto;
import com.nect.api.domain.team.chat.dto.res.ChatRoomResponseDto;
import com.nect.api.domain.team.chat.dto.res.ProjectMemberDto;
import com.nect.api.domain.team.chat.dto.res.ProjectMemberResponseDto;
import com.nect.api.domain.team.chat.enums.ChatErrorCode;
import com.nect.api.domain.team.chat.exeption.ChatException;
import com.nect.api.global.infra.S3Service;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectUser;
import com.nect.core.entity.team.chat.ChatRoom;
import com.nect.core.entity.team.chat.ChatRoomUser;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final UserRepository userRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProjectRepository projectRepository;
    private final ChatService chatService;
    private final S3Service s3Service;

    public List<ProjectMemberResponseDto> getProjectMembers(Long projectId) {
        List<User> members = projectUserRepository.findAllUsersByProjectId(projectId);
        return ChatConverter.toProjectMemberResponseDTOList(members);
    }

    // 팀 채팅방 생성
    @Transactional
    public ChatRoomResponseDto createGroupChatRoom(Long currentUserId, GroupChatRoomCreateRequestDto request) {

        Set<Long> uniqueTargetIds = new HashSet<>(request.getTargetUserIds());
        uniqueTargetIds.remove(currentUserId);

        if (uniqueTargetIds.isEmpty()) {
            throw new ChatException(ChatErrorCode.NO_MEMBERS_SELECTED, "최소 1명 이상 선택해야 합니다.");
        }

        if (!StringUtils.hasText(request.getRoomName())) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_ALREADY_EXISTS, "채팅방 이름을 입력해야 합니다.");
        }

        if (request.getRoomName().length() > 30) {
            throw new ChatException(ChatErrorCode.ROOM_NAME_TOO_LONG, "채팅방 이름은 30자를 초과할 수 없습니다.");
        }

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));

        boolean isCreatorInProject = projectUserRepository.existsByProjectIdAndUserId(request.getProjectId(), currentUserId);
        if (!isCreatorInProject) {
            throw new ChatException(ChatErrorCode.USER_NOT_FOUND, "현재 사용자는 해당 프로젝트의 멤버가 아닙니다.");
        }

        User me = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_FOUND, "현재 사용자를 찾을 수 없습니다."));


        List<User> targetUsers = projectUserRepository.findActiveUsersByProjectIdAndUserIds(
                request.getProjectId(),
                new ArrayList<>(uniqueTargetIds)
        );
        // 2. 결과 대조 로그
        List<Long> foundUserIds = targetUsers.stream().map(User::getUserId).toList();

        if (targetUsers.size() != uniqueTargetIds.size()) {
            Set<Long> missingIds = new HashSet<>(uniqueTargetIds);
            missingIds.removeAll(foundUserIds);

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

        List<String> profileImages = members.stream()
                .map(member -> member.getUser().getProfileImageName())
                .filter(StringUtils::hasText)
                .map(s3Service::getPresignedGetUrl)
                .filter(StringUtils::hasText)
                .limit(4)
                .collect(Collectors.toList());

        return ChatConverter.toResponseDTO(chatRoom, profileImages);


    }

    @Transactional
    public ChatRoomInviteResponseDto inviteMembers(
            Long roomId,
            Long currentUserId,
            ChatRoomInviteRequestDto request
    ) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        Long projectId = chatRoom.getProject().getId();

        boolean isInviterMember = chatRoomUserRepository
                .existsByChatRoomIdAndUserUserId(roomId, currentUserId);
        if (!isInviterMember) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED,
                    "채팅방 멤버만 초대할 수 있습니다.");
        }

        if (request.getTargetUserIds() == null || request.getTargetUserIds().isEmpty()) {
            throw new ChatException(ChatErrorCode.NO_MEMBERS_SELECTED,
                    "초대할 멤버를 선택해주세요.");
        }

        // 초대 대상이 프로젝트 멤버인지 확인
        List<Long> validUserIds = projectUserRepository
                .findActiveUsersByProjectIdAndUserIds(projectId, request.getTargetUserIds())
                .stream()
                .map(User::getUserId)
                .toList();

        if (validUserIds.size() != request.getTargetUserIds().size()) {
            throw new ChatException(ChatErrorCode.USER_NOT_FOUND,
                    "일부 사용자가 프로젝트에 속하지 않습니다.");
        }

        // 이미 채팅방에 있는 멤버 제외
        List<Long> alreadyJoinedIds = chatRoomUserRepository
                .findUserIdsByChatRoomId(roomId);

        List<Long> newMemberIds = validUserIds.stream()
                .filter(userId -> !alreadyJoinedIds.contains(userId))
                .collect(Collectors.toList());


        List<User> newMembers = userRepository.findAllByUserIdIn(newMemberIds);

        List<ChatRoomUser> chatRoomUsers = newMembers.stream()
                .map(user -> ChatConverter.toChatRoomMemberEntity(chatRoom, user, null))
                .collect(Collectors.toList());

        chatRoomUserRepository.saveAll(chatRoomUsers);

        // 초대 알림 메시지 전송
        User inviter = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_FOUND));

        String invitedNames = newMembers.stream()
                .map(User::getName)
                .collect(Collectors.joining(", "));

        //TODO : 초대메시지
        String notificationMessage = String.format(
                "%s님이 %s님을 초대했습니다.",
                inviter.getName(),
                invitedNames
        );

        chatService.sendMessage(roomId, currentUserId, notificationMessage);


        List<String> invitedUserNames = newMembers.stream()
                .map(User::getName)
                .collect(Collectors.toList());

        List<String> profileImages = newMembers.stream()
                .map(User::getProfileImageName)
                .filter(StringUtils::hasText)
                .map(s3Service::getPresignedGetUrl)
                .collect(Collectors.toList());

        return ChatRoomInviteResponseDto.builder()
                .roomId(roomId)
                .invitedCount(newMembers.size())
                .invitedUserNames(invitedUserNames)
                .profileImages(profileImages)
                .build();
    }

    private void validateInviter(Long projectId, Long userId) {
        if (!projectUserRepository.existsByProjectIdAndUserIdAndMemberStatus(
                projectId, userId, ProjectMemberStatus.ACTIVE)) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED, "프로젝트 멤버만 초대 권한이 있습니다.");
        }
    }

    private List<User> getValidProjectUsers(Long projectId, List<Long> ids) {
        List<ProjectUser> members = projectUserRepository.findAllActiveProjectMembers(projectId, ids);
        if (members.size() != ids.size()) {
            throw new ChatException(ChatErrorCode.USER_NOT_FOUND, "프로젝트 멤버가 아니거나 탈퇴한 유저가 포함됨");
        }
        return userRepository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberDto> getProjectMembers(Long projectId, Long currentUserId,String keyword) {

        boolean isMember = projectUserRepository.existsByProjectIdAndUserId(projectId, currentUserId);
        if (!isMember) {
            throw new ChatException(ChatErrorCode.USER_NOT_FOUND, "프로젝트 멤버가 아닙니다.");
        }

        List<Long> userIds = projectUserRepository
                .findUserIdsByProjectIdExcludingUser(projectId, currentUserId);

        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<User> projectMembers;
        if (StringUtils.hasText(keyword)) {
            projectMembers = userRepository.findAllByUserIdInAndKeyword(userIds, keyword);
        } else {
            projectMembers = userRepository.findAllByUserIdIn(userIds);
        }


        return projectMembers.stream()
                .map(user -> ProjectMemberDto.builder()
                        .userId(user.getUserId())
                        .nickname(user.getNickname())
                        .name(user.getName())
                        .profileImage(StringUtils.hasText(user.getProfileImageName())
                                ? s3Service.getPresignedGetUrl(user.getProfileImageName())
                                : null)
                        .build())
                .collect(Collectors.toList());
    }

}