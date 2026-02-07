package com.nect.api.domain.team.process.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record WeekMissionDropdownResDto(
        @JsonProperty("missions")
        List<MissionDto> missions
) {
    public WeekMissionDropdownResDto {
        missions = (missions == null) ? List.of() : missions;
    }

    public record MissionDto(
            @JsonProperty("mission_number")
            Integer missionNumber,

            @JsonProperty("start_date")
            LocalDate startDate,

            @JsonProperty("end_date")
            LocalDate endDate,

            @JsonProperty("is_current")
            Boolean isCurrent
    ) {}
}
