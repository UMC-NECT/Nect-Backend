package com.nect.api.domain.home.dto;

import com.nect.core.entity.user.enums.Role;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class HomeHeaderResponse {

    private Long userId;
    private String imageUrl;
    private String name;
    private String email;
    private Role role;

    public static HomeHeaderResponse of(
            Long userId,
            String imageUrl,
            String name,
            String email,
            Role role
    ) {
        return HomeHeaderResponse.builder()
                .userId(userId)
                .imageUrl(imageUrl)
                .name(name)
                .email(email)
                .role(role)
                .build();
    }
}
