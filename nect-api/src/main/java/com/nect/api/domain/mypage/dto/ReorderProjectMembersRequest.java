package com.nect.api.domain.mypage.dto;

import com.nect.core.entity.user.enums.RoleField;
import lombok.Builder;

import java.util.List;

public record ReorderProjectMembersRequest(
        List<PartUpdate> updates
) {
    @Builder
    public record PartUpdate(
            RoleField roleField,
            String customRoleField,
            List<Long> orderedUserIds
    ) {}
}
