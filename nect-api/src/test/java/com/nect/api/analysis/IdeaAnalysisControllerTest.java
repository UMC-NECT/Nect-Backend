package com.nect.api.analysis;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.domain.analysis.dto.req.IdeaAnalysisRequestDto;
import com.nect.api.domain.analysis.dto.res.IdeaAnalysisResponseDto;
import com.nect.api.domain.analysis.service.IdeaAnalysisService;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class IdeaAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IdeaAnalysisService ideaAnalysisService;

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
    @DisplayName("프로젝트 아이디어 분석 API")
    void 프로젝트_아이디어_분석_API() throws Exception {
        // given
        IdeaAnalysisRequestDto requestDto = IdeaAnalysisRequestDto.builder()
                .projectName("대학생 스터디 매칭 플랫폼")
                .projectSummary("관심사가 같은 대학생들을 연결해주는 서비스")
                .targetUsers("대학생, 취업준비생")
                .problemStatement("혼자 공부할 때 동기부여가 부족함")
                .coreFeature1("AI 기반 스터디원 매칭")
                .coreFeature2("실시간 학습 시간 공유")
                .coreFeature3("스터디 리워드 시스템")
                .platform("WEB_AND_MOBILE")
                .referenceServices("에브리타임, 열품타")
                .technicalChallenges("실시간 데이터 동기화")
                .targetCompletionDate(LocalDate.of(2026, 4, 30))
                .build();

        given(ideaAnalysisService.analyzeProjectIdea(anyLong(), any(IdeaAnalysisRequestDto.class)))
                .willReturn(mockResponse());

        // when & then
        mockMvc.perform(post("/api/v1/analysis")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isOk())
                .andDo(document("idea-analysis",
                        resource(ResourceSnippetParameters.builder()
                                .tag("아이디어 분석")
                                .summary("AI 프로젝트 아이디어 분석")
                                .description("사용자의 아이디어를 AI가 분석하여 팀 구성 및 로드맵을 제안합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .requestFields(
                                        fieldWithPath("projectName").description("프로젝트 이름"),
                                        fieldWithPath("projectSummary").description("프로젝트 한줄 요약"),
                                        fieldWithPath("targetUsers").description("주요 타겟 사용자"),
                                        fieldWithPath("problemStatement").description("해결하고자 하는 문제점"),
                                        fieldWithPath("coreFeature1").description("핵심 기능 1"),
                                        fieldWithPath("coreFeature2").description("핵심 기능 2"),
                                        fieldWithPath("coreFeature3").description("핵심 기능 3"),
                                        fieldWithPath("platform").description("서비스 플랫폼 (WEB, MOBILE 등)"),
                                        fieldWithPath("referenceServices").description("참고 서비스"),
                                        fieldWithPath("technicalChallenges").description("예상되는 기술적 난관"),
                                        fieldWithPath("targetCompletionDate").description("목표 완료일 (yyyy-MM-dd)")
                                )
                                .responseFields(
                                        fieldWithPath("recommended_project_names").description("추천 프로젝트 이름 리스트"),
                                        fieldWithPath("estimated_duration").description("예상 소요 기간"),

                                        fieldWithPath("team_composition.frontend").description("필요 프론트엔드 인원"),
                                        fieldWithPath("team_composition.backend").description("필요 백엔드 인원"),
                                        fieldWithPath("team_composition.designer").description("필요 디자이너 인원"),
                                        fieldWithPath("team_composition.pm").description("필요 PM 인원"),
                                        fieldWithPath("team_composition.others").description("기타 필요 인원"),

                                        fieldWithPath("improvement_point1").description("보완점 1"),
                                        fieldWithPath("improvement_point2").description("보완점 2"),
                                        fieldWithPath("improvement_point3").description("보완점 3"),

                                        fieldWithPath("weekly_roadmap[].week_number").description("주차 번호"),
                                        fieldWithPath("weekly_roadmap[].week_title").description("해당 주차 목표"),
                                        fieldWithPath("weekly_roadmap[].pm_tasks").description("PM 업무"),
                                        fieldWithPath("weekly_roadmap[].design_tasks").description("디자인 업무"),
                                        fieldWithPath("weekly_roadmap[].frontend_tasks").description("프론트엔드 업무"),
                                        fieldWithPath("weekly_roadmap[].backend_tasks").description("백엔드 업무"),

                                        fieldWithPath("analysis_id").optional().description("분석 결과 ID (저장 시)")
                                )
                                .build()
                        )
                ));
    }

    private IdeaAnalysisResponseDto mockResponse() {
        return IdeaAnalysisResponseDto.builder()
                .recommendedProjectNames(List.of("StudyHub", "CamLink", "NectStudy"))
                .estimatedDuration("8주")
                .teamComposition(IdeaAnalysisResponseDto.TeamComposition.builder()
                        .frontend(2).backend(2).designer(1).pm(1).others(0).build())
                .improvementPoint1("사용자 온보딩 과정의 간소화가 필요합니다.")
                .improvementPoint2("실시간 매칭 알고리즘의 고도화가 요구됩니다.")
                .improvementPoint3("수익 모델에 대한 구체적인 설계가 보완되어야 합니다.")
                .weeklyRoadmap(List.of(
                        IdeaAnalysisResponseDto.WeeklyRoadmap.builder()
                                .weekNumber(1).weekTitle("기획 및 요구사항 정의")
                                .pmTasks("요구사항 명세서 작성").designTasks("와이어프레임 설계")
                                .frontendTasks("환경 세팅").backendTasks("DB 스키마 설계").build()
                ))
                .build();
    }
}