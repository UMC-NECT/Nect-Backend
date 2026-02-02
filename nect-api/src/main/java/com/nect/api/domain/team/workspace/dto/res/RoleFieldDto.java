package com.nect.api.domain.team.workspace.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.user.enums.RoleField;

public record RoleFieldDto(
        @JsonProperty("type")
        RoleField type,

        /**
         * type == CUSTOM 일 때만 값이 존재
         */
        @JsonProperty("custom_name")
        String customName
) {
    public static RoleFieldDto of(RoleField type, String customName) {
        if (type == RoleField.CUSTOM) {
            return new RoleFieldDto(type, customName);
        }
        return new RoleFieldDto(type, null);
    }

    public static RoleFieldDto of(RoleField type) {
        return new RoleFieldDto(type, null);
    }
}
