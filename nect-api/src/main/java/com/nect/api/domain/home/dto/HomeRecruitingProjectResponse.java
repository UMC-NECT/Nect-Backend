package com.nect.api.domain.home.dto;

import java.time.LocalDate;
import java.util.List;

public record HomeRecruitingProjectResponse(
        Long projectId,
        String projectName,
        String introduction,
        LocalDate startDate,
        LocalDate endDate,
        String recruitmentStatus,
        Boolean isMatching,
        Integer dDay,
        Long chatRoomId,
        List<String> categories,
        List<HomeRecruitingProjectPosition> positions,
        HomeRecruitingProjectTeamComposition teamComposition,
        List<String> goals,
        List<String> mainFeatures,
        List<String> targetUsers,
        HomeRecruitingProjectLeader leader,
        List<HomeRecruitingProjectFile> attachedFiles
) {
}
