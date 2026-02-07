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
public class HomeMembersResponse {
    private List<HomeMemberItem> members;

    public static HomeMembersResponse of(List<HomeMemberItem> members) {
        return HomeMembersResponse.builder()
                .members(members)
                .build();
    }
}
