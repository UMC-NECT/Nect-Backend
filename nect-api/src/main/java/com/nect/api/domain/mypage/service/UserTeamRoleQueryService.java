package com.nect.api.domain.mypage.service;

import com.nect.api.domain.mypage.dto.UserTeamRolesResDto;
import com.nect.api.domain.mypage.enums.UserTeamRoleErrorCode;
import com.nect.api.domain.mypage.exception.UserTeamRoleException;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.user.UserTeamRole;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.user.UserTeamRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserTeamRoleQueryService {

    private final UserTeamRoleRepository userTeamRoleRepository;
    private final ProjectUserRepository projectUserRepository;

    // 마이페이지 팀 파트 조회(UserTeamRole 사용), 프로젝트 멤버면 조회 가능
    @Transactional(readOnly = true)
    public UserTeamRolesResDto readMyPageParts(Long projectId, Long requesterUserId) {

        if (projectId == null) {
            throw new UserTeamRoleException(UserTeamRoleErrorCode.INVALID_REQUEST, "projectId is required");
        }

        boolean isActiveMember = projectUserRepository.existsByProjectIdAndUserIdAndMemberStatus(
                projectId, requesterUserId, ProjectMemberStatus.ACTIVE
        );
        if (!isActiveMember) {
            throw new UserTeamRoleException(UserTeamRoleErrorCode.FORBIDDEN_NOT_PROJECT_MEMBER,
                    "projectId=" + projectId + ", userId=" + requesterUserId);
        }

        List<UserTeamRole> roles =
                userTeamRoleRepository.findAllByProject_IdAndDeletedAtIsNullOrderByIdAsc(projectId);

        List<UserTeamRolesResDto.PartDto> parts = roles.stream()
                .map(r -> new UserTeamRolesResDto.PartDto(
                        r.getId(),
                        r.getRoleField(),
                        r.getCustomRoleFieldName(),
                        r.getLabel(),
                        r.getRequiredCount()
                ))
                .toList();

        return new UserTeamRolesResDto(parts);
    }
}
