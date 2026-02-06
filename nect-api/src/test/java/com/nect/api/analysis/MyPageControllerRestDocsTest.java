package com.nect.api.analysis;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.mypage.dto.MyProjectsResponseDto;
import com.nect.api.domain.mypage.service.MyPageProjectService;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
import com.nect.core.entity.user.enums.RoleField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
@DisplayName("마이페이지 프로젝트 조회 API 문서화 테스트")
class MyPageControllerRestDocsTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MyPageProjectService myPageProjectService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    private static final String AUTH_HEADER = "Authorization";
    private static final String TEST_ACCESS_TOKEN = "Bearer AccessToken";

    @BeforeEach
    void setUpAuth() {
        doNothing().when(jwtUtil).validateToken(anyString());
        given(tokenBlacklistService.isBlacklisted(anyString())).willReturn(false);
        given(jwtUtil.getUserIdFromToken(anyString())).willReturn(1L);
        given(userDetailsService.loadUserByUsername(anyString())).willReturn(
                UserDetailsImpl.builder()
                        .userId(1L)
                        .roles(List.of("ROLE_USER"))
                        .build()
        );
    }

    @Test
    @DisplayName("마이페이지 프로젝트 목록 조회 API")
    void getMyProjects() throws Exception {
        // given
        MyProjectsResponseDto.TeamRoleInfo teamRole1 = MyProjectsResponseDto.TeamRoleInfo.builder()
                .roleField(RoleField.FRONTEND)
                .requiredCount(2)
                .build();

        MyProjectsResponseDto.TeamRoleInfo teamRole2 = MyProjectsResponseDto.TeamRoleInfo.builder()
                .roleField(RoleField.BACKEND)
                .requiredCount(2)
                .build();

        MyProjectsResponseDto.LeaderInfo leader = MyProjectsResponseDto.LeaderInfo.builder()
                .userId(100L)
                .name("김팀장")
                .profileImageUrl("https://example.com/profile/100.jpg")
                .build();

        MyProjectsResponseDto.TeamMemberProjectInfo teamMemberProject1 =
                MyProjectsResponseDto.TeamMemberProjectInfo.builder()
                        .projectId(5L)
                        .title("커뮤니티 앱 개발")
                        .description("지역 기반 커뮤니티 플랫폼")
                        .imageName("project_image_005.jpg")
                        .createdAt(LocalDateTime.of(2025, 10, 15, 9, 0))
                        .endedAt(LocalDateTime.of(2026, 1, 20, 18, 0))
                        .build();

        MyProjectsResponseDto.TeamMemberProjectInfo teamMemberProject2 =
                MyProjectsResponseDto.TeamMemberProjectInfo.builder()
                        .projectId(8L)
                        .title("이커머스 플랫폼")
                        .description("중소기업 전용 온라인 쇼핑몰")
                        .imageName("project_image_008.jpg")
                        .createdAt(LocalDateTime.of(2025, 12, 1, 9, 0))
                        .endedAt(null)
                        .build();

        MyProjectsResponseDto.ProjectInfo projectInfo1 = MyProjectsResponseDto.ProjectInfo.builder()
                .projectId(1L)
                .projectTitle("스마트 런칭 플랫폼")
                .description("AI 기반 맞춤형 프로젝트 매칭 플랫폼")
                .imageName("project_image_001.jpg")
                .plannedStartedOn(LocalDate.of(2026, 3, 1))
                .plannedEndedOn(LocalDate.of(2026, 8, 31))
                .teamRoles(List.of(teamRole1, teamRole2))
                .leader(leader)
                .teamMemberProjects(List.of(teamMemberProject1, teamMemberProject2))
                .build();

        MyProjectsResponseDto.ProjectInfo projectInfo2 = MyProjectsResponseDto.ProjectInfo.builder()
                .projectId(3L)
                .projectTitle("헬스케어 매니지먼트")
                .description("개인 맞춤 건강관리 서비스")
                .imageName("project_image_003.jpg")
                .plannedStartedOn(LocalDate.of(2026, 2, 15))
                .plannedEndedOn(LocalDate.of(2026, 7, 15))
                .teamRoles(List.of(
                        MyProjectsResponseDto.TeamRoleInfo.builder()
                                .roleField(RoleField.BACKEND)
                                .requiredCount(1)
                                .build(),
                        MyProjectsResponseDto.TeamRoleInfo.builder()
                                .roleField(RoleField.BACKEND)
                                .requiredCount(1)
                                .build()
                ))
                .leader(MyProjectsResponseDto.LeaderInfo.builder()
                        .userId(200L)
                        .name("이리더")
                        .profileImageUrl("https://example.com/profile/200.jpg")
                        .build())
                .teamMemberProjects(List.of(
                        MyProjectsResponseDto.TeamMemberProjectInfo.builder()
                                .projectId(7L)
                                .title("피트니스 트래커")
                                .description("운동 기록 및 분석 앱")
                                .imageName("project_image_007.jpg")
                                .createdAt(LocalDateTime.of(2025, 11, 10, 9, 0))
                                .endedAt(LocalDateTime.of(2026, 2, 10, 18, 0))
                                .build()
                ))
                .build();

        MyProjectsResponseDto response = MyProjectsResponseDto.builder()
                .projects(List.of(projectInfo1, projectInfo2))
                .build();

        given(myPageProjectService.getMyProjects(eq(1L))).willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/api/v1/mypage/projects")
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.statusCode").value("C000"))
                .andExpect(jsonPath("$.status.message").value("success"))
                .andExpect(jsonPath("$.body.projects").isArray())
                .andExpect(jsonPath("$.body.projects[0].project_id").value(1))
                .andExpect(jsonPath("$.body.projects[0].project_title").value("스마트 런칭 플랫폼"))
                .andExpect(jsonPath("$.body.projects[0].description").value("AI 기반 맞춤형 프로젝트 매칭 플랫폼"))
                .andExpect(jsonPath("$.body.projects[0].team_roles").isArray())
                .andExpect(jsonPath("$.body.projects[0].leader.user_id").value(100))
                .andExpect(jsonPath("$.body.projects[0].team_member_projects").isArray())
                .andDo(document("mypage-projects-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("마이페이지")
                                .summary("마이페이지 프로젝트 목록 조회 API")
                                .description("현재 사용자가 참여 중인 모든 프로젝트 목록을 조회합니다. " +
                                        "각 프로젝트의 기본 정보, 팀 구성, 리더 정보, 팀원들의 다른 프로젝트 정보를 포함합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),

                                        // projects 배열
                                        fieldWithPath("body.projects[]").description("프로젝트 목록"),
                                        fieldWithPath("body.projects[].project_id").description("프로젝트 ID"),
                                        fieldWithPath("body.projects[].project_title").description("프로젝트 제목"),
                                        fieldWithPath("body.projects[].description").description("프로젝트 설명"),
                                        fieldWithPath("body.projects[].image_name").description("프로젝트 이미지 파일명"),
                                        fieldWithPath("body.projects[].planned_started_on").description("예상 시작일 (YYYY-MM-DD)"),
                                        fieldWithPath("body.projects[].planned_ended_on").description("예상 종료일 (YYYY-MM-DD)"),

                                        // team_roles 배열
                                        fieldWithPath("body.projects[].team_roles[]").description("팀 역할 구성 목록"),
                                        fieldWithPath("body.projects[].team_roles[].role_field").description("역할 필드 (FRONTEND, BACKEND, DESIGNER 등)"),
                                        fieldWithPath("body.projects[].team_roles[].required_count").description("필요 인원 수"),

                                        // leader 객체
                                        fieldWithPath("body.projects[].leader").description("리더 정보").optional(),
                                        fieldWithPath("body.projects[].leader.user_id").description("리더 사용자 ID"),
                                        fieldWithPath("body.projects[].leader.name").description("리더 이름"),
                                        fieldWithPath("body.projects[].leader.profile_image_url").description("리더 프로필 이미지 URL"),

                                        // team_member_projects 배열
                                        fieldWithPath("body.projects[].team_member_projects[]").description("팀원들의 다른 프로젝트 목록"),
                                        fieldWithPath("body.projects[].team_member_projects[].project_id").description("프로젝트 ID"),
                                        fieldWithPath("body.projects[].team_member_projects[].title").description("프로젝트 제목"),
                                        fieldWithPath("body.projects[].team_member_projects[].description").description("프로젝트 설명"),
                                        fieldWithPath("body.projects[].team_member_projects[].image_name").description("프로젝트 이미지 파일명"),
                                        fieldWithPath("body.projects[].team_member_projects[].created_at").description("프로젝트 생성일시"),
                                        fieldWithPath("body.projects[].team_member_projects[].ended_at").description("프로젝트 종료일시").optional()
                                )
                                .build()
                        )
                ));
    }

}