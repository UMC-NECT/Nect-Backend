package com.nect.api.domain.home.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.domain.home.dto.HomeHeaderResponse;
import com.nect.api.domain.home.dto.HomeMemberItem;
import com.nect.api.domain.home.dto.HomeMembersResponse;
import com.nect.api.domain.home.dto.HomeProjectDetailResponse;
import com.nect.api.domain.home.dto.HomeProjectItem;
import com.nect.api.domain.home.dto.HomeProjectMembersResponse;
import com.nect.api.domain.home.dto.HomeProjectResponse;
import com.nect.api.domain.home.dto.HomeStatisticResponse;
import com.nect.api.domain.team.project.dto.ProjectMemberStatisticResponse;
import com.nect.api.domain.home.facade.MainHomeFacade;
import com.nect.api.domain.mypage.dto.MyProjectsResponseDto;
import com.nect.api.domain.mypage.dto.ProfileSettingsDto;
import com.nect.api.domain.mypage.service.MypageService;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
import com.nect.core.entity.team.enums.FileExt;
import com.nect.core.entity.team.enums.PlanFileType;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.entity.team.enums.RecruitmentStatus;
import com.nect.core.entity.user.enums.InterestField;
import com.nect.core.entity.user.enums.Role;
import com.nect.core.entity.user.enums.RoleField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MainHomeFacade mainHomeFacade;

    @MockitoBean
    private MypageService mypageService;

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
    @DisplayName("모집 중인 프로젝트 조회 API")
    void 모집_중인_프로젝트_조회_API() throws Exception {
        given(mainHomeFacade.getRecruitingProjects(eq(1L), eq(3), eq(Role.DEVELOPER), eq(InterestField.IT_WEB_MOBILE)))
                .willReturn(mockProjectResponse());

        mockMvc.perform(get("/api/v1/home/projects")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("count", "3")
                        .param("role", "DEVELOPER")
                        .param("interest", "IT_WEB_MOBILE")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("home-projects-recruiting",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Home")
                                .summary("모집 중인 프로젝트 조회")
                                .description("홈 화면에서 모집 중인 프로젝트 목록을 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .queryParameters(
                                        parameterWithName("count").description("조회할 프로젝트 개수"),
                                        parameterWithName("role")
                                                .optional()
                                                .description("필터 역할 (role과 interest는 모두 null이거나 모두 null이 아니어야 함; enum 조회는 /api/v1/enums/roles)"),
                                        parameterWithName("interest")
                                                .optional()
                                                .description("필터 관심 분야 (role과 interest는 모두 null이거나 모두 null이 아니어야 함; enum 조회는 /api/v1/enums/interest-fields)")
                                )
                                .responseFields(projectResponseFields())
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("모집 중인 프로젝트 상세 조회 API")
    void 모집_중인_프로젝트_상세_조회_API() throws Exception {
        given(mainHomeFacade.getRecruitingProjectsDetails(eq(10L)))
                .willReturn(mockProjectDetailResponse());

        mockMvc.perform(get("/api/v1/home/projects/{projectId}", 10L)
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("home-projects-recruiting-detail",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Home")
                                .summary("모집 중인 프로젝트 상세 조회")
                                .description("홈 화면에서 모집 중인 프로젝트의 상세 정보를 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("projectId").description("프로젝트 ID")
                                )
                                .responseFields(projectDetailResponseFields())
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("모집 중인 프로젝트 팀원 목록 조회 API")
    void 모집_중인_프로젝트_팀원_목록_조회_API() throws Exception {
        given(mainHomeFacade.homeReadProjectUsers(eq(10L)))
                .willReturn(mockProjectMembersResponse());

        mockMvc.perform(get("/api/v1/home/projects/{projectId}/members", 10L)
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("home-projects-recruiting-members",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Home")
                                .summary("모집 중인 프로젝트 팀원 목록 조회")
                                .description("홈 화면에서 모집 중인 프로젝트의 팀원 목록을 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("projectId").description("프로젝트 ID")
                                )
                                .responseFields(projectMembersResponseFields())
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("홈화면 프로젝트 추천 API")
    void 홈화면_프로젝트_추천_API() throws Exception {
        given(mainHomeFacade.getRecommendedProjects(eq(1L), eq(3)))
                .willReturn(mockProjectResponse());

        mockMvc.perform(get("/api/v1/home/recommendations/projects")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("count", "3")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("home-projects-recommended",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Home")
                                .summary("홈화면 프로젝트 추천")
                                .description("홈 화면에서 추천 프로젝트 목록을 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .queryParameters(
                                        parameterWithName("count").description("조회할 프로젝트 개수")
                                )
                                .responseFields(projectResponseFields())
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("홈화면 통계 조회 API")
    void 홈화면_통계_조회_API() throws Exception {
        HomeStatisticResponse response = new HomeStatisticResponse(
                120,
                65,
                40,
                3200
        );

        given(mainHomeFacade.statisticResponse()).willReturn(response);

        mockMvc.perform(get("/api/v1/home/statistics")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("home-statistics",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Home")
                                .summary("홈화면 통계 조회")
                                .description("홈 화면에 표시되는 통계 정보를 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .responseFields(
                                        fieldWithPath("status").type(JsonFieldType.OBJECT).description("응답 상태"),
                                        fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                        fieldWithPath("status.message").type(JsonFieldType.STRING).description("메시지"),
                                        fieldWithPath("status.description").optional().type(JsonFieldType.STRING).description("상세 설명"),

                                        fieldWithPath("body").type(JsonFieldType.OBJECT).description("응답 바디"),
                                        fieldWithPath("body.totalProjectCount").type(JsonFieldType.NUMBER).description("전체 프로젝트 수"),
                                        fieldWithPath("body.matchingSuccessRate").type(JsonFieldType.NUMBER).description("매칭 성공률(%)"),
                                        fieldWithPath("body.reParticipateRate").type(JsonFieldType.NUMBER).description("재참여율(%)"),
                                        fieldWithPath("body.totalUserCount").type(JsonFieldType.NUMBER).description("전체 사용자 수")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("홈화면 매칭 가능한 넥터 API")
    void 홈화면_매칭_가능한_넥터_API() throws Exception {
        given(mainHomeFacade.getMatchableMembers(eq(1L), eq(3), eq(Role.DEVELOPER), eq(InterestField.IT_WEB_MOBILE)))
                .willReturn(mockMembersResponse());

        mockMvc.perform(get("/api/v1/home/members")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("count", "3")
                        .param("role", "DEVELOPER")
                        .param("interest", "IT_WEB_MOBILE")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("home-members-matchable",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Home")
                                .summary("홈화면 매칭 가능한 넥터 조회")
                                .description("홈 화면에서 매칭 가능한 넥터 목록을 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .queryParameters(
                                        parameterWithName("count").description("조회할 넥터 개수"),
                                        parameterWithName("role")
                                                .optional()
                                                .description("필터 역할 (role과 interest는 모두 null이거나 모두 null이 아니어야 함; enum 조회는 /api/v1/enums/roles)"),
                                        parameterWithName("interest")
                                                .optional()
                                                .description("필터 관심 분야 (role과 interest는 모두 null이거나 모두 null이 아니어야 함; enum 조회는 /api/v1/enums/interest-fields)")
                                )
                                .responseFields(memberResponseFields())
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("홈화면 매칭 가능한 넥터 상세조회 API")
    void 홈화면_매칭_가능한_넥터_상세조회_API() throws Exception {
        given(mypageService.getProfile(21L))
                .willReturn(mockProfileSettingsResponse());

        mockMvc.perform(get("/api/v1/home/members/{userId}", 21L)
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("home-members-matchable-detail",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Home")
                                .summary("홈화면 매칭 가능한 넥터 상세 조회")
                                .description("홈 화면에서 매칭 가능한 넥터의 상세 프로필 정보를 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("userId").description("유저 ID")
                                )
                                .responseFields(profileSettingsResponseFields())
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("홈화면 팀원 추천 API")
    void 홈화면_팀원_추천_API() throws Exception {
        given(mainHomeFacade.getRecommendedMembers(eq(1L), eq(3)))
                .willReturn(mockMembersResponse());

        mockMvc.perform(get("/api/v1/home/recommendations/members")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("count", "3")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("home-members-recommended",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Home")
                                .summary("홈화면 팀원 추천")
                                .description("홈 화면에서 추천 팀원 목록을 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .queryParameters(
                                        parameterWithName("count").description("조회할 넥터 개수")
                                )
                                .responseFields(memberResponseFields())
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("홈화면 헤더 프로필 API")
    void 홈화면_헤더_프로필_API() throws Exception {
        given(mainHomeFacade.getHeaderProfile(eq(1L)))
                .willReturn(mockHeaderProfileResponse());

        mockMvc.perform(get("/api/v1/home/profile")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("home-header-profile",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Home")
                                .summary("홈화면 헤더 프로필")
                                .description("홈 화면 헤더에 표시할 프로필 정보를 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .responseFields(headerProfileResponseFields())
                                .build()
                        )
                ));
    }

    private HomeProjectResponse mockProjectResponse() {
        return HomeProjectResponse.of(List.of(
                HomeProjectItem.of(
                        10L,
                        "https://imageUrl",
                        "AI 협업툴 개발",
                        "홍길동",
                        "DEVELOPER",
                        "팀 협업 효율을 높이는 AI 기반 협업툴 프로젝트입니다.",
                        12,
                        6,
                        3,
                        true,
                        "모집 중",
                        mockProjectMemberStatistics()
                ),
                HomeProjectItem.of(
                        11L,
                        "https://imageUrl",
                        "모바일 일정 관리",
                        "김철수",
                        "PLANNER",
                        "개인 맞춤 일정 관리 앱을 개발합니다.",
                        7,
                        5,
                        4,
                        false,
                        "매칭 가능",
                        mockProjectMemberStatistics()
                )
        ));
    }

    private HomeHeaderResponse mockHeaderProfileResponse() {
        return HomeHeaderResponse.of(
                1L,
                "https://example.com/profile/1.png",
                "홍길동",
                "honggildong@example.com",
                Role.DEVELOPER
        );
    }

    private HomeMembersResponse mockMembersResponse() {
        return HomeMembersResponse.of(List.of(
                HomeMemberItem.of(
                        21L,
                        "https://example.com/profile/21.png",
                        "이영희",
                        "DESIGNER",
                        "사용자 경험 중심의 디자인을 지향합니다.",
                        "저는 핵심역량이에요",
                        "JOB_SEEKING",
                        true,
                        List.of("PM", "Design")
                ),
                HomeMemberItem.of(
                        22L,
                        "https://example.com/profile/22.png",
                        "박민수",
                        "DEVELOPER",
                        "대규모 트래픽 처리를 경험했습니다.",
                        "저는 핵심역량이에요",
                        "EMPLOYED",
                        false,
                        List.of("Server", "Frontend")
                )
        ));
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

    private HomeProjectMembersResponse mockProjectMembersResponse() {
        return new HomeProjectMembersResponse(List.of(
                new HomeProjectMembersResponse.UserInfo(
                        1L,
                        "홍길동",
                        "hong",
                        "https://example.com/profile/1.png",
                        "백엔드 개발자입니다.",
                        RoleField.BACKEND,
                        null,
                        "Backend",
                        ProjectMemberType.LEADER
                ),
                new HomeProjectMembersResponse.UserInfo(
                        2L,
                        "이영희",
                        "lee",
                        "https://example.com/profile/2.png",
                        "UI/UX 디자이너입니다.",
                        RoleField.UI_UX,
                        null,
                        "Design",
                        ProjectMemberType.MEMBER
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

    private static List<FieldDescriptor> projectResponseFields() {
        return List.of(
                fieldWithPath("status.statusCode").description("응답 상태 코드"),
                fieldWithPath("status.message").description("응답 메시지"),
                fieldWithPath("status.description").optional().description("응답 상세 설명"),
                fieldWithPath("body.projects").description("프로젝트 목록"),
                fieldWithPath("body.projects[].projectId").description("프로젝트 ID"),
                fieldWithPath("body.projects[].imageUrl").description("프로젝트 이미지 URL"),
                fieldWithPath("body.projects[].projectName").description("프로젝트 이름"),
                fieldWithPath("body.projects[].authorName").description("작성자 이름"),
                fieldWithPath("body.projects[].authorPart").description("작성자 파트"),
                fieldWithPath("body.projects[].introduction").description("프로젝트 소개"),
                fieldWithPath("body.projects[].leftDays").description("모집 마감까지 남은 일수"),
                fieldWithPath("body.projects[].maxMemberCount").description("최대 모집 인원"),
                fieldWithPath("body.projects[].curMemberCount").description("현재 참여 인원"),
                fieldWithPath("body.projects[].isScrapped").description("스크랩 여부"),
                fieldWithPath("body.projects[].status").description("프로젝트 상태"),
                fieldWithPath("body.projects[].roles").description("프로젝트 멤버 통계"),
                fieldWithPath("body.projects[].roles.roles").description("Role 기준 통계 목록"),
                fieldWithPath("body.projects[].roles.roles[].role").description("Role"),
                fieldWithPath("body.projects[].roles.roles[].count").description("Role 인원 수"),
                fieldWithPath("body.projects[].roles.roles[].role_fields").description("RoleField 기준 통계 목록"),
                fieldWithPath("body.projects[].roles.roles[].role_fields[].role_field").description("RoleField"),
                fieldWithPath("body.projects[].roles.roles[].role_fields[].count").description("RoleField 인원 수")
        );
    }

    private static List<FieldDescriptor> memberResponseFields() {
        return List.of(
                fieldWithPath("status.statusCode").description("응답 상태 코드"),
                fieldWithPath("status.message").description("응답 메시지"),
                fieldWithPath("status.description").optional().description("응답 상세 설명"),
                fieldWithPath("body.members").description("넥터 목록"),
                fieldWithPath("body.members[].userId").description("유저 ID"),
                fieldWithPath("body.members[].imageUrl").description("프로필 이미지 URL"),
                fieldWithPath("body.members[].name").description("이름"),
                fieldWithPath("body.members[].part").description("파트(역할)"),
                fieldWithPath("body.members[].introduction").description("소개"),
                fieldWithPath("body.members[].coreCompetencies").description("핵심 역량"),
                fieldWithPath("body.members[].status").description("상태"),
                fieldWithPath("body.members[].isScrapped").description("스크랩 여부"),
                fieldWithPath("body.members[].roles").description("역할 목록")
        );
    }

    private static List<FieldDescriptor> projectMembersResponseFields() {
        return List.of(
                fieldWithPath("status.statusCode").description("응답 상태 코드"),
                fieldWithPath("status.message").description("응답 메시지"),
                fieldWithPath("status.description").optional().description("응답 상세 설명"),
                fieldWithPath("body.users").description("프로젝트 팀원 목록"),
                fieldWithPath("body.users[].user_id").description("유저 ID"),
                fieldWithPath("body.users[].name").description("이름"),
                fieldWithPath("body.users[].nickname").description("닉네임"),
                fieldWithPath("body.users[].profile_image_url").description("프로필 이미지 URL"),
                fieldWithPath("body.users[].bio").description("자기소개"),
                fieldWithPath("body.users[].role_field").description("역할 필드"),
                fieldWithPath("body.users[].custom_role_field_name").optional().description("커스텀 역할명"),
                fieldWithPath("body.users[].part_label").description("파트 라벨"),
                fieldWithPath("body.users[].member_type").description("프로젝트 멤버 타입")
        );
    }

    private static List<FieldDescriptor> headerProfileResponseFields() {
        return List.of(
                fieldWithPath("status.statusCode").description("응답 상태 코드"),
                fieldWithPath("status.message").description("응답 메시지"),
                fieldWithPath("status.description").optional().description("응답 상세 설명"),
                fieldWithPath("body.userId").description("유저 ID"),
                fieldWithPath("body.imageUrl").description("프로필 이미지 URL"),
                fieldWithPath("body.name").description("이름"),
                fieldWithPath("body.email").description("이메일"),
                fieldWithPath("body.role").description("역할")
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

}
