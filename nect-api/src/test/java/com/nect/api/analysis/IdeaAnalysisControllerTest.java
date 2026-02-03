package com.nect.api.analysis;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.domain.analysis.dto.res.IdeaAnalysisPageResponseDto;
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
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;

import java.time.LocalDate;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                .willReturn(mockAnalysisResponse());

        // when & then
        mockMvc.perform(post("/api/v1/analysis")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isOk())
                .andDo(document("idea-analysis-create",
                        resource(ResourceSnippetParameters.builder()
                                .tag("아이디어 분석")
                                .summary("AI 프로젝트 아이디어 분석")
                                .description("""
                                        사용자의 프로젝트 아이디어를 AI가 분석하여 팀 구성, 프로젝트 기간, 보완점, 주차별 로드맵을 제안합니다.
                                        **설명:**
                                        - 응답값에서 role_field_display_name는 역할 한글명으로 임시로 넣어놨습니다 필요하시면 사용해 주세요.
                                        **제약사항:**
                                        - 아이디어 분석은 사용자당 최대 2개까지만 가능합니다.
                                        - 3개 이상 분석 시도 시 다음과 같은 에러 응답이 반환됩니다:
                                        
```json
                                        {
                                          "status": {
                                            "statusCode": "ANALYSIS-001",
                                            "message": "아이디어 분석은 인당 최대 2개까지만 가능합니다.",
                                            "description": null
                                          }
                                        }
```
                                        """)
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
                                        fieldWithPath("platform").description("서비스 플랫폼 (WEB, MOBILE, WEB_AND_MOBILE 등)"),
                                        fieldWithPath("referenceServices").description("참고 서비스"),
                                        fieldWithPath("technicalChallenges").description("예상되는 기술적 난관"),
                                        fieldWithPath("targetCompletionDate").description("목표 완료일 (yyyy-MM-dd)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("응답 상태 코드"),
                                        fieldWithPath("status.message").description("응답 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),

                                        fieldWithPath("body.analysis_id").description("분석 결과 ID"),
                                        fieldWithPath("body.recommended_project_names[]").description("추천 프로젝트 이름 리스트 (최대 3개)"),

                                        fieldWithPath("body.project_duration.start_date").description("프로젝트 시작 예정일 (yyyy-MM-dd)"),
                                        fieldWithPath("body.project_duration.end_date").description("프로젝트 종료 예정일 (yyyy-MM-dd)"),
                                        fieldWithPath("body.project_duration.total_weeks").description("총 소요 주차수"),
                                        fieldWithPath("body.project_duration.display_text").description("기간 표시 텍스트 (예: '8주 (2026-03-01 ~ 2026-04-26)')"),

                                        fieldWithPath("body.team_composition[].role_field").description("역할 필드 (FRONTEND, BACKEND, UI_UX, SERVICE 등)"),
                                        fieldWithPath("body.team_composition[].role_field_display_name").description("역할 한글명"),
                                        fieldWithPath("body.team_composition[].required_count").description("필요 인원수"),

                                        fieldWithPath("body.improvement_points[].order").description("보완점 순서 (1, 2, 3)"),
                                        fieldWithPath("body.improvement_points[].title").description("보완점 제목"),
                                        fieldWithPath("body.improvement_points[].description").description("보완점 상세 설명"),

                                        fieldWithPath("body.weekly_roadmap[].week_number").description("주차 번호"),
                                        fieldWithPath("body.weekly_roadmap[].week_title").description("해당 주차 목표"),
                                        fieldWithPath("body.weekly_roadmap[].week_start_date").description("주차 시작일 (yyyy-MM-dd)"),
                                        fieldWithPath("body.weekly_roadmap[].week_end_date").description("주차 종료일 (yyyy-MM-dd)"),
                                        fieldWithPath("body.weekly_roadmap[].week_period").description("주차 기간 텍스트"),
                                        fieldWithPath("body.weekly_roadmap[].role_tasks[].role_field").description("역할 필드"),
                                        fieldWithPath("body.weekly_roadmap[].role_tasks[].role_field_display_name").description("역할 한글명"),
                                        fieldWithPath("body.weekly_roadmap[].role_tasks[].tasks").description("해당 역할의 주차별 태스크")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("분석서 페이징 조회 API")
    void 분석서_페이징_조회_API() throws Exception {
        // given
        given(ideaAnalysisService.getAnalysisPage(anyLong(), anyInt()))
                .willReturn(mockPageResponse());

        // when & then
        mockMvc.perform(get("/api/v1/analysis")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("page", "0")
                )
                .andExpect(status().isOk())
                .andDo(document("idea-analysis-page",
                        resource(ResourceSnippetParameters.builder()
                                .tag("아이디어 분석")
                                .summary("분석서 페이징 조회")
                                .description("""
                                        사용자의 아이디어 분석 결과를 페이징하여 조회합니다.
                                        한 페이지당 1개의 분석서가 조회되며, 화살표 버튼으로 이전/다음 분석서를 탐색할 수 있습니다.
                                        
                                        **페이징 정보:**
                                        - page: 0부터 시작 (0 = 최신 분석서, 1 = 이전 분석서)
                                        - 최대 2개의 분석서만 존재
                                        - has_next/has_previous로 다음/이전 페이지 존재 여부 확인 가능
                                        """)
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .queryParameters(
                                        parameterWithName("page").description("페이지 번호 (0부터 시작, 기본값: 0)").optional()
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("응답 상태 코드"),
                                        fieldWithPath("status.message").description("응답 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),

                                        fieldWithPath("body.analysis.analysis_id").description("분석 결과 ID"),
                                        fieldWithPath("body.analysis.recommended_project_names[]").description("추천 프로젝트 이름 리스트"),

                                        fieldWithPath("body.analysis.project_duration.start_date").description("프로젝트 시작 예정일"),
                                        fieldWithPath("body.analysis.project_duration.end_date").description("프로젝트 종료 예정일"),
                                        fieldWithPath("body.analysis.project_duration.total_weeks").description("총 소요 주차수"),
                                        fieldWithPath("body.analysis.project_duration.display_text").description("기간 표시 텍스트"),

                                        fieldWithPath("body.analysis.team_composition[].role_field").description("역할 필드"),
                                        fieldWithPath("body.analysis.team_composition[].role_field_display_name").description("역할 한글명"),
                                        fieldWithPath("body.analysis.team_composition[].required_count").description("필요 인원수"),

                                        fieldWithPath("body.analysis.improvement_points[].order").description("보완점 순서"),
                                        fieldWithPath("body.analysis.improvement_points[].title").description("보완점 제목"),
                                        fieldWithPath("body.analysis.improvement_points[].description").description("보완점 상세 설명"),

                                        fieldWithPath("body.analysis.weekly_roadmap[].week_number").description("주차 번호"),
                                        fieldWithPath("body.analysis.weekly_roadmap[].week_title").description("해당 주차 목표"),
                                        fieldWithPath("body.analysis.weekly_roadmap[].week_start_date").description("주차 시작일"),
                                        fieldWithPath("body.analysis.weekly_roadmap[].week_end_date").description("주차 종료일"),
                                        fieldWithPath("body.analysis.weekly_roadmap[].week_period").description("주차 기간 텍스트"),
                                        fieldWithPath("body.analysis.weekly_roadmap[].role_tasks[].role_field").description("역할 필드"),
                                        fieldWithPath("body.analysis.weekly_roadmap[].role_tasks[].role_field_display_name").description("역할 한글명"),
                                        fieldWithPath("body.analysis.weekly_roadmap[].role_tasks[].tasks").description("해당 역할의 주차별 태스크"),

                                        fieldWithPath("body.page_info.current_page").description("현재 페이지 번호 (0부터 시작)"),
                                        fieldWithPath("body.page_info.total_pages").description("전체 페이지 수"),
                                        fieldWithPath("body.page_info.total_elements").description("전체 분석서 개수"),
                                        fieldWithPath("body.page_info.has_next").description("다음 페이지 존재 여부"),
                                        fieldWithPath("body.page_info.has_previous").description("이전 페이지 존재 여부")
                                )
                                .build()
                        )
                ));
    }

    private IdeaAnalysisResponseDto mockAnalysisResponse() {
        return IdeaAnalysisResponseDto.builder()
                .analysisId(1L)
                .recommendedProjectNames(List.of("StudyConnect", "CampusLink", "StudyMate"))
                .projectDuration(IdeaAnalysisResponseDto.ProjectDuration.builder()
                        .startDate(LocalDate.of(2026, 3, 1))
                        .endDate(LocalDate.of(2026, 4, 26))
                        .totalWeeks(8)
                        .displayText("8주 (2026-03-01 ~ 2026-04-26)")
                        .build())
                .teamComposition(List.of(
                        IdeaAnalysisResponseDto.TeamMember.builder()
                                .roleField("SERVICE")
                                .roleFieldDisplayName("서비스")
                                .requiredCount(1)
                                .build(),
                        IdeaAnalysisResponseDto.TeamMember.builder()
                                .roleField("UI_UX")
                                .roleFieldDisplayName("UI/UX")
                                .requiredCount(1)
                                .build(),
                        IdeaAnalysisResponseDto.TeamMember.builder()
                                .roleField("FRONTEND")
                                .roleFieldDisplayName("프론트엔드")
                                .requiredCount(2)
                                .build(),
                        IdeaAnalysisResponseDto.TeamMember.builder()
                                .roleField("BACKEND")
                                .roleFieldDisplayName("백엔드")
                                .requiredCount(2)
                                .build()
                ))
                .improvementPoints(List.of(
                        IdeaAnalysisResponseDto.ImprovementPoint.builder()
                                .order(1)
                                .title("정확한 목표와 수치 분석")
                                .description("명확한 KPI 설정과 데이터 기반 의사결정이 필요합니다.")
                                .build(),
                        IdeaAnalysisResponseDto.ImprovementPoint.builder()
                                .order(2)
                                .title("주차별 산출물 체크리스트")
                                .description("각 주차별 완료 기준을 명확히 정의해야 합니다.")
                                .build(),
                        IdeaAnalysisResponseDto.ImprovementPoint.builder()
                                .order(3)
                                .title("리스크 관리 계획")
                                .description("기술적 난관에 대한 대응 전략을 수립해야 합니다.")
                                .build()
                ))
                .weeklyRoadmap(List.of(
                        IdeaAnalysisResponseDto.WeeklyRoadmap.builder()
                                .weekNumber(1)
                                .weekTitle("아이디어 확정 및 요구사항 정의")
                                .weekStartDate(LocalDate.of(2026, 3, 1))
                                .weekEndDate(LocalDate.of(2026, 3, 7))
                                .weekPeriod("2026-03-01 ~ 2026-03-07")
                                .roleTasks(List.of(
                                        IdeaAnalysisResponseDto.RoleTask.builder()
                                                .roleField("SERVICE")
                                                .roleFieldDisplayName("서비스")
                                                .tasks("서비스 기획 및 요구사항 명세서 작성")
                                                .build(),
                                        IdeaAnalysisResponseDto.RoleTask.builder()
                                                .roleField("UI_UX")
                                                .roleFieldDisplayName("UI/UX")
                                                .tasks("디자인 시스템 초기 설계")
                                                .build(),
                                        IdeaAnalysisResponseDto.RoleTask.builder()
                                                .roleField("FRONTEND")
                                                .roleFieldDisplayName("프론트엔드")
                                                .tasks("개발 환경 세팅")
                                                .build(),
                                        IdeaAnalysisResponseDto.RoleTask.builder()
                                                .roleField("BACKEND")
                                                .roleFieldDisplayName("백엔드")
                                                .tasks("ERD 설계 및 기술 스택 결정")
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }

    private IdeaAnalysisPageResponseDto mockPageResponse() {
        return IdeaAnalysisPageResponseDto.builder()
                .analysis(mockAnalysisResponse())
                .pageInfo(IdeaAnalysisPageResponseDto.PageInfo.builder()
                        .currentPage(0)
                        .totalPages(2)
                        .totalElements(2L)
                        .hasNext(true)
                        .hasPrevious(false)
                        .build())
                .build();
    }

    @Test
    @DisplayName("분석서 삭제 API")
    void 분석서_삭제_API() throws Exception {
        // given
        Long analysisId = 1L;
        // Service의 void 메서드 모킹
        doNothing().when(ideaAnalysisService).deleteAnalysis(anyLong(), eq(analysisId));

        // when & then
        mockMvc.perform(delete("/api/v1/analysis/{analysisId}", analysisId)
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                )
                .andExpect(status().isOk())
                .andDo(document("idea-analysis-delete",
                        resource(ResourceSnippetParameters.builder()
                                .tag("아이디어 분석")
                                .summary("분석서 삭제")
                                .description("""
                                    사용자의 아이디어 분석 결과를 삭제합니다.
                                    
                                    **제약 사항:**
                                    - 본인의 분석서만 삭제할 수 있습니다.
                                    - 존재하지 않는 분석서 ID인 경우 에러가 반환됩니다.
                                    
                                    **연관 데이터:**
                                    - 팀 구성, 보완점, 주차별 로드맵 등 모든 하위 데이터가 함께 삭제됩니다.
                                    """)
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("analysisId").description("삭제할 분석서 ID")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("응답 상태 코드"),
                                        fieldWithPath("status.message").description("응답 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional()
                                        // 응답 바디가 ApiResponse<Void>이므로 body 필드는 기술하지 않습니다.
                                )
                                .build()
                        )
                ));
    }

}