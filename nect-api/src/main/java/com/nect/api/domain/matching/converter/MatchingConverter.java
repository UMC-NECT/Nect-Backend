package com.nect.api.domain.matching.converter;

import com.nect.api.domain.matching.dto.MatchingResDto;
import com.nect.core.entity.matching.Matching;
import com.nect.core.entity.matching.enums.MatchingRequestType;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.enums.RoleField;

public class MatchingConverter {

    public static MatchingResDto.MatchingRes toMatchingResDto(Matching matching) {
        return MatchingResDto.MatchingRes.builder()
                .id(matching.getId())
                .requestUserId(matching.getRequestUser().getUserId())
                .targetUserId(matching.getTargetUser().getUserId())
                .projectId(matching.getProject().getId())
                .field(matching.getField())
                .customField(matching.getCustomField())
                .matchingStatus(matching.getMatchingStatus())
                .requestType(matching.getRequestType())
                .expiresAt(matching.getExpiresAt())
                .build();
    }

    public static Matching toMatching(
            User requestUser,
            User targetUser,
            Project project,
            RoleField field,
            MatchingRequestType requestType,
            String customField
    ){
        return Matching.builder()
                .requestUser(requestUser)
                .targetUser(targetUser)
                .project(project)
                .field(field)
                .requestType(requestType)
                .customField(field == RoleField.CUSTOM ? customField : null)
                .build();
    }

    public static MatchingResDto.UserSummary toUserSummary(User user){
        // TODO: 프로필 구현 완성 시 수정
        return MatchingResDto.UserSummary.builder()
                .nickname(user.getNickname())
                .bio("")
                .field(RoleField.BACKEND)
                .profileUrl("")
                .build();
    }

    public static MatchingResDto.ProjectSummary toProjectSummary(Project project, long countUserNum) {
        return MatchingResDto.ProjectSummary.builder()
                .title(project.getTitle())
                .description(project.getDescription())
                .imageUrl(project.getImageName())
                .currentMembersNum(countUserNum)
                .build();
    }
}
