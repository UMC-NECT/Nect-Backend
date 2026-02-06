package com.nect.api.domain.home.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class HomeProjectItem {
    private Long projectId;
    private String imageUrl;
    private String projectName;
    private String authorName;
    private String authorPart;
    private String introduction;
    private Integer leftDays;
    private Integer maxMemberCount;
    private Integer curMemberCount;
    private Boolean isScrapped;
    private String status;
    private Map<String, Integer> roles;

    public static HomeProjectItem of(
            Long projectId,
            String imageUrl,
            String projectName,
            String authorName,
            String authorPart,
            String introduction,
            Integer leftDays,
            Integer maxMemberCount,
            Integer curMemberCount,
            Boolean isScrapped,
            String status,
            Map<String, Integer> roles
    ) {
        return HomeProjectItem.builder()
                .projectId(projectId)
                .imageUrl(imageUrl)
                .projectName(projectName)
                .authorName(authorName)
                .authorPart(authorPart)
                .introduction(introduction)
                .leftDays(leftDays)
                .maxMemberCount(maxMemberCount)
                .curMemberCount(curMemberCount)
                .isScrapped(isScrapped)
                .status(status)
                .roles(roles)
                .build();
    }
}
