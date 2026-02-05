package com.nect.api.domain.team.project.service;

import com.nect.api.domain.team.project.dto.ProjectPartsResDto;
import com.nect.api.domain.team.project.dto.ProjectUsersResDto;
import com.nect.api.domain.team.project.enums.code.ProjectErrorCode;
import com.nect.api.domain.team.project.exception.ProjectException;
import com.nect.core.entity.team.ProjectTeamRole;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.team.ProjectTeamRoleRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectTeamQueryService {
    private final ProjectTeamRoleRepository projectTeamRoleRepository;
    private final ProjectUserRepository projectUserRepository;
    private final UserRepository userRepository;

    // 프로젝트 파트 목록 조회 서비스
    @Transactional(readOnly = true)
    public ProjectPartsResDto readProjectParts(Long projectId, Long userId) {
        assertActiveProjectMember(projectId, userId);

        List<ProjectTeamRole> roles = projectTeamRoleRepository.findAllActiveByProjectId(projectId);

        List<ProjectPartsResDto.PartDto> parts = roles.stream()
                .map(ptr -> {
                    RoleField rf = ptr.getRoleField();
                    String customName = ptr.getCustomRoleFieldName();

                    String label = (rf == RoleField.CUSTOM)
                            ? customName
                            : rf.getLabelEn();

                    return new ProjectPartsResDto.PartDto(
                            ptr.getId(),
                            rf,
                            customName,
                            label,
                            ptr.getRequiredCount()
                    );
                })
                .toList();

        return new ProjectPartsResDto(parts);
    }

    // 프로젝트 멤버 전체 조회 서비스
    @Transactional(readOnly = true)
    public ProjectUsersResDto readProjectUsers(Long projectId, Long userId) {
        assertActiveProjectMember(projectId, userId);

        List<ProjectUserRepository.MemberBoardRow> rows =
                projectUserRepository.findActiveMemberBoardRows(projectId);

        List<Long> ids = rows.stream()
                .map(ProjectUserRepository.MemberBoardRow::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, User> userMap = userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        List<ProjectUsersResDto.UserDto> users = rows.stream()
                .map(r -> {
                    User u = userMap.get(r.getUserId());
                    String profileUrl = (u == null) ? null : u.getProfileImageUrl();

                    RoleField rf = r.getRoleField();
                    String customName = r.getCustomRoleFieldName();

                    String label = (rf == RoleField.CUSTOM)
                            ? customName
                            : rf.getDescription();

                    return new ProjectUsersResDto.UserDto(
                            r.getUserId(),
                            r.getName(),
                            r.getNickname(),
                            profileUrl,
                            rf,
                            customName,
                            label,
                            r.getMemberType()
                    );
                })
                .toList();

        return new ProjectUsersResDto(users);
    }


    private void assertActiveProjectMember(Long projectId, Long userId) {
        boolean ok = projectUserRepository.existsByProjectIdAndUserIdAndMemberStatus(
                projectId, userId, ProjectMemberStatus.ACTIVE
        );
        if (!ok) {
            throw new ProjectException(ProjectErrorCode.PROJECT_MEMBER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId);
        }

    }
}
