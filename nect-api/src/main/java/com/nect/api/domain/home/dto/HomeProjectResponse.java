package com.nect.api.domain.home.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class HomeProjectResponse {
    private List<HomeProjectItem> projects;

    public static HomeProjectResponse of(List<HomeProjectItem> projects) {
        return HomeProjectResponse.builder()
                .projects(projects)
                .build();
    }
}
