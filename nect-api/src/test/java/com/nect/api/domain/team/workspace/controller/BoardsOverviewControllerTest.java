package com.nect.api.domain.team.workspace.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.domain.team.workspace.dto.res.*;
import com.nect.api.domain.team.workspace.facade.BoardsOverviewFacade;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.entity.team.workspace.enums.PostType;
import com.nect.core.entity.user.enums.RoleField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class BoardsOverviewControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BoardsOverviewFacade boardsOverviewFacade;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

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

    private RequestPostProcessor mockUser(Long userId) {
        UserDetailsImpl principal = UserDetailsImpl.builder()
                .userId(userId)
                .roles(List.of("ROLE_MEMBER"))
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                "",
                principal.getAuthorities()
        );

        return SecurityMockMvcRequestPostProcessors.authentication(auth);
    }

    @Test
    @DisplayName("팀보드 통합(전체) 조회 - calendar_month_indicators 없음")
    void getOverview_withoutCalendarIndicators() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        BoardsBasicInfoGetResDto basicInfo = new BoardsBasicInfoGetResDto(
                projectId,
                "프로젝트 제목",
                "프로젝트 설명",
                "공지 텍스트",
                "정기회의 텍스트",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1),
                10L,
                true
        );

        MissionProgressResDto missionProgress = new MissionProgressResDto(
                new MissionProgressResDto.TotalDto(12L, 7L, 0.5833333333),
                List.of(
                        new MissionProgressResDto.TeamDto(
                                RoleFieldDto.of(RoleField.BACKEND),
                                5L, 3L, 0.6
                        ),
                        new MissionProgressResDto.TeamDto(
                                RoleFieldDto.of(RoleField.CUSTOM, "기획-운영"),
                                7L, 4L, 0.5714285714
                        )
                )
        );

        MemberBoardResDto members = new MemberBoardResDto(
                List.of(
                        new MemberBoardResDto.MemberDto(
                                1L,
                                "홍길동",
                                "길동",
                                null,
                                RoleFieldDto.of(RoleField.BACKEND),
                                ProjectMemberType.LEADER,
                                new MemberBoardResDto.CountsDto(1, 2, 3),
                                true,
                                3600L,
                                LocalDateTime.of(2026, 1, 31, 10, 0, 0)
                        )
                )
        );

        ScheduleUpcomingResDto upcomingSchedules = new ScheduleUpcomingResDto(
                List.of(
                        new ScheduleUpcomingResDto.Item(
                                101L,
                                "주간 회의",
                                LocalDateTime.of(2026, 2, 1, 10, 0, 0),
                                LocalDateTime.of(2026, 2, 1, 11, 0, 0),
                                false,
                                false
                        ),
                        new ScheduleUpcomingResDto.Item(
                                102L,
                                "해커톤(멀티데이)",
                                LocalDateTime.of(2026, 2, 3, 0, 0, 0),
                                LocalDateTime.of(2026, 2, 5, 23, 59, 59),
                                true,
                                true
                        )
                )
        );

        PostListResDto postsPreview = new PostListResDto(
                List.of(
                        new PostListResDto.PostSummaryDto(
                                1L,
                                PostType.NOTICE,
                                "공지 제목",
                                "공지 내용 프리뷰...",
                                true,
                                10L,
                                LocalDateTime.of(2026, 1, 31, 12, 0, 0)
                        ),
                        new PostListResDto.PostSummaryDto(
                                2L,
                                PostType.FREE,
                                "자유 글 제목",
                                "자유 글 프리뷰...",
                                false,
                                3L,
                                LocalDateTime.of(2026, 1, 30, 9, 30, 0)
                        )
                ),
                new PostListResDto.PageInfo(
                        0,      // page
                        4,      // size
                        12L,    // total_elements
                        3,      // total_pages
                        true    // has_next
                )
        );

        SharedDocumentsPreviewResDto sharedDocs = new SharedDocumentsPreviewResDto(List.of());

        BoardsOverviewResDto response = BoardsOverviewResDto.of(
                basicInfo,
                missionProgress,
                members,
                upcomingSchedules,
                sharedDocs,
                postsPreview,
                null // calendar_month_indicators 없음
        );

        given(boardsOverviewFacade.getOverview(
                eq(projectId), eq(userId),
                isNull(), isNull(),
                any(), anyInt(), anyInt(), anyInt(),
                isNull()
        )).willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/boards/overview", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("scheduleLimit", "6")
                        .param("docsLimit", "4")
                        .param("postsLimit", "4")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("boards-overview-get-without-calendar",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Boards")
                                        .summary("팀보드 통합(전체) 조회")
                                        .description("팀보드 화면에 필요한 카드들을 한 번에 조회합니다. year/month가 없으면 calendar_month_indicators는 null입니다.")
                                        .pathParameters(
                                                parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .queryParameters(
                                                parameterWithName("year").optional().description("캘린더 인디케이터 연도(옵션)"),
                                                parameterWithName("month").optional().description("캘린더 인디케이터 월(옵션)"),
                                                parameterWithName("from").optional().description("다가오는 일정 기준일(yyyy-MM-dd, 옵션)"),
                                                parameterWithName("scheduleLimit").optional().description("다가오는 일정 limit (기본 6)"),
                                                parameterWithName("docsLimit").optional().description("공유 문서 프리뷰 limit (기본 4)"),
                                                parameterWithName("postsLimit").optional().description("게시글 프리뷰 limit (기본 4)"),
                                                parameterWithName("postType").optional().description("게시판 프리뷰 타입(옵션)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),

                                                subsectionWithPath("body.basic_info").type(OBJECT).description("팀보드 기본 정보 카드"),
                                                subsectionWithPath("body.mission_progress").type(OBJECT).description("미션 진행도 카드"),
                                                subsectionWithPath("body.members").type(OBJECT).description("팀원 프로필 보드 카드"),
                                                subsectionWithPath("body.upcoming_schedules").type(OBJECT).description("다가오는 팀 일정 카드"),
                                                subsectionWithPath("body.shared_documents_preview").type(OBJECT).description("공유 문서함 프리뷰 카드"),
                                                subsectionWithPath("body.posts_preview").type(OBJECT).description("게시글 프리뷰 카드"),
                                                fieldWithPath("body.calendar_month_indicators").optional().type(OBJECT).description("캘린더 월간 인디케이터(옵션, 없으면 null)")
                                        )
                                        .build()
                        )
                ));

        verify(boardsOverviewFacade).getOverview(
                eq(projectId), eq(userId),
                isNull(), isNull(),
                any(), anyInt(), anyInt(), anyInt(),
                isNull()
        );
    }

    @Test
    @DisplayName("팀보드 통합(전체) 조회 - calendar_month_indicators 포함")
    void getOverview_withCalendarIndicators() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        CalendarMonthIndicatorsResDto indicators = new CalendarMonthIndicatorsResDto(
                2026,
                1,
                List.of(
                        new CalendarMonthIndicatorsResDto.DayIndicator(LocalDate.of(2026, 1, 5), 2),
                        new CalendarMonthIndicatorsResDto.DayIndicator(LocalDate.of(2026, 1, 20), 1)
                )
        );

        BoardsOverviewResDto response = BoardsOverviewResDto.of(
                new BoardsBasicInfoGetResDto(
                        projectId, "프로젝트 제목", "프로젝트 설명",
                        "공지 텍스트", "정기회의 텍스트",
                        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1),
                        10L, true
                ),
                new MissionProgressResDto(
                        new MissionProgressResDto.TotalDto(12L, 7L, 0.58),
                        List.of()
                ),
                new MemberBoardResDto(List.of()),
                new ScheduleUpcomingResDto(List.of()),
                new SharedDocumentsPreviewResDto(List.of()),
                new PostListResDto(
                        List.of(),
                        new PostListResDto.PageInfo(
                                0,      // page
                                0,      // size
                                0L,     // total_elements
                                0,      // total_pages
                                false   // has_next
                        )
                ),
                indicators
        );

        given(boardsOverviewFacade.getOverview(
                eq(projectId), eq(userId),
                eq(2026), eq(1),
                any(), anyInt(), anyInt(), anyInt(),
                eq(PostType.NOTICE) // 예시
        )).willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/boards/overview", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("year", "2026")
                        .param("month", "1")
                        .param("postType", "NOTICE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("boards-overview-get-with-calendar",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Boards")
                                        .summary("팀보드 통합(전체) 조회 (캘린더 포함)")
                                        .description("year/month를 포함하면 calendar_month_indicators를 함께 내려줍니다.")
                                        .pathParameters(
                                                parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .queryParameters(
                                                parameterWithName("year").optional().description("캘린더 인디케이터 연도(옵션)"),
                                                parameterWithName("month").optional().description("캘린더 인디케이터 월(옵션)"),
                                                parameterWithName("from").optional().description("다가오는 일정 기준일(yyyy-MM-dd, 옵션)"),
                                                parameterWithName("scheduleLimit").optional().description("다가오는 일정 limit (기본 6)"),
                                                parameterWithName("docsLimit").optional().description("공유 문서 프리뷰 limit (기본 4)"),
                                                parameterWithName("postsLimit").optional().description("게시글 프리뷰 limit (기본 4)"),
                                                parameterWithName("postType").optional().description("게시판 프리뷰 타입(옵션)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),

                                                subsectionWithPath("body.basic_info").type(OBJECT).description("팀보드 기본 정보 카드"),
                                                subsectionWithPath("body.mission_progress").type(OBJECT).description("미션 진행도 카드"),
                                                subsectionWithPath("body.members").type(OBJECT).description("팀원 프로필 보드 카드"),
                                                subsectionWithPath("body.upcoming_schedules").type(OBJECT).description("다가오는 팀 일정 카드"),
                                                subsectionWithPath("body.shared_documents_preview").type(OBJECT).description("공유 문서함 프리뷰 카드"),
                                                subsectionWithPath("body.posts_preview").type(OBJECT).description("게시글 프리뷰 카드"),

                                                subsectionWithPath("body.calendar_month_indicators").type(OBJECT).description("캘린더 월간 인디케이터")
                                        )
                                        .build()
                        )
                ));

        verify(boardsOverviewFacade).getOverview(
                eq(projectId), eq(userId),
                eq(2026), eq(1),
                any(), anyInt(), anyInt(), anyInt(),
                eq(PostType.NOTICE)
        );
    }
}
