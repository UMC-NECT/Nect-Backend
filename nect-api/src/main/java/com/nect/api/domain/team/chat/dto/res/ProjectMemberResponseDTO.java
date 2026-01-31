package com.nect.api.domain.team.chat.dto.res;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
//같은 프로젝트 내 유저 조회 DTO
public class ProjectMemberResponseDto {
    private Long userId;
    private String username; // 화면에 표시할 이름
    // private String profileImage; // TODO 프로필 이미지가 있다면 추가
}
