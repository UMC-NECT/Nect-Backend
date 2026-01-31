package com.nect.api.domain.team.workspace.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PostCreateResDto(
        @JsonProperty("post_id")
        Long postId
) {}
