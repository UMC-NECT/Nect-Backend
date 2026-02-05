package com.nect.api.domain.team.project.dto;

import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.entity.user.enums.RoleField;
import lombok.Builder;

@Builder
public record ProjectUserResDto(
        Long id,
        Long userId,
        Long projectId,
        RoleField field,
        ProjectMemberType memberType,
        ProjectMemberStatus memberStatus
) {}
