package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AssigneeResDto(
        @JsonProperty("user_id")
        Long userId,

        @JsonProperty("user_name")
        String userName,

        @JsonProperty("user_image")
        String userImage
) {}
