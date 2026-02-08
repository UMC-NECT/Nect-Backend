package com.nect.api.domain.team.project.service;

import com.nect.api.domain.team.project.dto.ProjectPartCreateReqDto;
import com.nect.api.domain.team.project.dto.ProjectPartCreateResDto;
import com.nect.api.domain.team.project.dto.ProjectPartUpdateReqDto;
import com.nect.api.domain.team.project.dto.ProjectPartUpdateResDto;
import com.nect.api.domain.team.project.enums.code.ProjectErrorCode;
import com.nect.api.domain.team.project.exception.ProjectException;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectTeamRole;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectTeamRoleRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectTeamCommandService {
    private final ProjectTeamRoleRepository projectTeamRoleRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProjectRepository projectRepository;

    private void assertActiveProjectMember(Long projectId, Long userId) {
        boolean ok = projectUserRepository.existsByProjectIdAndUserIdAndMemberStatus(
                projectId, userId, ProjectMemberStatus.ACTIVE
        );
        if (!ok) {
            throw new ProjectException(ProjectErrorCode.PROJECT_MEMBER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId);
        }
    }

    private void assertActiveLeader(Long projectId, Long userId) {
        boolean isLeader = projectUserRepository.existsActiveLeader(projectId, userId);
        if (!isLeader) {
            throw new ProjectException(ProjectErrorCode.LEADER_ONLY_ACTION,
                    "projectId=" + projectId + ", userId=" + userId);
        }
    }

    private String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }

    @Transactional
    public ProjectPartCreateResDto createProjectPart(Long projectId, Long userId, ProjectPartCreateReqDto req) {
        assertActiveProjectMember(projectId, userId);
        assertActiveLeader(projectId, userId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId));


        // roleField 필수
        RoleField roleField = req.roleField();
        if(roleField == null) {
            throw new ProjectException(ProjectErrorCode.INVALID_REQUEST, "role_field is null");
        }

        // 필요인원은 기본 1 -> 아직 정해진게 없음
        int requiredCount = (req.requiredCount() == null) ? 1 : req.requiredCount();
        if(requiredCount < 1) {
            throw new ProjectException(ProjectErrorCode.INVALID_REQUEST,
                    "required_count must be >= 1");
        }

        String customName = req.customRoleFieldName();

        if(roleField == RoleField.CUSTOM) {
            customName = normalize(customName);
            if (customName == null || customName.isBlank()) {
                throw new ProjectException(ProjectErrorCode.INVALID_CUSTOM_PART_NAME,
                        "custom_role_field is blank");
            }

            // CUSTOM 중복 방지
            boolean duplicate = projectTeamRoleRepository
                    .existsByProject_IdAndDeletedAtIsNullAndRoleFieldAndCustomRoleFieldName(
                            projectId, RoleField.CUSTOM, customName
                    );

            if(duplicate) {
                throw new ProjectException(ProjectErrorCode.DUPLICATE_PART,
                        "duplicate custom_role_field_name=" + customName
                );
            }
        }else {
            boolean duplicate = projectTeamRoleRepository
                    .existsByProject_IdAndDeletedAtIsNullAndRoleField(projectId, roleField);

            if (duplicate) {
                throw new ProjectException(ProjectErrorCode.DUPLICATE_PART,
                        "duplicate role_field=" + roleField);
            }
        }

        if (customName != null && customName.length() > 50) {
            throw new ProjectException(ProjectErrorCode.INVALID_CUSTOM_PART_NAME, "custom name too long");
        }

        // 저장
        ProjectTeamRole saved = projectTeamRoleRepository.save(
                ProjectTeamRole.builder()
                        .project(project)
                        .roleField(roleField)
                        .customRoleFieldName(customName)
                        .requiredCount(requiredCount)
                        .build()
        );

        String label = (saved.getRoleField() == RoleField.CUSTOM)
                ? saved.getCustomRoleFieldName()
                : saved.getRoleField().getLabelEn();

        return new ProjectPartCreateResDto(
                saved.getId(),
                saved.getRoleField(),
                saved.getCustomRoleFieldName(),
                label,
                saved.getRequiredCount()
        );
    }

    // 팀 파트 이름 수정 서비스
    @Transactional
    public ProjectPartUpdateResDto updateProjectPart(Long projectId, Long partId, Long userId, ProjectPartUpdateReqDto req) {
        assertActiveProjectMember(projectId, userId);
        assertActiveLeader(projectId, userId);

        ProjectTeamRole part = projectTeamRoleRepository.findByIdAndProject_IdAndDeletedAtIsNull(partId, projectId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.PROJECT_PART_NOT_FOUND,
                        "projectId=" + projectId + ", partId=" + partId)
                );

        // 필요인원 수정
        if (req.requiredCount() != null) {
            if (req.requiredCount() < 1) {
                throw new ProjectException(ProjectErrorCode.INVALID_REQUEST, "required_count must be >= 1");
            }
            part.setRequiredCount(req.requiredCount());
        }

        // 커스텀 필드 수정
        if (req.customRoleFieldName() != null) {
            if (part.getRoleField() != RoleField.CUSTOM) {
                // 커스텀이 아닌 필드는 이름 수정 불가
                throw new ProjectException(ProjectErrorCode.INVALID_REQUEST,
                        "custom_role_field_name can be updated only when role_field=CUSTOM");
            }

            String newName = normalize(req.customRoleFieldName());
            if (newName == null || newName.length() > 50) {
                throw new ProjectException(ProjectErrorCode.INVALID_CUSTOM_PART_NAME, "invalid custom name");
            }

            // 이름이 바뀌는 경우에만 중복 체크
            String oldName = part.getCustomRoleFieldName();
            if (!newName.equals(oldName)) {
                boolean duplicate = projectTeamRoleRepository
                        .existsByProject_IdAndDeletedAtIsNullAndRoleFieldAndCustomRoleFieldName(
                                projectId, RoleField.CUSTOM, newName
                        );
                if (duplicate) {
                    throw new ProjectException(ProjectErrorCode.DUPLICATE_PART,
                            "duplicate custom_role_field_name=" + newName);
                }
                part.setCustomRoleFieldName(newName);
            }
        }

        String label = (part.getRoleField() == RoleField.CUSTOM)
                ? part.getCustomRoleFieldName()
                : part.getRoleField().getLabelEn();

        return new ProjectPartUpdateResDto(
                part.getId(),
                part.getRoleField(),
                part.getCustomRoleFieldName(),
                label,
                part.getRequiredCount()
        );
    }
}
