package com.nect.api.domain.team.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.entity.user.enums.RoleField;

import java.util.List;

public record ProjectUsersResDto(
        @JsonProperty("users")
        List<UserDto> users
) {
    public ProjectUsersResDto {
        users = (users == null) ? List.of() : users;
    }

    public record UserDto(
            @JsonProperty("user_id")
            Long userId,

            @JsonProperty("name")
            String name,

            @JsonProperty("nickname")
            String nickname,

            @JsonProperty("profile_image_url")
            String profileImageUrl,

            @JsonProperty("role_field")
            RoleField roleField,

            @JsonProperty("custom_role_field_name")
            String customRoleFieldName,

            @JsonProperty("part_label")
            String partLabel,

            @JsonProperty("member_type")
            ProjectMemberType memberType
    ) {}
}
