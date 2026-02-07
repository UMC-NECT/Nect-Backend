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
public class HomeMemberItem {
    private Long userId;
    private String imageUrl;
    private String name;
    private String part;
    private String introduction;
    private String status;
    private Boolean isScrapped;
    private List<String> roles;

    public static HomeMemberItem of(
            Long userId,
            String imageUrl,
            String name,
            String part,
            String introduction,
            String status,
            Boolean isScrapped,
            List<String> roles
    ) {
        return HomeMemberItem.builder()
                .userId(userId)
                .imageUrl(imageUrl)
                .name(name)
                .part(part)
                .introduction(introduction)
                .status(status)
                .isScrapped(isScrapped)
                .roles(roles)
                .build();
    }
}
