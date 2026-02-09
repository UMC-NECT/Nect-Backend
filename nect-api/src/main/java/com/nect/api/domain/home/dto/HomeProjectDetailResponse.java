package com.nect.api.domain.home.dto;

import com.nect.api.domain.mypage.dto.MyProjectsResponseDto;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HomeProjectDetailResponse {

    private MyProjectsResponseDto.ProjectInfo defaultInfo; // 기본정보
    private MyProjectsResponseDto.ProjectFieldResponse fields; // 프로젝트 분야
    //    private stack...; // TODO 필수스택
    private MyProjectsResponseDto.StringListResponse purposes; // 프로젝트 목표
    private MyProjectsResponseDto.StringListResponse functions; // 주요기능
    private MyProjectsResponseDto.StringListResponse serviceUsers; // 서비스 사용자
    private MyProjectsResponseDto.ProjectPlanFilesResponse planFiles; // 기획 파일

}
