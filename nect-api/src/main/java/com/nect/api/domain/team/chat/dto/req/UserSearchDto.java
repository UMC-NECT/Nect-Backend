package com.nect.api.domain.team.chat.dto.req;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
// 유저 조회 DTO
public class UserSearchDto {
    private Long userId;
    private String userName;
    private String profileImage;  //TODO : 프로필 엔티티 생기면 수정 필요

}
