package com.nect.api.domain.team.workspace.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nect.core.entity.team.enums.ProjectMemberType;

import java.time.LocalDateTime;
import java.util.List;

public record MemberBoardResDto(
        @JsonProperty("members")
        List<MemberDto> members
) {
    public record MemberDto(
            @JsonProperty("user_id")
            Long userId,

            @JsonProperty("name")
            String name,

            @JsonProperty("nickname")
            String nickname,

            @JsonProperty("profile_image_url")
            String profileImageUrl,

            @JsonProperty("field")
            RoleFieldDto field,

            @JsonProperty("member_type")
            ProjectMemberType memberType,

            @JsonProperty("counts")
            CountsDto counts,

            @JsonProperty("is_working")
            boolean isWorking,

            @JsonProperty("today_work_seconds")
            long todayWorkSeconds,

            @JsonProperty("working_started_at")
            LocalDateTime workingStartedAt
    ) {}

    public record CountsDto(
            @JsonProperty("planning")
            long planning,

            @JsonProperty("in_progress")
            long inProgress,

            @JsonProperty("done")
            long done
    ) {}
}