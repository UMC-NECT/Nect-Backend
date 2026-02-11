package com.nect.api.domain.team.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.user.enums.Role;
import com.nect.core.entity.user.enums.RoleField;

import java.util.List;

public record ProjectMemberStatisticResponse(
        @JsonProperty("roles")
        List<RoleStatistic> roles
) {
    public ProjectMemberStatisticResponse {
        roles = (roles == null) ? List.of() : roles;
    }

    public record RoleStatistic(
            @JsonProperty("role")
            Role role,

            @JsonProperty("count")
            int count,

            @JsonProperty("role_fields")
            List<RoleFieldStatistic> roleFields
    ) {
        public RoleStatistic {
            roleFields = (roleFields == null) ? List.of() : roleFields;
        }
    }

    public record RoleFieldStatistic(
            @JsonProperty("role_field")
            RoleField roleField,

            @JsonProperty("label_en")
            String labelEn,

            @JsonProperty("count")
            int count
    ) {}
}
