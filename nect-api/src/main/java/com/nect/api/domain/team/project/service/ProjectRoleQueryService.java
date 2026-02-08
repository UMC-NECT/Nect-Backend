package com.nect.api.domain.team.project.service;

import com.nect.api.domain.team.project.dto.ProjectPartsResDto;
import com.nect.api.domain.team.project.enums.code.ProjectErrorCode;
import com.nect.api.domain.team.project.exception.ProjectException;
import com.nect.core.entity.team.ProjectTeamRole;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.team.ProjectTeamRoleRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectRoleQueryService {

    private final ProjectTeamRoleRepository projectTeamRoleRepository;
    private final ProjectUserRepository projectUserRepository;

    // 작업실 전용: 프로젝트 파트 목록 조회 (ProjectTeamRole사용)
    @Transactional(readOnly = true)
    public ProjectPartsResDto readProjectParts(Long projectId, Long requesterUserId) {
        assertActiveProjectMember(projectId, requesterUserId);

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
