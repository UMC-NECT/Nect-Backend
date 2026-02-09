package com.nect.api.domain.mypage.service;

import com.nect.api.domain.mypage.dto.UserTeamRoleCreateReqDto;
import com.nect.api.domain.mypage.dto.UserTeamRoleCreateResDto;
import com.nect.api.domain.mypage.dto.UserTeamRoleUpdateReqDto;
import com.nect.api.domain.mypage.dto.UserTeamRoleUpdateResDto;
import com.nect.api.domain.mypage.enums.UserTeamRoleErrorCode;
import com.nect.api.domain.mypage.exception.UserTeamRoleException;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.UserTeamRole;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.user.UserRepository;
import com.nect.core.repository.user.UserTeamRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserTeamRoleService {
    private static final int DEFAULT_REQUIRED_COUNT = 1;

    private final UserTeamRoleRepository userTeamRoleRepository;
    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;

    private String normalize(String raw) {
        if (raw == null) return null;
        String t = raw.trim();
        return t.isEmpty() ? null : t;
    }

    // 마이페이지 팀 파트 생성 서비스
    @Transactional
    public UserTeamRoleCreateResDto create(Long projectId, Long userId, UserTeamRoleCreateReqDto req) {
        // 요청 검증
        if (projectId == null) {
            throw new UserTeamRoleException(UserTeamRoleErrorCode.INVALID_REQUEST,
                    "projectId is required"
            );
        }

        if(req == null || req.roleField() == null) {
            throw new UserTeamRoleException(UserTeamRoleErrorCode.INVALID_REQUEST,
                    "roleField is required"
            );
        }

        // 리더 검증
        boolean isLeader = projectUserRepository.existsActiveLeader(projectId, userId);
        if (!isLeader) {
            throw new UserTeamRoleException(
                    UserTeamRoleErrorCode.FORBIDDEN_NOT_LEADER,
                    "projectId=" + projectId + ", userId=" + userId
            );
        }

        RoleField roleField = req.roleField();
        String customName = normalize(req.customRoleFieldName());
        int requiredCount = (req.requiredCount() == null) ? DEFAULT_REQUIRED_COUNT : req.requiredCount();

        if(requiredCount < 1) {
            throw new UserTeamRoleException(
                    UserTeamRoleErrorCode.INVALID_REQUEST,
                    "requiredCount=" + requiredCount
            );
        }

        if(roleField == RoleField.CUSTOM) {
            if (customName == null || customName.isBlank()) {
                throw new UserTeamRoleException(UserTeamRoleErrorCode.INVALID_CUSTOM_ROLE_NAME, "customRoleName is required");
            }

            boolean duplicate = userTeamRoleRepository
                    .existsByProject_IdAndRoleFieldAndCustomRoleFieldNameIgnoreCaseAndDeletedAtIsNull(projectId, roleField, customName);

            if(duplicate) {
                throw new UserTeamRoleException(
                        UserTeamRoleErrorCode.DUPLICATE_ROLE,
                        "customRoleName=" + customName
                );
            }
        }else {
            boolean duplicate = userTeamRoleRepository
                    .existsByProject_IdAndRoleFieldAndDeletedAtIsNull(projectId, roleField);

            if (duplicate) {
                throw new UserTeamRoleException(
                        UserTeamRoleErrorCode.DUPLICATE_ROLE,
                        "roleField=" + roleField.name()
                );
            }

            customName = null;
        }


        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new UserTeamRoleException(UserTeamRoleErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId));

        UserTeamRole saved = userTeamRoleRepository.save(
                UserTeamRole.builder()
                        .project(project)
                        .roleField(roleField)
                        .customRoleFieldName(customName)
                        .requiredCount(requiredCount)
                        .build()
        );

        return new UserTeamRoleCreateResDto(
                saved.getId(),
                saved.getRoleField(),
                saved.getCustomRoleFieldName(),
                saved.getLabel(),
                saved.getRequiredCount()
        );
    }

    // 마이페이지 팀 수정 서비스(이름 or 총원)
    @Transactional
    public UserTeamRoleUpdateResDto update(Long projectId, Long userId, Long userTeamRoleId, UserTeamRoleUpdateReqDto req) {
        if (projectId == null) {
            throw new UserTeamRoleException(UserTeamRoleErrorCode.INVALID_REQUEST, "projectId is required");
        }
        if (userTeamRoleId == null) {
            throw new UserTeamRoleException(UserTeamRoleErrorCode.INVALID_REQUEST, "userTeamRoleId is required");
        }
        if (req == null) {
            throw new UserTeamRoleException(UserTeamRoleErrorCode.INVALID_REQUEST, "req is required");
        }

        // 리더 검증
        boolean isLeader = projectUserRepository.existsActiveLeader(projectId, userId);
        if (!isLeader) {
            throw new UserTeamRoleException(
                    UserTeamRoleErrorCode.FORBIDDEN_NOT_LEADER,
                    "projectId=" + projectId + ", userId=" + userId
            );
        }

        // 수정 대상 조회(soft delete 제외)
        UserTeamRole role = userTeamRoleRepository
                .findByIdAndProject_IdAndDeletedAtIsNull(userTeamRoleId, projectId)
                .orElseThrow(() -> new UserTeamRoleException(
                        UserTeamRoleErrorCode.ROLE_NOT_FOUND,
                        "projectId=" + projectId + ", userTeamRoleId=" + userTeamRoleId
                ));

        // CUSTOM만 수정 가능
        if (role.getRoleField() != RoleField.CUSTOM) {
            throw new UserTeamRoleException(
                    UserTeamRoleErrorCode.INVALID_REQUEST,
                    "only CUSTOM role can be updated. roleField=" + role.getRoleField()
            );
        }

        // required_count 수정
        if (req.requiredCount() != null) {
            int requiredCount = req.requiredCount();
            if (requiredCount < 1) {
                throw new UserTeamRoleException(UserTeamRoleErrorCode.INVALID_REQUEST, "requiredCount=" + requiredCount);
            }
            role.updateRequiredCount(requiredCount);
        }

        // custom 이름 수정
        if (req.customRoleFieldName() != null) {
            String newName = normalize(req.customRoleFieldName());

            if (newName == null || newName.isBlank()) {
                throw new UserTeamRoleException(UserTeamRoleErrorCode.INVALID_CUSTOM_ROLE_NAME, "customRoleName is required");
            }
            if (newName.length() > 50) {
                throw new UserTeamRoleException(UserTeamRoleErrorCode.INVALID_CUSTOM_ROLE_NAME, "customRoleName too long");
            }

            String oldName = role.getCustomRoleFieldName();
            if (oldName == null || !oldName.equalsIgnoreCase(newName)) {
                boolean duplicate = userTeamRoleRepository
                        .existsByProject_IdAndRoleFieldAndCustomRoleFieldNameIgnoreCaseAndDeletedAtIsNull(
                                projectId, RoleField.CUSTOM, newName
                        );

                if (duplicate) {
                    throw new UserTeamRoleException(UserTeamRoleErrorCode.DUPLICATE_ROLE, "customRoleName=" + newName);
                }
                role.updateCustomRoleName(newName);
            }
        }

        return new UserTeamRoleUpdateResDto(
                role.getId(),
                role.getRoleField(),
                role.getCustomRoleFieldName(),
                role.getLabel(),      // JSON 키는 DTO에서 part_label로 매핑
                role.getRequiredCount()
        );
    }
}
