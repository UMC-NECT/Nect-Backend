package com.nect.api.domain.home.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.domain.home.dto.HomeMemberItem;
import com.nect.api.domain.home.dto.HomeMembersResponse;
import com.nect.api.domain.home.dto.HomeProjectMemberItem;
import com.nect.api.domain.home.dto.HomeProjectItem;
import com.nect.api.domain.home.dto.HomeProjectMembersResponse;
import com.nect.api.domain.home.dto.HomeProjectResponse;
import com.nect.api.domain.home.dto.HomeProjectTeamMembers;
import com.nect.api.domain.home.dto.HomeRecruitingProjectFile;
import com.nect.api.domain.home.dto.HomeRecruitingProjectLeader;
import com.nect.api.domain.home.dto.HomeRecruitingProjectPosition;
import com.nect.api.domain.home.dto.HomeRecruitingProjectResponse;
import com.nect.api.domain.home.dto.HomeRecruitingProjectTeamComposition;
import com.nect.api.domain.home.dto.HomeRecruitingProjectTeamPart;
import com.nect.api.domain.home.service.HomeProjectQueryService;
import com.nect.api.domain.home.service.HomeQueryService;
import com.nect.api.domain.home.service.HomeRecommendService;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
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
import java.util.List;
import java.util.Map;

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
    private HomeQueryService homeQueryService;

    @MockitoBean
    private HomeRecommendService homeRecommendService;

    @MockitoBean
    private HomeProjectQueryService homeProjectQueryService;

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
        given(homeQueryService.getProjects(eq(1L), eq(3)))
                .willReturn(mockProjectResponse());

        mockMvc.perform(get("/api/v1/home/projects")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("count", "3")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("home-projects-recruiting",
                        resource(ResourceSnippetParameters.builder()
                                .tag("홈")
                                .summary("모집 중인 프로젝트 조회")
                                .description("홈 화면에서 모집 중인 프로젝트 목록을 조회합니다.")
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
    @DisplayName("홈화면 프로젝트 추천 API")
    void 홈화면_프로젝트_추천_API() throws Exception {
        given(homeRecommendService.getProjects(eq(1L), eq(3)))
                .willReturn(mockProjectResponse());

        mockMvc.perform(get("/api/v1/home/recommendations/projects")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("count", "3")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("home-projects-recommended",
                        resource(ResourceSnippetParameters.builder()
                                .tag("홈")
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
    @DisplayName("홈화면 매칭 가능한 넥터 API")
    void 홈화면_매칭_가능한_넥터_API() throws Exception {
        given(homeQueryService.getMembers(eq(1L), eq(3)))
                .willReturn(mockMembersResponse());

        mockMvc.perform(get("/api/v1/home/members")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("count", "3")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("home-members-matchable",
                        resource(ResourceSnippetParameters.builder()
                                .tag("홈")
                                .summary("홈화면 매칭 가능한 넥터 조회")
                                .description("홈 화면에서 매칭 가능한 넥터 목록을 조회합니다.")
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
    @DisplayName("홈화면 팀원 추천 API")
    void 홈화면_팀원_추천_API() throws Exception {
        given(homeRecommendService.getMembers(eq(1L), eq(3)))
                .willReturn(mockMembersResponse());

        mockMvc.perform(get("/api/v1/home/recommendations/members")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("count", "3")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("home-members-recommended",
                        resource(ResourceSnippetParameters.builder()
                                .tag("홈")
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
    @DisplayName("홈화면 프로젝트 상세 API ( 미완 )")
    void 홈화면_프로젝트_상세_API() throws Exception {
        given(homeProjectQueryService.getProjectInfo(eq(10L), eq(1L)))
                .willReturn(mockRecruitingProjectResponse());

        mockMvc.perform(get("/api/v1/home/projects/{projectId}", 10L)
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("home-projects-detail",
                        resource(ResourceSnippetParameters.builder()
                                .tag("홈")
                                .summary("홈화면 프로젝트 상세")
                                .description("홈 화면에서 모집 중인 프로젝트 상세 정보를 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("projectId").description("프로젝트 ID")
                                )
                                .responseFields(recruitingProjectResponseFields())
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("홈화면 프로젝트 팀원 목록 API")
    void 홈화면_프로젝트_팀원_정보_API() throws Exception {
        given(homeProjectQueryService.getMembersInfo(eq(12L)))
                .willReturn(mockProjectMembersResponse());

        mockMvc.perform(get("/api/v1/home/projects/{projectId}/members", 12L)
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("home-projects-members",
                        resource(ResourceSnippetParameters.builder()
                                .tag("홈")
                                .summary("홈화면 프로젝트 팀원 정보")
                                .description("홈 화면에서 프로젝트 팀원 목록 정보를 조회합니다.")
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

    private HomeProjectResponse mockProjectResponse() {
        return new HomeProjectResponse(List.of(
                new HomeProjectItem(
                        10L,
                        1001L,
                        "AI 협업툴 개발",
                        "홍길동",
                        "Backend",
                        "팀 협업 효율을 높이는 AI 기반 협업툴 프로젝트입니다.",
                        12,
                        6,
                        3,
                        true,
                        "모집 중",
                        Map.of("Backend", 2, "Design", 1)
                ),
                new HomeProjectItem(
                        11L,
                        1002L,
                        "모바일 일정 관리",
                        "김철수",
                        "PM",
                        "개인 맞춤 일정 관리 앱을 개발합니다.",
                        7,
                        5,
                        4,
                        false,
                        "매칭 가능",
                        Map.of("PM", 1, "Design", 1)
                )
        ));
    }

    private HomeRecruitingProjectResponse mockRecruitingProjectResponse() {
        return new HomeRecruitingProjectResponse(
                12L,
                "넥트(Nect)",
                "아이디어 분석으로 프로젝트 등록, 팀원 매칭, 협업 보드까지, 사이드 프로젝트 웹 플랫폼 개발",
                LocalDate.parse("2025-11-13"),
                LocalDate.parse("2026-02-11"),
                "OPEN",
                true,
                22,
                7L,
                List.of("IT · 웹/모바일 서비스", "네트워킹 · 커뮤니티"),
                List.of(
                        new HomeRecruitingProjectPosition(
                                "DESIGN",
                                1,
                                List.of(
                                        "사용자 경험을 고려한 플랫폼의 UI/UX 디자인을 담당합니다.",
                                        "UI디자인을 위한 Figma / Illustrator 사용 가능하셔야합니다."
                                )
                        ),
                        new HomeRecruitingProjectPosition(
                                "BACKEND",
                                2,
                                List.of(
                                        "사용자 경험을 고려한 플랫폼의 UI/UX 디자인을 담당합니다.",
                                        "UI디자인을 위한 Figma / Illustrator 사용 가능하셔야합니다."
                                )
                        )
                ),
                new HomeRecruitingProjectTeamComposition(
                        new HomeRecruitingProjectTeamPart(1, Map.of("PM", 1)),
                        new HomeRecruitingProjectTeamPart(1, Map.of("Design", 1)),
                        new HomeRecruitingProjectTeamPart(8, Map.of("Frontend", 4, "Backend", 4))
                ),
                List.of(
                        "사이드 프로젝트 팀원을 신뢰성 있게 매칭",
                        "프로젝트 팀 매칭부터 협업까지 원스톱 플랫폼 제공",
                        "팀 중심 협업 문화 확산"
                ),
                List.of(
                        "리더 프로필",
                        "관심사·목표 기반 매칭",
                        "아이디어 분석 기능",
                        "협업 보드",
                        "알림 및 커뮤니케이션",
                        "Week-Mission 시스템"
                ),
                List.of(
                        "대학생 - 공모전, 해커톤, 포트폴리오용 프로젝트를 진행하고 싶은 학생",
                        "직장인 - 본업 외 사이드프로젝트나 개인 브랜딩을 위해 팀을 구하는 직장인",
                        "프리랜서/크리에이터 - 새로운 협업 경험을 통해 네트워크를 넓히고 싶은 창작자"
                ),
                new HomeRecruitingProjectLeader(
                        3L,
                        "시루",
                        "PM",
                        "디자인 전공 출신 프로덕트 매니저",
                        "leader_profile.jpg"
                ),
                List.of(
                        new HomeRecruitingProjectFile(
                                1L,
                                "넥트 프로젝트 기획서",
                                "https://www.figma.com/silde/"
                        )
                )
        );
    }

    private HomeMembersResponse mockMembersResponse() {
        return new HomeMembersResponse(List.of(
                new HomeMemberItem(
                        21L,
                        "https://example.com/profile/21.png",
                        "이영희",
                        "Design",
                        "사용자 경험 중심의 디자인을 지향합니다.",
                        "매칭 가능",
                        true,
                        List.of("PM", "Design")
                ),
                new HomeMemberItem(
                        22L,
                        "https://example.com/profile/22.png",
                        "박민수",
                        "Backend",
                        "대규모 트래픽 처리를 경험했습니다.",
                        "매칭 가능",
                        false,
                        List.of("Server", "Frontend")
                )
        ));
    }

    private HomeProjectMembersResponse mockProjectMembersResponse() {
        return new HomeProjectMembersResponse(
                12L,
                new HomeProjectTeamMembers(Map.of(
                        "PM", List.of(
                                new HomeProjectMemberItem(
                                        101L,
                                        "시루",
                                        "PM",
                                        "LEADER",
                                        "MATCHED",
                                        "디자인 전공 출신 만능형 프로덕트 매니저입니다. 함께 성장 할 팀을 구합니다.",
                                        "pm_leader.jpg"
                                )
                        ),
                        "Design", List.of(
                                new HomeProjectMemberItem(
                                        102L,
                                        "이방토",
                                        "Design",
                                        "LEAD",
                                        "MATCHING",
                                        "디자인 프로젝트 경험이 많고 꼼꼼한 UX/UI 디자이너입니다. UX리서치/브랜딩/패키지/그래픽/일러스트 모두 가능합니다.",
                                        "design_lead.jpg"
                                )
                        ),
                        "Frontend", List.of(
                                new HomeProjectMemberItem(
                                        201L,
                                        "숀",
                                        "Frontend",
                                        "LEAD",
                                        "MATCHED",
                                        "프로필 소개",
                                        "fe_lead.jpg"
                                ),
                                new HomeProjectMemberItem(
                                        202L,
                                        "갱빈",
                                        "Frontend",
                                        "MEMBER",
                                        "MATCHED",
                                        "프로필 소개",
                                        "fe_1.jpg"
                                ),
                                new HomeProjectMemberItem(
                                        203L,
                                        "웬디",
                                        "Frontend",
                                        "MEMBER",
                                        "MATCHED",
                                        "프로필 소개",
                                        "fe_2.jpg"
                                ),
                                new HomeProjectMemberItem(
                                        204L,
                                        "미노",
                                        "Frontend",
                                        "MEMBER",
                                        "MATCHED",
                                        "프로필 소개",
                                        "fe_3.jpg"
                                )
                        ),
                        "Backend", List.of(
                                new HomeProjectMemberItem(
                                        301L,
                                        "세인트",
                                        "Backend",
                                        "LEAD",
                                        "MATCHED",
                                        "프로필 소개",
                                        "be_lead.jpg"
                                ),
                                new HomeProjectMemberItem(
                                        302L,
                                        "미카엘",
                                        "Backend",
                                        "MEMBER",
                                        "MATCHED",
                                        "프로필 소개",
                                        "be_1.jpg"
                                )
                        )
                ))
        );
    }

    private static List<FieldDescriptor> projectResponseFields() {
        return List.of(
                fieldWithPath("status.statusCode").description("응답 상태 코드"),
                fieldWithPath("status.message").description("응답 메시지"),
                fieldWithPath("status.description").optional().description("응답 상세 설명"),
                fieldWithPath("body.projects").description("프로젝트 목록"),
                fieldWithPath("body.projects[].projectId").description("프로젝트 ID"),
                fieldWithPath("body.projects[].imageUrl").description("프로젝트 이미지 ID"),
                fieldWithPath("body.projects[].projectName").description("프로젝트 이름"),
                fieldWithPath("body.projects[].authorName").description("작성자 이름"),
                fieldWithPath("body.projects[].authorPart").description("작성자 파트"),
                fieldWithPath("body.projects[].introduction").description("프로젝트 소개"),
                fieldWithPath("body.projects[].leftDays").description("모집 마감까지 남은 일수"),
                fieldWithPath("body.projects[].maxMemberCount").description("최대 모집 인원"),
                fieldWithPath("body.projects[].curMemberCount").description("현재 참여 인원"),
                fieldWithPath("body.projects[].isScrapped").description("스크랩 여부"),
                fieldWithPath("body.projects[].status").description("프로젝트 상태"),
                fieldWithPath("body.projects[].roles")
                        .type(JsonFieldType.OBJECT)
                        .description("모집 역할별 인원 (key=역할, value=인원)"),
                fieldWithPath("body.projects[].roles.*")
                        .type(JsonFieldType.NUMBER)
                        .description("역할별 인원 값")
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
                fieldWithPath("body.members[].part").description("파트"),
                fieldWithPath("body.members[].introduction").description("소개"),
                fieldWithPath("body.members[].status").description("상태"),
                fieldWithPath("body.members[].isScrapped").description("스크랩 여부"),
                fieldWithPath("body.members[].roles").description("역할 목록")
        );
    }

    private static List<FieldDescriptor> recruitingProjectResponseFields() {
        return List.of(
                fieldWithPath("status.statusCode").description("응답 상태 코드"),
                fieldWithPath("status.message").description("응답 메시지"),
                fieldWithPath("status.description").optional().description("응답 상세 설명"),
                fieldWithPath("body.projectId").description("프로젝트 ID"),
                fieldWithPath("body.projectName").description("프로젝트 이름"),
                fieldWithPath("body.introduction").description("프로젝트 소개"),
                fieldWithPath("body.startDate").description("프로젝트 시작일"),
                fieldWithPath("body.endDate").description("프로젝트 종료일"),
                fieldWithPath("body.recruitmentStatus").description("모집 상태"),
                fieldWithPath("body.isMatching").description("로그인 유저 참여 여부"),
                fieldWithPath("body.dDay").description("모집 마감까지 남은 일수"),
                fieldWithPath("body.chatRoomId").optional().description("채팅방 ID"),
                fieldWithPath("body.categories").description("프로젝트 카테고리"),
                fieldWithPath("body.positions").description("모집 포지션 목록"),
                fieldWithPath("body.positions[].role").description("포지션 역할"),
                fieldWithPath("body.positions[].requiredCount").description("모집 인원 수"),
                fieldWithPath("body.positions[].description").description("포지션 설명 목록"),
                fieldWithPath("body.teamComposition")
                        .type(JsonFieldType.OBJECT)
                        .optional()
                        .description("팀 구성 정보"),
                fieldWithPath("body.teamComposition.planning").type(JsonFieldType.OBJECT).description("기획 파트 구성"),
                fieldWithPath("body.teamComposition.planning.totalCount").description("기획 파트 총 인원"),
                fieldWithPath("body.teamComposition.planning.parts").type(JsonFieldType.OBJECT).description("기획 파트 상세 인원"),
                fieldWithPath("body.teamComposition.planning.parts.*").type(JsonFieldType.NUMBER).description("기획 파트 인원 값"),
                fieldWithPath("body.teamComposition.design").type(JsonFieldType.OBJECT).description("디자인 파트 구성"),
                fieldWithPath("body.teamComposition.design.totalCount").description("디자인 파트 총 인원"),
                fieldWithPath("body.teamComposition.design.parts").type(JsonFieldType.OBJECT).description("디자인 파트 상세 인원"),
                fieldWithPath("body.teamComposition.design.parts.*").type(JsonFieldType.NUMBER).description("디자인 파트 인원 값"),
                fieldWithPath("body.teamComposition.development").type(JsonFieldType.OBJECT).description("개발 파트 구성"),
                fieldWithPath("body.teamComposition.development.totalCount").description("개발 파트 총 인원"),
                fieldWithPath("body.teamComposition.development.parts").type(JsonFieldType.OBJECT).description("개발 파트 상세 인원"),
                fieldWithPath("body.teamComposition.development.parts.*").type(JsonFieldType.NUMBER).description("개발 파트 인원 값"),
                fieldWithPath("body.goals").description("프로젝트 목표"),
                fieldWithPath("body.mainFeatures").description("주요 기능"),
                fieldWithPath("body.targetUsers").description("타겟 유저"),
                fieldWithPath("body.leader").type(JsonFieldType.OBJECT).description("리더 정보"),
                fieldWithPath("body.leader.userId").description("리더 유저 ID"),
                fieldWithPath("body.leader.name").description("리더 이름"),
                fieldWithPath("body.leader.role").description("리더 역할"),
                fieldWithPath("body.leader.introduction").description("리더 소개"),
                fieldWithPath("body.leader.profileImage").description("리더 프로필 이미지"),
                fieldWithPath("body.attachedFiles").description("첨부 파일 목록"),
                fieldWithPath("body.attachedFiles[].fileId").description("첨부 파일 ID"),
                fieldWithPath("body.attachedFiles[].fileName").description("첨부 파일명"),
                fieldWithPath("body.attachedFiles[].fileUrl").description("첨부 파일 URL")
        );
    }

    private static List<FieldDescriptor> projectMembersResponseFields() {
        return List.of(
                fieldWithPath("status.statusCode").description("응답 상태 코드"),
                fieldWithPath("status.message").description("응답 메시지"),
                fieldWithPath("status.description").optional().description("응답 상세 설명"),
                fieldWithPath("body.projectId").description("프로젝트 ID"),
                fieldWithPath("body.teamMembers").type(JsonFieldType.OBJECT).description("팀원 정보"),
                fieldWithPath("body.teamMembers.parts").type(JsonFieldType.OBJECT).description("파트별 팀원 목록"),
                fieldWithPath("body.teamMembers.parts.*").type(JsonFieldType.ARRAY).description("파트별 팀원 배열"),
                fieldWithPath("body.teamMembers.parts.*[].userId").description("유저 ID"),
                fieldWithPath("body.teamMembers.parts.*[].name").description("이름"),
                fieldWithPath("body.teamMembers.parts.*[].part").description("파트"),
                fieldWithPath("body.teamMembers.parts.*[].roleInPart").description("파트 내 역할"),
                fieldWithPath("body.teamMembers.parts.*[].matchingStatus").description("매칭 상태"),
                fieldWithPath("body.teamMembers.parts.*[].introduction").description("소개"),
                fieldWithPath("body.teamMembers.parts.*[].profileImage").description("프로필 이미지")
        );
    }
}
