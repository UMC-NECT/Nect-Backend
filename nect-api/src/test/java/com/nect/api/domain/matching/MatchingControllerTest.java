package com.nect.api.domain.matching;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.domain.home.dto.HomeProjectDetailResponse;
import com.nect.api.domain.home.facade.MainHomeFacade;
import com.nect.api.domain.matching.dto.MatchingReqDto;
import com.nect.api.domain.matching.dto.MatchingResDto;
import com.nect.api.domain.matching.enums.CounterParty;
import com.nect.api.domain.matching.facade.MatchingFacade;
import com.nect.api.domain.matching.service.MatchingService;
import com.nect.api.domain.mypage.dto.MyProjectsResponseDto;
import com.nect.api.domain.mypage.dto.ProfileSettingsDto;
import com.nect.api.domain.mypage.service.MypageService;
import com.nect.api.domain.team.project.dto.ProjectMemberStatisticResponse;
import com.nect.api.domain.team.project.dto.ProjectUserResDto;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
import com.nect.core.entity.matching.enums.MatchingRejectReason;
import com.nect.core.entity.matching.enums.MatchingRequestType;
import com.nect.core.entity.matching.enums.MatchingStatus;
import com.nect.core.entity.team.enums.*;
import com.nect.core.entity.user.enums.Role;
import com.nect.core.entity.user.enums.RoleField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
public class MatchingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MatchingFacade matchingFacade;

    @MockitoBean
    MatchingService matchingService;

    @MockitoBean
    MypageService mypageService;

    @MockitoBean
    MainHomeFacade mainHomeFacade;

    @MockitoBean
    TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private static List<FieldDescriptor> profileSettingsResponseFields() {
        return List.of(
                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                fieldWithPath("body.userId").type(JsonFieldType.NUMBER).description("사용자 ID"),
                fieldWithPath("body.name").type(JsonFieldType.STRING).description("이름"),
                fieldWithPath("body.nickname").type(JsonFieldType.STRING).description("닉네임"),
                fieldWithPath("body.email").type(JsonFieldType.STRING).description("이메일"),
                fieldWithPath("body.role").type(JsonFieldType.STRING).description("역할 (DEVELOPER, DESIGNER, PLANNER, MARKETER)").optional(),
                fieldWithPath("body.profileImageUrl").type(JsonFieldType.STRING).description("프로필 사진 URL").optional(),
                fieldWithPath("body.bio").type(JsonFieldType.STRING).description("자기소개").optional(),
                fieldWithPath("body.coreCompetencies").type(JsonFieldType.STRING).description("핵심 역량").optional(),
                fieldWithPath("body.userStatus").type(JsonFieldType.STRING).description("사용자 상태 (예: 재학중, 구직중, 재직중)").optional(),
                fieldWithPath("body.isPublicMatching").type(JsonFieldType.BOOLEAN).description("공개 매칭 여부"),
                fieldWithPath("body.careerDuration").type(JsonFieldType.STRING).description("경력 기간").optional(),
                fieldWithPath("body.interestedJob").type(JsonFieldType.STRING).description("관심 직무").optional(),
                fieldWithPath("body.interestedField").type(JsonFieldType.STRING).description("관심 직종").optional(),
                fieldWithPath("body.careers").type(JsonFieldType.ARRAY).description("경력 목록"),
                fieldWithPath("body.careers[].userCareerId").type(JsonFieldType.NUMBER).description("경력 ID"),
                fieldWithPath("body.careers[].projectName").type(JsonFieldType.STRING).description("프로젝트명"),
                fieldWithPath("body.careers[].industryField").type(JsonFieldType.STRING).description("산업 분야"),
                fieldWithPath("body.careers[].startDate").type(JsonFieldType.STRING).description("시작일"),
                fieldWithPath("body.careers[].endDate").type(JsonFieldType.STRING).description("종료일").optional(),
                fieldWithPath("body.careers[].isOngoing").type(JsonFieldType.BOOLEAN).description("진행중 여부"),
                fieldWithPath("body.careers[].role").type(JsonFieldType.STRING).description("역할"),
                fieldWithPath("body.careers[].achievements").type(JsonFieldType.ARRAY).description("성과 목록"),
                fieldWithPath("body.careers[].achievements[].userAchievementId").type(JsonFieldType.NUMBER).description("성과 ID"),
                fieldWithPath("body.careers[].achievements[].title").type(JsonFieldType.STRING).description("성과 제목"),
                fieldWithPath("body.careers[].achievements[].content").type(JsonFieldType.STRING).description("성과 내용"),
                fieldWithPath("body.portfolios").type(JsonFieldType.ARRAY).description("포트폴리오 목록"),
                fieldWithPath("body.portfolios[].userPortfolioId").type(JsonFieldType.NUMBER).description("포트폴리오 ID"),
                fieldWithPath("body.portfolios[].title").type(JsonFieldType.STRING).description("포트폴리오 제목"),
                fieldWithPath("body.portfolios[].link").type(JsonFieldType.STRING).description("포트폴리오 링크").optional(),
                fieldWithPath("body.portfolios[].fileUrl").type(JsonFieldType.STRING).description("포트폴리오 파일 URL").optional(),
                fieldWithPath("body.projectHistories").type(JsonFieldType.ARRAY).description("프로젝트 히스토리 목록"),
                fieldWithPath("body.projectHistories[].userProjectHistoryId").type(JsonFieldType.NUMBER).description("프로젝트 히스토리 ID"),
                fieldWithPath("body.projectHistories[].projectName").type(JsonFieldType.STRING).description("프로젝트 이름"),
                fieldWithPath("body.projectHistories[].projectImage").type(JsonFieldType.STRING).description("프로젝트 이미지").optional(),
                fieldWithPath("body.projectHistories[].projectDescription").type(JsonFieldType.STRING).description("프로젝트 설명").optional(),
                fieldWithPath("body.projectHistories[].startYearMonth").type(JsonFieldType.STRING).description("시작 연월"),
                fieldWithPath("body.projectHistories[].endYearMonth").type(JsonFieldType.STRING).description("종료 연월").optional(),
                fieldWithPath("body.skills").type(JsonFieldType.ARRAY).description("스킬 목록"),
                fieldWithPath("body.skills[].category").type(JsonFieldType.STRING).description("스킬 카테고리"),
                fieldWithPath("body.skills[].categoryLabel").type(JsonFieldType.STRING).description("스킬 카테고리 라벨"),
                fieldWithPath("body.skills[].skills").type(JsonFieldType.ARRAY).description("스킬 항목 목록"),
                fieldWithPath("body.skills[].skills[].skill").type(JsonFieldType.STRING).description("스킬 코드"),
                fieldWithPath("body.skills[].skills[].skillLabel").type(JsonFieldType.STRING).description("스킬 라벨"),
                fieldWithPath("body.skills[].skills[].isSelected").type(JsonFieldType.BOOLEAN).description("선택 여부"),
                fieldWithPath("body.profileType").type(JsonFieldType.STRING).description("AI 프로필 분석 타입 (예: 기술 주도형 개발자)").optional(),
                fieldWithPath("body.tags").type(JsonFieldType.ARRAY).description("프로필 분석 키워드 태그 (예: #프로그래밍전문가, #백엔드개발자)").optional()
        );
    }

    private static List<FieldDescriptor> projectDetailResponseFields() {
        return List.of(
                fieldWithPath("status.statusCode").description("응답 상태 코드"),
                fieldWithPath("status.message").description("응답 메시지"),
                fieldWithPath("status.description").optional().description("응답 상세 설명"),

                fieldWithPath("body.defaultInfo").description("프로젝트 기본 정보"),
                fieldWithPath("body.defaultInfo.project_id").description("프로젝트 ID"),
                fieldWithPath("body.defaultInfo.project_title").description("프로젝트 제목"),
                fieldWithPath("body.defaultInfo.description").description("프로젝트 소개"),
                fieldWithPath("body.defaultInfo.planned_started_on").type(JsonFieldType.STRING).optional().description("프로젝트 시작 예정일"),
                fieldWithPath("body.defaultInfo.planned_ended_on").type(JsonFieldType.STRING).optional().description("프로젝트 종료 예정일"),
                fieldWithPath("body.defaultInfo.image_name").type(JsonFieldType.STRING).optional().description("프로젝트 이미지 파일명"),
                fieldWithPath("body.defaultInfo.recruitment_status").type(JsonFieldType.STRING).optional().description("프로젝트 모집 상태"),
                fieldWithPath("body.defaultInfo.team_roles").description("프로젝트 멤버 통계"),
                fieldWithPath("body.defaultInfo.team_roles.roles").description("Role 기준 통계 목록"),
                fieldWithPath("body.defaultInfo.team_roles.roles[].role").description("Role"),
                fieldWithPath("body.defaultInfo.team_roles.roles[].count").description("Role 인원 수"),
                fieldWithPath("body.defaultInfo.team_roles.roles[].role_fields").description("RoleField 기준 통계 목록"),
                fieldWithPath("body.defaultInfo.team_roles.roles[].role_fields[].role_field").description("RoleField"),
                fieldWithPath("body.defaultInfo.team_roles.roles[].role_fields[].count").description("RoleField 인원 수"),
                fieldWithPath("body.defaultInfo.leader").description("프로젝트 리더 정보"),
                fieldWithPath("body.defaultInfo.leader.user_id").description("리더 유저 ID"),
                fieldWithPath("body.defaultInfo.leader.name").description("리더 이름"),
                fieldWithPath("body.defaultInfo.leader.profile_image_url").type(JsonFieldType.STRING).optional().description("리더 프로필 이미지 URL"),
                fieldWithPath("body.defaultInfo.team_member_projects").description("팀원들의 다른 프로젝트 목록"),
                fieldWithPath("body.defaultInfo.team_member_projects[].project_id").description("프로젝트 ID"),
                fieldWithPath("body.defaultInfo.team_member_projects[].title").description("프로젝트 제목"),
                fieldWithPath("body.defaultInfo.team_member_projects[].description").description("프로젝트 설명"),
                fieldWithPath("body.defaultInfo.team_member_projects[].image_name").type(JsonFieldType.STRING).optional().description("프로젝트 이미지 파일명"),
                fieldWithPath("body.defaultInfo.team_member_projects[].created_at").description("프로젝트 생성일"),
                fieldWithPath("body.defaultInfo.team_member_projects[].ended_at").type(JsonFieldType.STRING).optional().description("프로젝트 종료일"),

                fieldWithPath("body.fields").description("프로젝트 분야"),
                fieldWithPath("body.fields.project_id").description("프로젝트 ID"),
                fieldWithPath("body.fields.fields").type(JsonFieldType.ARRAY).description("프로젝트 분야 목록"),
                fieldWithPath("body.fields.fields[].field_name").type(JsonFieldType.STRING).optional().description("분야 이름"),
                fieldWithPath("body.fields.fields[].is_selected").type(JsonFieldType.BOOLEAN).optional().description("선택 여부"),

                fieldWithPath("body.purposes").description("프로젝트 목표"),
                fieldWithPath("body.purposes.project_id").description("프로젝트 ID"),
                fieldWithPath("body.purposes.values").description("프로젝트 목표 목록"),

                fieldWithPath("body.functions").description("주요 기능"),
                fieldWithPath("body.functions.project_id").description("프로젝트 ID"),
                fieldWithPath("body.functions.values").description("주요 기능 목록"),

                fieldWithPath("body.serviceUsers").description("서비스 사용자"),
                fieldWithPath("body.serviceUsers.project_id").description("프로젝트 ID"),
                fieldWithPath("body.serviceUsers.values").description("서비스 사용자 목록"),

                fieldWithPath("body.planFiles").description("기획 파일 목록"),
                fieldWithPath("body.planFiles.project_id").description("프로젝트 ID"),
                fieldWithPath("body.planFiles.files").description("기획 파일 목록"),
                fieldWithPath("body.planFiles.files[].plan_file_id").description("기획 파일 ID"),
                fieldWithPath("body.planFiles.files[].name").description("기획 파일 이름"),
                fieldWithPath("body.planFiles.files[].file_name").description("파일명"),
                fieldWithPath("body.planFiles.files[].plan_file_type").description("기획 파일 타입"),
                fieldWithPath("body.planFiles.files[].file_ext").description("파일 확장자")
        );
    }

    @BeforeEach
    void setUpAuth() {
        doNothing().when(jwtUtil).validateToken(anyString());
        given(tokenBlacklistService.isBlacklisted(anyString())).willReturn(false);
        given(jwtUtil.getUserIdFromToken(anyString())).willReturn(1L);
        given(userDetailsService.loadUserByUsername(anyString())).willReturn(
                UserDetailsImpl.builder()
                        .userId(1L)
                        .roles(List.of("ROLE_MEMBER"))
                        .build()
        );
    }

    @Test
    void requestMatchingByUser() throws Exception {
        MatchingReqDto.matchingReqDto reqDto = new MatchingReqDto.matchingReqDto(RoleField.BACKEND, null);

        given(matchingFacade.createUserToProjectMatching(anyLong(), eq(1L), eq(reqDto)))
                .willReturn(
                        MatchingResDto.MatchingRes.builder()
                                .id(1L)
                                .requestUserId(1L)
                                .targetUserId(1L)
                                .projectId(1L)
                                .field(RoleField.BACKEND)
                                .customField(null)
                                .matchingStatus(MatchingStatus.PENDING)
                                .requestType(MatchingRequestType.USER_TO_PROJECT)
                                .expiresAt(LocalDateTime.parse("2026-01-26T12:30:00"))
                                .build()
                );

        mockMvc.perform(post("/api/v1/matchings/projects/{projectId}", 1L)
                        .with(csrf())
                        .header("Authorization", "Bearer AccessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"field\":\"BACKEND\"}"))
                .andExpect(status().isOk())
                .andDo(document("matching-request-user-to-project",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Matching")
                                .summary("유저 -> 프로젝트 매칭 요청")
                                .description("유저가 특정 프로젝트의 특정 분야(field)에 매칭을 요청합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("projectId").description("프로젝트 ID")
                                )
                                .requestFields(
                                        fieldWithPath("field").type(JsonFieldType.STRING).description("요청 분야")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.id").description("매칭 ID"),
                                        fieldWithPath("body.requestUserId").description("요청자 유저 ID"),
                                        fieldWithPath("body.targetUserId").description("대상 유저 ID"),
                                        fieldWithPath("body.projectId").description("프로젝트 ID"),
                                        fieldWithPath("body.field").description("분야"),
                                        fieldWithPath("body.customField").description("커스텀 분야(default = null)"),
                                        fieldWithPath("body.matchingStatus").description("매칭 상태"),
                                        fieldWithPath("body.requestType").description("요청 타입"),
                                        fieldWithPath("body.expiresAt").description("만료 시각")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void requestMatchingByProject() throws Exception{
        MatchingReqDto.matchingReqDto reqDto = new MatchingReqDto.matchingReqDto(RoleField.BACKEND, null);

        given(matchingFacade.createProjectToUserMatching(anyLong(), eq(1L), eq(1L), eq(reqDto)))
                .willReturn(
                        MatchingResDto.MatchingRes.builder()
                                .id(1L)
                                .requestUserId(1L)
                                .targetUserId(1L)
                                .projectId(1L)
                                .field(RoleField.BACKEND)
                                .customField(null)
                                .matchingStatus(MatchingStatus.PENDING)
                                .requestType(MatchingRequestType.PROJECT_TO_USER)
                                .expiresAt(LocalDateTime.parse("2026-01-26T12:30:00"))
                                .build()
                );

        mockMvc.perform(post("/api/v1/matchings/projects/{projectId}/users/{targetUserId}", 1L, 1L)
                        .with(csrf())
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"field\": \"BACKEND\"}"))
                .andExpect(status().isOk())
                .andDo(document("matching-request-project-to-user",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Matching")
                                .summary("프로젝트 -> 유저 매칭 요청")
                                .description("프로젝트의 리더가 특정 유저를 특정 분야에 매칭을 요청합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("projectId").description("프로젝트 ID"),
                                        parameterWithName("targetUserId").description("요청받는 유저 ID")
                                )
                                .requestFields(
                                        fieldWithPath("field").type(JsonFieldType.STRING).description("요청 분야")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.id").description("매칭 ID"),
                                        fieldWithPath("body.requestUserId").description("요청자 유저 ID"),
                                        fieldWithPath("body.targetUserId").description("대상 유저 ID"),
                                        fieldWithPath("body.projectId").description("프로젝트 ID"),
                                        fieldWithPath("body.field").description("분야"),
                                        fieldWithPath("body.customField").description("커스텀 분야(default = null)"),
                                        fieldWithPath("body.matchingStatus").description("매칭 상태"),
                                        fieldWithPath("body.requestType").description("요청 타입"),
                                        fieldWithPath("body.expiresAt").description("만료 시각")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void cancelMatchingRequest() throws Exception{
        given(matchingFacade.cancelMatching(eq(1L), anyLong()))
                .willReturn(
                        MatchingResDto.MatchingRes.builder()
                                .id(1L)
                                .requestUserId(1L)
                                .targetUserId(1L)
                                .projectId(1L)
                                .field(RoleField.BACKEND)
                                .customField(null)
                                .matchingStatus(MatchingStatus.CANCELED)
                                .requestType(MatchingRequestType.USER_TO_PROJECT)
                                .expiresAt(LocalDateTime.parse("2026-01-26T12:30:00"))
                                .build()
                );

        mockMvc.perform(post("/api/v1/matchings/{matchingId}/cancel", 1L)
                        .with(csrf())
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("matching-cancel",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Matching")
                                .summary("매칭 취소 (유저 -> 프로젝트, 프로젝트 -> 유저 범용 API)")
                                .description("매칭 요청을 한 유저가 해당 매칭을 취소합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("matchingId").description("매칭 ID")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.id").description("매칭 ID"),
                                        fieldWithPath("body.requestUserId").description("요청자 유저 ID"),
                                        fieldWithPath("body.targetUserId").description("대상 유저 ID"),
                                        fieldWithPath("body.projectId").description("프로젝트 ID"),
                                        fieldWithPath("body.field").description("분야"),
                                        fieldWithPath("body.customField").description("커스텀 분야(default = null)"),
                                        fieldWithPath("body.matchingStatus").description("매칭 상태"),
                                        fieldWithPath("body.requestType").description("요청 타입"),
                                        fieldWithPath("body.expiresAt").description("만료 시각")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void acceptMatchingRequest() throws Exception{
        MatchingResDto.MatchingAcceptResDto dto = MatchingResDto.MatchingAcceptResDto.builder()
                        .matching(MatchingResDto.MatchingRes.builder()
                                .id(1L)
                                .requestUserId(1L)
                                .targetUserId(1L)
                                .projectId(1L)
                                .field(RoleField.BACKEND)
                                .customField(null)
                                .matchingStatus(MatchingStatus.ACCEPTED)
                                .requestType(MatchingRequestType.PROJECT_TO_USER)
                                .expiresAt(LocalDateTime.parse("2026-01-26T12:30:00"))
                                .build())
                        .projectUser(ProjectUserResDto.builder()
                                .id(1L)
                                .userId(1L)
                                .projectId(1L)
                                .field(RoleField.BACKEND)
                                .memberType(ProjectMemberType.MEMBER)
                                .memberStatus(ProjectMemberStatus.ACTIVE)
                                .build())
                        .build();

        given(matchingFacade.acceptMatchingRequest(eq(1L), anyLong())).willReturn(dto);

        mockMvc.perform(post("/api/v1/matchings/{matchingId}/accept", 1L)
                        .with(csrf())
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("matching-accept",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Matching")
                                .summary("매칭 수락 (유저 -> 프로젝트, 프로젝트 -> 유저 범용 API)")
                                .description("매칭 요청을 받은 주체(회원, 리더)가 요청을 수락합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("matchingId").description("매칭 ID")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.matching.id").description("매칭 ID"),
                                        fieldWithPath("body.matching.requestUserId").description("요청자 유저 ID"),
                                        fieldWithPath("body.matching.targetUserId").description("대상 유저 ID"),
                                        fieldWithPath("body.matching.projectId").description("프로젝트 ID"),
                                        fieldWithPath("body.matching.field").description("분야"),
                                        fieldWithPath("body.matching.customField").description("커스텀 분야(default = null)"),
                                        fieldWithPath("body.matching.matchingStatus").description("매칭 상태"),
                                        fieldWithPath("body.matching.requestType").description("요청 타입"),
                                        fieldWithPath("body.matching.expiresAt").description("만료 시각"),

                                        fieldWithPath("body.projectUser.id").description("프로젝트 멤버 ID"),
                                        fieldWithPath("body.projectUser.userId").description("유저 ID"),
                                        fieldWithPath("body.projectUser.projectId").description("프로젝트 ID"),
                                        fieldWithPath("body.projectUser.field").description("분야"),
                                        fieldWithPath("body.projectUser.memberType").description("멤버 타입"),
                                        fieldWithPath("body.projectUser.memberStatus").description("멤버 상태")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void rejectMatchingRequest() throws Exception{
        given(matchingFacade.rejectMatching(eq(1L), anyLong(), eq(MatchingRejectReason.OTHER)))
                .willReturn(
                        MatchingResDto.MatchingRes.builder()
                                .id(1L)
                                .requestUserId(1L)
                                .targetUserId(1L)
                                .projectId(1L)
                                .field(RoleField.BACKEND)
                                .customField(null)
                                .matchingStatus(MatchingStatus.REJECTED)
                                .requestType(MatchingRequestType.PROJECT_TO_USER)
                                .expiresAt(LocalDateTime.parse("2026-01-26T12:30:00"))
                                .build()
                );

        mockMvc.perform(post("/api/v1/matchings/{matchingId}/reject", 1L)
                        .with(csrf())
                        .header("Authorization", "Bearer AccessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rejectReason\":\"OTHER\"}"))
                .andExpect(status().isOk())
                .andDo(document("matching-reject",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Matching")
                                .summary("매칭 거절 (유저 -> 프로젝트, 프로젝트 -> 유저 범용 API)")
                                .description("매칭 요청을 받은 주체(회원, 리더)가 요청을 거절합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("matchingId").description("매칭 ID")
                                )
                                .requestFields(
                                        fieldWithPath("rejectReason").type(JsonFieldType.STRING).description("거절 사유")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.id").description("매칭 ID"),
                                        fieldWithPath("body.requestUserId").description("요청자 유저 ID"),
                                        fieldWithPath("body.targetUserId").description("대상 유저 ID"),
                                        fieldWithPath("body.projectId").description("프로젝트 ID"),
                                        fieldWithPath("body.field").description("분야"),
                                        fieldWithPath("body.customField").description("커스텀 분야(default = null)"),
                                        fieldWithPath("body.matchingStatus").description("매칭 상태"),
                                        fieldWithPath("body.requestType").description("요청 타입"),
                                        fieldWithPath("body.expiresAt").description("만료 시각")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void getReceivedMatchingsByProject() throws Exception {
        MatchingResDto.ProjectSummary projectSummary = MatchingResDto.ProjectSummary.builder()
                .projectId(1L)
                .title("NECT")
                .description("Project description")
                .imageUrl("https://example.com/image.jpg")
                .currentMembersNum(3)
                .totalMemberNum(12)
                .build();

        MatchingResDto.UserSummary userSummary = MatchingResDto.UserSummary.builder()
                .userId(1L)
                .nickname("seoyeon")
                .bio("Designer")
                .field(RoleField.BACKEND)
                .customField(null)
                .profileUrl("https://example.com/image.jpg")
                .build();

        MatchingResDto.MatchingListRes dto = MatchingResDto.MatchingListRes.builder()
                .counterParty(CounterParty.PROJECT)
                .userMatchings(java.util.List.of(userSummary))
                .projectMatchings(java.util.List.of(projectSummary))
                .build();

        given(matchingService.getReceivedMatchingsByTarget(anyLong(), eq(CounterParty.PROJECT), eq(MatchingStatus.PENDING)))
                .willReturn(dto);

        mockMvc.perform(get("/api/v1/matchings/received")
                        .param("target", "project")
                        .param("status", "pending")
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("matching-get-received",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Matching")
                                .summary("받은 매칭 요청")
                                .description("target에 해당되는 받은(수신) 매칭 요청을 조회합니다. ")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .queryParameters(
                                        parameterWithName("target").description("조회 대상 (project | user)"),
                                        parameterWithName("status").description("매칭 상태 (pending | accepted | rejected | canceled | expired)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.counterParty").description("대상 타입 (PROJECT | USER)"),

                                        fieldWithPath("body.userMatchings").description("유저 매칭 요약 목록(대상이 USER일 때 채워짐)"),
                                        fieldWithPath("body.userMatchings[].userId").description("유저 ID"),
                                        fieldWithPath("body.userMatchings[].nickname").description("닉네임"),
                                        fieldWithPath("body.userMatchings[].bio").description("한줄 소개"),
                                        fieldWithPath("body.userMatchings[].field").description("분야"),
                                        fieldWithPath("body.userMatchings[].customField").description("커스텀 분야 (분야가 CUSTOM일 때)"),
                                        fieldWithPath("body.userMatchings[].profileUrl").description("프로필 URL"),

                                        fieldWithPath("body.projectMatchings").description("프로젝트 매칭 요약 목록(대상이 PROJECT일 때 채워짐)"),
                                        fieldWithPath("body.projectMatchings[].projectId").description("프로젝트 ID"),
                                        fieldWithPath("body.projectMatchings[].title").description("프로젝트 제목"),
                                        fieldWithPath("body.projectMatchings[].description").description("프로젝트 설명"),
                                        fieldWithPath("body.projectMatchings[].imageUrl").description("프로젝트 대표 이미지"),
                                        fieldWithPath("body.projectMatchings[].currentMembersNum").description("현재 멤버 수"),
                                        fieldWithPath("body.projectMatchings[].totalMemberNum").description("프로젝트가 필요로 하는 총 멤버 수")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void getSentMatchingsByUser() throws Exception {
        UserDetailsImpl testUser = new UserDetailsImpl(1L, Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());

        MatchingResDto.UserSummary userSummary = MatchingResDto.UserSummary.builder()
                .userId(1L)
                .nickname("seoyeon")
                .bio("Designer")
                .field(RoleField.BACKEND)
                .customField(null)
                .profileUrl("https://example.com/avatar.jpg")
                .build();

        MatchingResDto.ProjectSummary projectSummary = MatchingResDto.ProjectSummary.builder()
                .projectId(1L)
                .title("NECT")
                .description("Project description")
                .imageUrl("https://example.com/image.jpg")
                .currentMembersNum(3)
                .totalMemberNum(12)
                .build();

        MatchingResDto.MatchingListRes dto = MatchingResDto.MatchingListRes.builder()
                .counterParty(CounterParty.USER)
                .userMatchings(java.util.List.of(userSummary))
                .projectMatchings(java.util.List.of(projectSummary))
                .build();

        given(matchingService.getSentMatchingsByTarget(anyLong(), eq(CounterParty.USER), eq(MatchingStatus.PENDING)))
                .willReturn(dto);

        mockMvc.perform(get("/api/v1/matchings/sent")
                        .param("target", "user")
                        .param("status", "pending")
                        .with(authentication(authentication))
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("matching-get-sent",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Matching")
                                .summary("보낸 매칭 요청")
                                .description("보낸(발신) 매칭 요청을 조회합니다. target에 해당되는 매칭 요청만 조회됩니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .queryParameters(
                                        parameterWithName("target").description("조회 대상 (project | user)"),
                                        parameterWithName("status").description("매칭 상태 (pending | accepted | rejected | canceled | expired)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.counterParty").description("대상 타입 (PROJECT | USER)"),

                                        fieldWithPath("body.userMatchings").description("유저 매칭 요약 목록(대상이 USER일 때 채워짐)"),
                                        fieldWithPath("body.userMatchings[].userId").description("유저 ID"),
                                        fieldWithPath("body.userMatchings[].nickname").description("닉네임"),
                                        fieldWithPath("body.userMatchings[].bio").description("한줄 소개"),
                                        fieldWithPath("body.userMatchings[].field").description("분야"),
                                        fieldWithPath("body.userMatchings[].customField").description("커스텀 분야 (분야가 CUSTOM일 때)"),
                                        fieldWithPath("body.userMatchings[].profileUrl").description("프로필 URL"),

                                        fieldWithPath("body.projectMatchings").description("프로젝트 매칭 요약 목록(대상이 PROJECT일 때 채워짐)"),
                                        fieldWithPath("body.projectMatchings[].projectId").description("프로젝트 ID"),
                                        fieldWithPath("body.projectMatchings[].title").description("프로젝트 제목"),
                                        fieldWithPath("body.projectMatchings[].description").description("프로젝트 설명"),
                                        fieldWithPath("body.projectMatchings[].imageUrl").description("프로젝트 대표 이미지"),
                                        fieldWithPath("body.projectMatchings[].currentMembersNum").description("현재 멤버 수"),
                                        fieldWithPath("body.projectMatchings[].totalMemberNum").description("프로젝트가 필요로 하는 총 멤버 수")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void getMatchingsCount() throws Exception {
        UserDetailsImpl testUser = new UserDetailsImpl(1L, Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());

        MatchingResDto.MatchingCounts counts = MatchingResDto.MatchingCounts.builder()
                .receivedCount(2)
                .sentCount(5)
                .build();

        given(matchingService.getMatchingsCount(anyLong())).willReturn(counts);

        mockMvc.perform(get("/api/v1/matchings/count")
                        .with(authentication(authentication))
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("matching-get-count",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Matching")
                                .summary("매칭 요청 개수 조회")
                                .description("보낸/받은 매칭(PENDING) 개수를 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.receivedCount").description("받은(PENDING) 요청 수"),
                                        fieldWithPath("body.sentCount").description("보낸(PENDING) 요청 수")
                                )
                                .build()
                        )
                ));
    }

    private ProfileSettingsDto.ProfileSettingsResponseDto mockProfileSettingsResponse() {
        return new ProfileSettingsDto.ProfileSettingsResponseDto(
                21L,
                "이영희",
                "lee-0",
                "lee@example.com",
                "DESIGNER",
                "https://example.com/profile/21.png",
                "사용자 경험 중심의 디자인을 지향합니다.",
                "Figma, UX Research",
                "JOB_SEEKING",
                true,
                "1년",
                "UI/UX 디자이너",
                "IT/웹모바일",
                List.of(new ProfileSettingsDto.CareerDto(
                        1L,
                        "UX 프로젝트",
                        "IT",
                        "2023.01",
                        "2023.12",
                        false,
                        "디자이너",
                        List.of(new ProfileSettingsDto.AchievementDto(1L, "성과", "사용자 만족도 20% 향상"))
                )),
                List.of(new ProfileSettingsDto.PortfolioDto(
                        1L,
                        "디자인 포트폴리오",
                        "https://example.com/portfolio",
                        "https://example.com/portfolio.pdf"
                )),
                List.of(new ProfileSettingsDto.ProjectHistoryDto(
                        1L,
                        "커머스 리디자인",
                        "https://example.com/project.png",
                        "커머스 UX 개선",
                        "2023.01",
                        "2023.06"
                )),
                List.of(new ProfileSettingsDto.SkillDto(
                        "DESIGN",
                        "디자인",
                        List.of(new ProfileSettingsDto.SkillItemDto("FIGMA", "Figma", true))
                )),
                "디자인 주도형",
                List.of("#UX", "#디자인")
        );
    }

    @Test
    void 매칭_요청_보낸_받은_넥터_상세조회_API() throws Exception {
        given(mypageService.getProfile(21L))
                .willReturn(mockProfileSettingsResponse());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/matchings/users/{userId}", 21L)
                        .header("Authorization", "Bearer AccessToken")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(MockMvcRestDocumentationWrapper.document("matching-user-details",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Matching")
                                .summary("유저 상세 조회 모달")
                                .description("요청을 보낸/받은 유저의 상세 정보를 불러옵니다.")
                                .requestHeaders(
                                        ResourceDocumentation.headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        ResourceDocumentation.parameterWithName("userId").description("유저 ID")
                                )
                                .responseFields(profileSettingsResponseFields())
                                .build()
                        )
                ));
    }

    private HomeProjectDetailResponse mockProjectDetailResponse() {
        MyProjectsResponseDto.ProjectInfo projectInfo = MyProjectsResponseDto.ProjectInfo.builder()
                .projectId(10L)
                .projectTitle("AI 협업툴 개발")
                .description("팀 협업 효율을 높이는 AI 기반 협업툴 프로젝트입니다.")
                .plannedStartedOn(LocalDate.of(2025, 9, 1))
                .plannedEndedOn(LocalDate.of(2026, 2, 1))
                .imageName("project-10.png")
                .recruitmentStatus(RecruitmentStatus.OPEN)
                .teamRoles(mockProjectMemberStatistics())
                .leader(MyProjectsResponseDto.LeaderInfo.builder()
                        .userId(1L)
                        .name("홍길동")
                        .profileImageUrl("https://example.com/profile/1.png")
                        .build())
                .teamMemberProjects(List.of(
                        MyProjectsResponseDto.TeamMemberProjectInfo.builder()
                                .projectId(99L)
                                .title("모바일 일정 관리")
                                .description("개인 맞춤 일정 관리 앱을 개발합니다.")
                                .imageName("project-99.png")
                                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                                .endedAt(LocalDateTime.of(2024, 12, 31, 23, 59))
                                .build()
                ))
                .build();

        MyProjectsResponseDto.ProjectFieldResponse fields = sampleProjectFields(10L);

        MyProjectsResponseDto.StringListResponse purposes = new MyProjectsResponseDto.StringListResponse(
                10L,
                List.of("협업 효율 개선", "AI 기반 자동화")
        );

        MyProjectsResponseDto.StringListResponse functions = new MyProjectsResponseDto.StringListResponse(
                10L,
                List.of("태스크 자동 분류", "회의 요약")
        );

        MyProjectsResponseDto.StringListResponse serviceUsers = new MyProjectsResponseDto.StringListResponse(
                10L,
                List.of("프로덕트 팀", "개발 팀")
        );

        MyProjectsResponseDto.ProjectPlanFilesResponse planFiles = new MyProjectsResponseDto.ProjectPlanFilesResponse(
                10L,
                List.of(
                        new MyProjectsResponseDto.ProjectPlanFileInfo(
                                1L,
                                "기획서",
                                "plan.pdf",
                                PlanFileType.FILE,
                                FileExt.PDF
                        )
                )
        );

        return HomeProjectDetailResponse.builder()
                .defaultInfo(projectInfo)
                .fields(fields)
                .purposes(purposes)
                .functions(functions)
                .serviceUsers(serviceUsers)
                .planFiles(planFiles)
                .build();
    }

    @Test
    void 매칭_요청_보낸_받은_프로젝트_상세조회_API() throws Exception {
        given(mainHomeFacade.getRecruitingProjectsDetails(eq(10L)))
                .willReturn(mockProjectDetailResponse());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/matchings/projects/{projectId}", 10L)
                        .header("Authorization", "Bearer AccessToken")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(MockMvcRestDocumentationWrapper.document("matching-project-details",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Matching")
                                .summary("프로젝트 상세 조회 모달")
                                .description("매칭 요청을 받은/보낸 프로젝트의 상세 정보를 조회합니다.")
                                .requestHeaders(
                                        ResourceDocumentation.headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID")
                                )
                                .responseFields(projectDetailResponseFields())
                                .build()
                        )
                ));
    }

    private MyProjectsResponseDto.ProjectFieldResponse sampleProjectFields(Long projectId) {
        try {
            var interestInfoClass = Arrays.stream(MyProjectsResponseDto.class.getDeclaredClasses())
                    .filter(c -> "InterestInfo".equals(c.getSimpleName()))
                    .findFirst()
                    .orElseThrow();
            var interestCtor = interestInfoClass.getDeclaredConstructor(String.class, Boolean.class);
            interestCtor.setAccessible(true);
            Object interestInfo = interestCtor.newInstance("IT/웹모바일", true);

            var ctor = MyProjectsResponseDto.ProjectFieldResponse.class
                    .getDeclaredConstructor(Long.class, List.class);
            ctor.setAccessible(true);
            return ctor.newInstance(projectId, List.of(interestInfo));
        } catch (Exception e) {
            throw new IllegalStateException("failed to create ProjectFieldResponse", e);
        }
    }

    private ProjectMemberStatisticResponse mockProjectMemberStatistics() {
        return new ProjectMemberStatisticResponse(List.of(
                new ProjectMemberStatisticResponse.RoleStatistic(
                        Role.PLANNER,
                        1,
                        List.of(new ProjectMemberStatisticResponse.RoleFieldStatistic(RoleField.SERVICE, 1))
                ),
                new ProjectMemberStatisticResponse.RoleStatistic(
                        Role.DESIGNER,
                        2,
                        List.of(new ProjectMemberStatisticResponse.RoleFieldStatistic(RoleField.UI_UX, 2))
                ),
                new ProjectMemberStatisticResponse.RoleStatistic(
                        Role.DEVELOPER,
                        3,
                        List.of(new ProjectMemberStatisticResponse.RoleFieldStatistic(RoleField.BACKEND, 3))
                ),
                new ProjectMemberStatisticResponse.RoleStatistic(
                        Role.MARKETER,
                        0,
                        List.of()
                ),
                new ProjectMemberStatisticResponse.RoleStatistic(
                        Role.OTHER,
                        0,
                        List.of()
                )
        ));
    }
}
