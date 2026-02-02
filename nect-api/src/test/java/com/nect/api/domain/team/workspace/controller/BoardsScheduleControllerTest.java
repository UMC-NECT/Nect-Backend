package com.nect.api.domain.team.workspace.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.workspace.dto.req.ScheduleCreateReqDto;
import com.nect.api.domain.team.workspace.dto.req.ScheduleUpdateReqDto;
import com.nect.api.domain.team.workspace.dto.res.CalendarMonthIndicatorsResDto;
import com.nect.api.domain.team.workspace.dto.res.ScheduleCreateResDto;
import com.nect.api.domain.team.workspace.dto.res.ScheduleUpcomingResDto;
import com.nect.api.domain.team.workspace.facade.BoardsScheduleFacade;
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
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class BoardsScheduleControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BoardsScheduleFacade facade;

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
    @DisplayName("캘린더 월간 인디케이터 조회")
    void getMonthIndicators() throws Exception {
        long projectId = 1L;
        long userId = 1L;
        int year = 2026;
        int month = 1;

        CalendarMonthIndicatorsResDto response = new CalendarMonthIndicatorsResDto(
                year,
                month,
                List.of(
                        new CalendarMonthIndicatorsResDto.DayIndicator(LocalDate.of(2026, 1, 3), 2),
                        new CalendarMonthIndicatorsResDto.DayIndicator(LocalDate.of(2026, 1, 10), 1)
                )
        );

        given(facade.getMonthIndicators(eq(projectId), eq(userId), eq(year), eq(month))).willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/boards/calendar/month", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("boards-calendar-month-indicators-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Boards")
                                        .summary("캘린더 월간 인디케이터 조회")
                                        .description("팀보드 우측 상단 캘린더에서 해당 월에 일정이 있는 날짜를 표시하기 위한 API입니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .queryParameters(
                                                ResourceDocumentation.parameterWithName("year").description("조회 연도"),
                                                ResourceDocumentation.parameterWithName("month").description("조회 월(1~12)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.year").type(NUMBER).description("조회 연도"),
                                                fieldWithPath("body.month").type(NUMBER).description("조회 월(1~12)"),

                                                fieldWithPath("body.days").type(ARRAY).description("날짜별 일정 인디케이터"),
                                                fieldWithPath("body.days[].date").type(STRING).description("날짜(yyyy-MM-dd)"),
                                                fieldWithPath("body.days[].event_count").type(NUMBER).description("해당 날짜 일정 개수")
                                        )
                                        .build()
                        )
                ));

        verify(facade).getMonthIndicators(eq(projectId), eq(userId), eq(year), eq(month));
    }

    @Test
    @DisplayName("다가오는 팀 일정 조회")
    void getUpcoming() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        String from = "2026-01-31";
        int limit = 6;

        // TODO: ScheduleUpcomingResDto 실제 JSON 구조에 맞춰 bodyJson 수정
        String bodyJson = """
            {
              "from": "2026-01-31",
              "limit": 6,
              "schedules": [
                {
                  "schedule_id": 1,
                  "title": "회의",
                  "start_at": "2026-02-01T10:00:00",
                  "end_at": "2026-02-01T11:00:00"
                }
              ]
            }
            """;

        ScheduleUpcomingResDto response =
                objectMapper.readValue(bodyJson, ScheduleUpcomingResDto.class);

        given(facade.getUpcoming(eq(projectId), eq(userId), eq(from), eq(limit))).willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/boards/schedules/upcoming", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("from", from)
                        .param("limit", String.valueOf(limit))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("boards-schedules-upcoming-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Boards")
                                        .summary("다가오는 팀 일정 조회")
                                        .description("프로젝트의 다가오는 팀 일정을 조회합니다. from(기준일) 이후 일정들을 limit 개수만큼 반환합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .queryParameters(
                                                ResourceDocumentation.parameterWithName("from").optional().description("기준 날짜(yyyy-MM-dd). 미입력 시 서버 기본값"),
                                                ResourceDocumentation.parameterWithName("limit").optional().description("최대 조회 개수 (default=6)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),
                                                subsectionWithPath("body").type(OBJECT).description("응답 바디 (다가오는 일정 리스트)")
                                        )
                                        .build()
                        )
                ));

        verify(facade).getUpcoming(eq(projectId), eq(userId), eq(from), eq(limit));
    }

    @Test
    @DisplayName("팀 일정 생성")
    void createSchedule() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        ScheduleCreateReqDto request = new ScheduleCreateReqDto(
                "회의",
                "주간 회의입니다.",
                LocalDateTime.of(2026, 2, 1, 10, 0),
                LocalDateTime.of(2026, 2, 1, 11, 0),
                false
        );

        ScheduleCreateResDto response = new ScheduleCreateResDto(10L);

        given(facade.create(eq(projectId), eq(userId), any(ScheduleCreateReqDto.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/projects/{projectId}/boards/schedules", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("boards-schedules-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Boards")
                                        .summary("팀 일정 생성")
                                        .description("프로젝트 팀 일정을 생성합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("title").type(STRING).description("일정 제목"),
                                                fieldWithPath("description").optional().type(STRING).description("일정 설명"),
                                                fieldWithPath("start_at").type(STRING).description("시작 일시(ISO-8601)"),
                                                fieldWithPath("end_at").type(STRING).description("종료 일시(ISO-8601)"),
                                                fieldWithPath("all_day").type(BOOLEAN).description("종일 여부")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.schedule_id").type(NUMBER).description("생성된 일정 ID")
                                        )
                                        .build()
                        )
                ));

        verify(facade).create(eq(projectId), eq(userId), any(ScheduleCreateReqDto.class));
    }


    @Test
    @DisplayName("팀 일정 수정")
    void update() throws Exception {
        long projectId = 1L;
        long scheduleId = 10L;
        long userId = 1L;

        ScheduleUpdateReqDto request = new ScheduleUpdateReqDto(
                "수정된 제목",
                "수정된 설명",
                LocalDateTime.of(2026, 2, 1, 10, 0),
                LocalDateTime.of(2026, 2, 1, 11, 0),
                false
        );

        willDoNothing().given(facade).update(eq(projectId), eq(userId), eq(scheduleId), any(ScheduleUpdateReqDto.class));

        mockMvc.perform(patch("/api/v1/projects/{projectId}/boards/schedules/{scheduleId}", projectId, scheduleId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("boards-schedules-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Boards")
                                        .summary("팀 일정 수정")
                                        .description("프로젝트의 특정 팀 일정을 수정합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("scheduleId").description("일정 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("title").optional().type(STRING).description("제목"),
                                                fieldWithPath("description").optional().type(STRING).description("설명"),
                                                fieldWithPath("start_at").optional().type(STRING).description("시작 일시(ISO-8601)"),
                                                fieldWithPath("end_at").optional().type(STRING).description("종료 일시(ISO-8601)"),
                                                fieldWithPath("all_day").optional().type(BOOLEAN).description("종일 여부")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명")
                                        )
                                        .build()
                        )
                ));

        verify(facade).update(eq(projectId), eq(userId), eq(scheduleId), any(ScheduleUpdateReqDto.class));
    }

    @Test
    @DisplayName("팀 일정 삭제")
    void deleteSchedule() throws Exception {
        long projectId = 1L;
        long scheduleId = 10L;
        long userId = 1L;

        willDoNothing().given(facade).delete(eq(projectId), eq(userId), eq(scheduleId));

        mockMvc.perform(delete("/api/v1/projects/{projectId}/boards/schedules/{scheduleId}", projectId, scheduleId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("boards-schedules-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Boards")
                                        .summary("팀 일정 삭제")
                                        .description("프로젝트의 특정 팀 일정을 삭제합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("scheduleId").description("일정 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명")
                                        )
                                        .build()
                        )
                ));

        verify(facade).delete(eq(projectId), eq(userId), eq(scheduleId));
    }
}
