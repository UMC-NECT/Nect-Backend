package com.nect.api.domain.home.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.domain.home.dto.HomeMemberItem;
import com.nect.api.domain.home.dto.HomeMembersResponse;
import com.nect.api.domain.home.dto.HomeProjectItem;
import com.nect.api.domain.home.dto.HomeProjectResponse;
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
}
