package com.nect.api.domain.home.dto;

public record HomeRecruitingProjectTeamComposition(
        HomeRecruitingProjectTeamPart planning,
        HomeRecruitingProjectTeamPart design,
        HomeRecruitingProjectTeamPart development
) {
}
