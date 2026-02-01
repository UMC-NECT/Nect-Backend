package com.nect.api.domain.team.workspace.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MissionProgressResDto(
        @JsonProperty("total")
        TotalDto total,

        @JsonProperty("teams")
        List<TeamDto> teams
) {
    public record TotalDto(
            @JsonProperty("total_count")
            Long totalCount,

            @JsonProperty("completed_count")
            Long completedCount,

            @JsonProperty("completion_rate")
            double completionRate
    ) {}

    public record TeamDto(
            @JsonProperty("field")
            RoleFieldDto field,

            @JsonProperty("total_count")
            Long totalCount,

            @JsonProperty("completed_count")
            Long completedCount,

            @JsonProperty("completion_rate")
            double completionRate
    ) {}
}
