package com.nect.api.domain.team.process.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.process.dto.req.ProcessTaskItemReorderReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessTaskItemUpsertReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessTaskItemReorderResDto;
import com.nect.api.domain.team.process.dto.res.ProcessTaskItemResDto;
import com.nect.api.domain.team.process.service.ProcessTaskItemService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class ProcessTaskItemControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProcessTaskItemService processTaskItemService;

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
    @DisplayName("업무 항목 생성")
    void createTaskItem() throws Exception {
        long projectId = 1L;
        long processId = 10L;
        long userId = 1L;

        ProcessTaskItemUpsertReqDto request = new ProcessTaskItemUpsertReqDto(
                "세부 작업 1",
                false,
                0
        );

        ProcessTaskItemResDto response = new ProcessTaskItemResDto(
                100L,
                "세부 작업 1",
                false,
                0,
                null
        );

        given(processTaskItemService.create(eq(projectId), eq(userId), eq(processId), any(ProcessTaskItemUpsertReqDto.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/projects/{projectId}/processes/{processId}/task-items", projectId, processId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("process-taskitem-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process-TaskItem")
                                        .summary("업무 항목 생성")
                                        .description("프로세스(카드)에 업무 항목(TaskItem)을 생성합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("processId").description("프로세스 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("content").type(STRING).description("업무 항목 내용"),
                                                fieldWithPath("is_done").optional().type(BOOLEAN).description("완료 여부(기본 false)"),
                                                fieldWithPath("sort_order").optional().type(NUMBER).description("삽입 위치(미입력 시 마지막)")
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

        verify(processTaskItemService).create(eq(projectId), eq(userId), eq(processId), any(ProcessTaskItemUpsertReqDto.class));
    }

    @Test
    @DisplayName("업무 항목 수정")
    void updateTaskItem() throws Exception {
        long projectId = 1L;
        long processId = 10L;
        long taskItemId = 100L;
        long userId = 1L;

        ProcessTaskItemUpsertReqDto request = new ProcessTaskItemUpsertReqDto(
                "수정된 세부 작업",
                true,
                1
        );

        ProcessTaskItemResDto response = new ProcessTaskItemResDto(
                taskItemId,
                "수정된 세부 작업",
                true,
                1,
                LocalDate.of(2026, 1, 25)
        );

        given(processTaskItemService.update(eq(projectId), eq(userId), eq(processId), eq(taskItemId), any(ProcessTaskItemUpsertReqDto.class)))
                .willReturn(response);

        mockMvc.perform(patch("/api/v1/projects/{projectId}/processes/{processId}/task-items/{taskItemId}", projectId, processId, taskItemId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("process-taskitem-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process-TaskItem")
                                        .summary("업무 항목 수정")
                                        .description("업무 항목(TaskItem)의 내용/완료여부/정렬순서를 수정합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("processId").description("프로세스 ID"),
                                                ResourceDocumentation.parameterWithName("taskItemId").description("업무 항목 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("content").optional().type(STRING).description("업무 항목 내용"),
                                                fieldWithPath("is_done").optional().type(BOOLEAN).description("완료 여부"),
                                                fieldWithPath("sort_order").optional().type(NUMBER).description("정렬 순서(인덱스)")
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

        verify(processTaskItemService).update(eq(projectId), eq(userId), eq(processId), eq(taskItemId), any(ProcessTaskItemUpsertReqDto.class));
    }

    @Test
    @DisplayName("업무 항목 삭제")
    void deleteTaskItem() throws Exception {
        long projectId = 1L;
        long processId = 10L;
        long taskItemId = 100L;
        long userId = 1L;

        willDoNothing().given(processTaskItemService).delete(eq(projectId), eq(userId), eq(processId), eq(taskItemId));

        mockMvc.perform(delete("/api/v1/projects/{projectId}/processes/{processId}/task-items/{taskItemId}", projectId, processId, taskItemId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("process-taskitem-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process-TaskItem")
                                        .summary("업무 항목 삭제")
                                        .description("업무 항목(TaskItem)을 삭제합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("processId").description("프로세스 ID"),
                                                ResourceDocumentation.parameterWithName("taskItemId").description("업무 항목 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.task_item_id").type(NUMBER).description("삭제된 업무 항목 ID")
                                        )
                                        .build()
                        )
                ));

        verify(processTaskItemService).delete(eq(projectId), eq(userId), eq(processId), eq(taskItemId));
    }

    @Test
    @DisplayName("업무 항목 드래그 정렬 저장")
    void reorderTaskItems() throws Exception {
        long projectId = 1L;
        long processId = 10L;
        long userId = 1L;

        ProcessTaskItemReorderReqDto request = new ProcessTaskItemReorderReqDto(
                List.of(100L, 101L, 102L)
        );

        ProcessTaskItemResDto i0 = new ProcessTaskItemResDto(100L, "A", false, 0, null);
        ProcessTaskItemResDto i1 = new ProcessTaskItemResDto(101L, "B", false, 1, null);
        ProcessTaskItemResDto i2 = new ProcessTaskItemResDto(102L, "C", true, 2, LocalDate.of(2026, 1, 25));

        ProcessTaskItemReorderResDto response = new ProcessTaskItemReorderResDto(
                processId,
                List.of(i0, i1, i2)
        );

        given(processTaskItemService.reorder(eq(projectId), eq(userId), eq(processId), any(ProcessTaskItemReorderReqDto.class)))
                .willReturn(response);

        mockMvc.perform(patch("/api/v1/projects/{projectId}/processes/{processId}/task-items/reorder", projectId, processId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("process-taskitem-reorder",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process-TaskItem")
                                        .summary("업무 항목 드래그 정렬 저장")
                                        .description("업무 항목의 정렬 순서를 드래그&드롭 결과(전체 목록) 기준으로 저장합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("processId").description("프로세스 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("ordered_task_item_ids").type(ARRAY).description("정렬된 업무 항목 ID 목록(전체 포함)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.process_id").type(NUMBER).description("프로세스 ID"),

                                                fieldWithPath("body.ordered_task_items").type(ARRAY).description("정렬 반영 후 업무 항목 목록"),
                                                fieldWithPath("body.ordered_task_items[].task_item_id").type(NUMBER).description("업무 항목 ID"),
                                                fieldWithPath("body.ordered_task_items[].content").type(STRING).description("업무 항목 내용"),
                                                fieldWithPath("body.ordered_task_items[].is_done").type(BOOLEAN).description("완료 여부"),
                                                fieldWithPath("body.ordered_task_items[].sort_order").type(NUMBER).description("정렬 순서"),
                                                fieldWithPath("body.ordered_task_items[].done_at").optional().type(STRING).description("완료일(yyyy-MM-dd, null 가능)")
                                        )
                                        .build()
                        )
                ));

        verify(processTaskItemService).reorder(eq(projectId), eq(userId), eq(processId), any(ProcessTaskItemReorderReqDto.class));
    }
}
