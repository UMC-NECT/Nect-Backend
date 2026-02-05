package com.nect.api.domain.team.process.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.process.dto.req.WeekMissionStatusUpdateReqDto;
import com.nect.api.domain.team.process.dto.req.WeekMissionTaskItemUpdateReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessTaskItemResDto;
import com.nect.api.domain.team.process.dto.res.WeekMissionDetailResDto;
import com.nect.api.domain.team.process.dto.res.WeekMissionWeekResDto;
import com.nect.api.domain.team.process.service.WeekMissionService;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
import com.nect.core.entity.team.process.enums.ProcessStatus;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class WeekMissionControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WeekMissionService weekMissionService;

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

    private <T> T newRecord(Class<T> recordType) {
        try {
            if (!recordType.isRecord()) return null;

            RecordComponent[] components = recordType.getRecordComponents();
            Class<?>[] paramTypes = new Class<?>[components.length];
            Object[] args = new Object[components.length];

            for (int i = 0; i < components.length; i++) {
                Class<?> t = components[i].getType();
                paramTypes[i] = t;
                args[i] = defaultValue(t);
            }

            Constructor<T> ctor = recordType.getDeclaredConstructor(paramTypes);
            ctor.setAccessible(true);
            return ctor.newInstance(args);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to instantiate record: " + recordType.getName(), e);
        }
    }

    private Object defaultValue(Class<?> t) {
        if (t == String.class) return "sample";
        if (t == Long.class || t == long.class) return 1L;
        if (t == Integer.class || t == int.class) return 1;
        if (t == Boolean.class || t == boolean.class) return false;
        if (t == LocalDate.class) return LocalDate.of(2026, 1, 19);
        if (t == LocalDateTime.class) return LocalDateTime.of(2026, 1, 19, 0, 0, 0);

        if (List.class.isAssignableFrom(t)) return List.of();

        if (t.isEnum()) {
            Object[] constants = t.getEnumConstants();
            return (constants != null && constants.length > 0) ? constants[0] : null;
        }

        if (t.isRecord()) {
            @SuppressWarnings("unchecked")
            Class<Object> rt = (Class<Object>) t;
            return newRecord(rt);
        }

        return null;
    }

    @Test
    @DisplayName("주차별 위크미션 조회")
    void getWeekMissions() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        LocalDate startDate = LocalDate.of(2026, 1, 19);
        int weeks = 2;

        WeekMissionWeekResDto response = newRecord(WeekMissionWeekResDto.class);

        given(weekMissionService.getWeekMissions(eq(projectId), eq(userId), eq(startDate), eq(weeks)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/week-missions/week", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("start_date", startDate.toString())
                        .param("weeks", String.valueOf(weeks))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("week-mission-week",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Week-Mission")
                                        .summary("주차별 위크미션 조회")
                                        .description("start_date 기준으로 weeks 만큼 위크미션 주차 목록을 조회합니다. start_date 미입력 시 서버 정책에 따른 기본 시작일로 동작합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .queryParameters(
                                                parameterWithName("start_date").optional().description("시작일(yyyy-MM-dd)"),
                                                parameterWithName("weeks").optional().description("조회할 주차 수(기본 1)")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                subsectionWithPath("body").type(OBJECT).description("주차별 위크미션 조회 결과")
                                        )
                                        .build()
                        )
                ));

        verify(weekMissionService).getWeekMissions(eq(projectId), eq(userId), eq(startDate), eq(weeks));
    }

    @Test
    @DisplayName("위크미션 상세 조회(체크리스트 포함)")
    void getWeekMissionDetail() throws Exception {
        long projectId = 1L;
        long processId = 10L;
        long userId = 1L;

        WeekMissionDetailResDto response = newRecord(WeekMissionDetailResDto.class);

        given(weekMissionService.getDetail(eq(projectId), eq(userId), eq(processId)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/week-missions/{processId}", projectId, processId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("week-mission-detail",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Week-Mission")
                                        .summary("위크미션 상세 조회")
                                        .description("위크미션(프로세스) 상세를 조회합니다. (체크리스트 포함)")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("processId").description("위크미션 프로세스 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                subsectionWithPath("body").type(OBJECT).description("위크미션 상세 결과")
                                        )
                                        .build()
                        )
                ));

        verify(weekMissionService).getDetail(eq(projectId), eq(userId), eq(processId));
    }

    @Test
    @DisplayName("위크미션 상태 변경")
    void updateWeekMissionStatus() throws Exception {
        long projectId = 1L;
        long processId = 10L;
        long userId = 1L;

        WeekMissionStatusUpdateReqDto request =
                new WeekMissionStatusUpdateReqDto(ProcessStatus.PLANNING);

        willDoNothing().given(weekMissionService)
                .updateWeekMissionStatus(eq(projectId), eq(userId), eq(processId), any(WeekMissionStatusUpdateReqDto.class));

        mockMvc.perform(patch("/api/v1/projects/{projectId}/week-missions/{processId}/status", projectId, processId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("week-mission-status-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Week-Mission")
                                        .summary("위크미션 상태 변경")
                                        .description("위크미션 프로세스의 상태를 변경합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("processId").description("위크미션 프로세스 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("status").type(STRING)
                                                        .description("변경할 상태(PLANNING/IN_PROGRESS/DONE/BACKLOG)")
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

        verify(weekMissionService).updateWeekMissionStatus(eq(projectId), eq(userId), eq(processId), any(WeekMissionStatusUpdateReqDto.class));
    }

    @Test
    @DisplayName("위크미션 TASK 내 항목 내용 수정")
    void updateWeekMissionTaskItem() throws Exception {
        long projectId = 1L;
        long processId = 10L;
        long taskItemId = 100L;
        long userId = 1L;

        WeekMissionTaskItemUpdateReqDto request = newRecord(WeekMissionTaskItemUpdateReqDto.class);

        ProcessTaskItemResDto response = new ProcessTaskItemResDto(
                taskItemId,
                "수정된 세부 작업",
                true,
                1,
                LocalDate.of(2026, 1, 25)
        );

        given(weekMissionService.updateWeekMissionTaskItem(eq(projectId), eq(userId), eq(processId), eq(taskItemId), any(WeekMissionTaskItemUpdateReqDto.class)))
                .willReturn(response);

        mockMvc.perform(patch("/api/v1/projects/{projectId}/week-missions/{processId}/task-items/{taskItemId}", projectId, processId, taskItemId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("week-mission-taskitem-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Week-Mission")
                                        .summary("위크미션 TASK 항목 수정")
                                        .description("위크미션 프로세스 내 TaskItem의 내용을 수정합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("processId").description("위크미션 프로세스 ID"),
                                                ResourceDocumentation.parameterWithName("taskItemId").description("업무 항목(TaskItem) ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("content").type(STRING).description("업무 항목 내용"),
                                                fieldWithPath("is_done").type(BOOLEAN).description("완료 여부"),
                                                fieldWithPath("role_field").optional().type(STRING).description("역할 분야(RoleField)"),
                                                fieldWithPath("custom_role_field_name").optional().type(STRING).description("커스텀 역할 분야명(CUSTOM일 때)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.task_item_id").type(NUMBER).description("업무 항목 ID"),
                                                fieldWithPath("body.content").type(STRING).description("업무 항목 내용"),
                                                fieldWithPath("body.is_done").type(BOOLEAN).description("완료 여부"),
                                                fieldWithPath("body.sort_order").type(NUMBER).description("정렬 순서"),
                                                fieldWithPath("body.done_at").optional().type(STRING).description("완료일(yyyy-MM-dd, null 가능)")
                                        )
                                        .build()
                        )
                ));

        verify(weekMissionService).updateWeekMissionTaskItem(eq(projectId), eq(userId), eq(processId), eq(taskItemId), any(WeekMissionTaskItemUpdateReqDto.class));
    }
}
