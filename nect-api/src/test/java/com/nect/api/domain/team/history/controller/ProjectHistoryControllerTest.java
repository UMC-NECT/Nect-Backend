package com.nect.api.domain.team.history.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.history.dto.res.ProjectHistoryListResDto;
import com.nect.api.domain.team.history.dto.res.ProjectHistoryResDto;
import com.nect.api.domain.team.history.service.ProjectHistoryService;
import com.nect.core.entity.team.history.enums.HistoryAction;
import com.nect.core.entity.team.history.enums.HistoryTargetType;
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

import java.time.LocalDateTime;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
@Transactional
class ProjectHistoryControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectHistoryService historyService;

    @Test
    @DisplayName("팀 히스토리 로그 조회(커서 기반, 최근 10개 고정)")
    void getHistories() throws Exception {
        // given
        long projectId = 1L;
        Long cursor = 100L;

        ProjectHistoryListResDto response = new ProjectHistoryListResDto(
                99L, // next_cursor (예시)
                List.of(
                        new ProjectHistoryResDto(
                                100L,                       // history_id
                                2L,                         // actor_user_id
                                HistoryAction.PROCESS_CREATED,
                                HistoryTargetType.PROCESS,
                                10L,                        // target_id
                                "{\"foo\":\"bar\"}",
                                LocalDateTime.of(2026, 1, 24, 12, 0, 0)
                        ),
                        new ProjectHistoryResDto(
                                99L,
                                3L,
                                HistoryAction.PROCESS_UPDATED,
                                HistoryTargetType.TASK_ITEM,
                                55L,
                                "{\"before\":\"A\",\"after\":\"B\"}",
                                LocalDateTime.of(2026, 1, 24, 12, 1, 0)
                        )
                )
        );

        given(historyService.getHistories(eq(projectId), eq(cursor)))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/projects/{projectId}/histories", projectId)
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("cursor", String.valueOf(cursor))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("project-history-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("History")
                                        .summary("팀 히스토리 로그 조회")
                                        .description("프로젝트 내 팀 히스토리 로그를 커서 기반으로 조회합니다. cursor 미입력 시 최신부터 조회합니다. (서버 정책: 최근 10개 고정)")
                                        .pathParameters(
                                                com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName("projectId")
                                                        .description("프로젝트 ID")
                                        )
                                        .queryParameters(
                                                parameterWithName("cursor").optional().description("커서(이전 페이지 마지막 history_id 등)")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).optional().description("Bearer Access Token")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.next_cursor").optional().type(NUMBER)
                                                        .description("다음 페이지 조회를 위한 커서(null이면 다음 페이지 없음)"),

                                                fieldWithPath("body.items").type(ARRAY).description("히스토리 로그 목록(최대 10개)"),

                                                fieldWithPath("body.items[].history_id").type(NUMBER).description("히스토리 ID"),
                                                fieldWithPath("body.items[].actor_user_id").type(NUMBER).description("행위자(유저) ID"),
                                                fieldWithPath("body.items[].action").type(STRING).description("액션 타입(HistoryAction)"),
                                                fieldWithPath("body.items[].target_type").type(STRING).description("대상 타입(HistoryTargetType)"),
                                                fieldWithPath("body.items[].target_id").type(NUMBER).description("대상 ID"),
                                                fieldWithPath("body.items[].meta_json").optional().type(STRING).description("메타 정보(JSON 문자열)"),
                                                fieldWithPath("body.items[].created_at").type(STRING).description("생성일시(ISO-8601)")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    @DisplayName("팀 히스토리 로그 조회(기본: cursor 미입력, 최근 10개 고정)")
    void getHistories_withoutParams() throws Exception {
        // given
        long projectId = 1L;

        ProjectHistoryListResDto response = new ProjectHistoryListResDto(
                null,
                List.of()
        );

        given(historyService.getHistories(eq(projectId), eq(null)))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/projects/{projectId}/histories", projectId)
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("project-history-list-default",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("History")
                                        .summary("팀 히스토리 로그 조회(기본)")
                                        .description("cursor 미입력 시 서버 정책으로 최신 로그부터 조회합니다. (서버 정책: 최근 10개 고정)")
                                        .pathParameters(
                                                com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName("projectId")
                                                        .description("프로젝트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).optional().description("Bearer Access Token")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.next_cursor").optional().type(NUMBER)
                                                        .description("다음 페이지 조회를 위한 커서(null이면 다음 페이지 없음)"),
                                                fieldWithPath("body.items").type(ARRAY).description("히스토리 로그 목록(최대 10개)")
                                        )
                                        .build()
                        )
                ));
    }
}
