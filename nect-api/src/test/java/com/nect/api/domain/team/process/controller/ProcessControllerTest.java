package com.nect.api.domain.team.process.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.process.dto.req.*;
import com.nect.api.domain.team.process.dto.res.*;
import com.nect.api.domain.team.process.service.ProcessService;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
import com.nect.core.entity.team.enums.FileExt;
import com.nect.core.entity.team.process.enums.ProcessFeedbackStatus;
import com.nect.core.entity.team.process.enums.ProcessStatus;
import com.nect.core.entity.user.enums.RoleField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class ProcessControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProcessService processService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUpAuth() {
        // 토큰 검증 통과
        doNothing().when(jwtUtil).validateToken(anyString());
        // 블랙리스트 아님
        given(tokenBlacklistService.isBlacklisted(anyString())).willReturn(false);
        // 토큰에서 userId 추출
        given(jwtUtil.getUserIdFromToken(anyString())).willReturn(1L);
        // 필터가 SecurityContext에 넣을 UserDetails
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
    @DisplayName("프로세스 생성")
    void createProcess() throws Exception {
        long projectId = 1L;
        long userId = 1L;
        long createdProcessId = 10L;

        ProcessCreateReqDto request = new ProcessCreateReqDto(
                "1주차 미션",
                "프로세스 내용",
                ProcessStatus.IN_PROGRESS,
                List.of(),
                List.of(),
                null,
                LocalDate.of(2026, 1, 19),
                LocalDate.of(2026, 1, 25),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        given(processService.createProcess(eq(projectId), eq(userId), any(ProcessCreateReqDto.class)))
                .willReturn(createdProcessId);

        mockMvc.perform(post("/api/v1/projects/{projectId}/processes", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("process-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process")
                                        .summary("프로세스 생성")
                                        .description("프로젝트에 새로운 프로세스(카드)를 생성합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("process_title").type(STRING).description("프로세스 제목"),
                                                fieldWithPath("process_content").type(STRING).description("프로세스 내용"),
                                                fieldWithPath("process_status").type(STRING).description("프로세스 상태"),

                                                fieldWithPath("assignee_ids").type(ARRAY).description("담당자 ID 목록"),
                                                fieldWithPath("role_fields").type(ARRAY).description("분야 목록 (예: BACKEND, FRONTEND 등)"),
                                                fieldWithPath("custom_field_name").optional().type(STRING).description("커스텀 분야명(null 가능)"),

                                                fieldWithPath("start_date").optional().type(STRING).description("시작일(yyyy-MM-dd, null 가능)"),
                                                fieldWithPath("dead_line").optional().type(STRING).description("마감일(yyyy-MM-dd, null 가능)"),

                                                fieldWithPath("mention_user_ids").type(ARRAY).description("멘션된 유저 ID 목록"),
                                                fieldWithPath("file_ids").type(ARRAY).description("첨부 파일 ID 목록"),

                                                fieldWithPath("links").type(ARRAY).description("첨부 링크 목록"),
                                                fieldWithPath("links[].url").optional().type(STRING).description("링크 URL"),

                                                fieldWithPath("task_items").type(ARRAY).description("업무 항목(TaskItem) 목록"),
                                                fieldWithPath("task_items[].content").optional().type(STRING).description("업무 항목 내용"),
                                                fieldWithPath("task_items[].is_done").optional().type(BOOLEAN).description("완료 여부"),
                                                fieldWithPath("task_items[].sort_order").optional().type(NUMBER).description("정렬 순서")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").description("응답 상태"),
                                                fieldWithPath("status.statusCode").description("상태 코드"),
                                                fieldWithPath("status.message").description("메시지"),
                                                fieldWithPath("status.description").optional().description("상세 설명(주로 에러 시)"),

                                                fieldWithPath("body").description("응답 바디"),
                                                fieldWithPath("body.process_id").description("생성된 프로세스 ID")
                                        )
                                        .build()
                        )
                ));

        verify(processService).createProcess(eq(projectId), eq(userId), any(ProcessCreateReqDto.class));
    }

    @Test
    @DisplayName("프로세스 상세 조회")
    void getProcessDetail() throws Exception {
        long projectId = 1L;
        long processId = 10L;
        long userId = 1L;

        FeedbackCreatedByResDto createdBy = new FeedbackCreatedByResDto(
                1L,
                "작성자",
                List.of("DESIGNER", "CUSTOM:UX Writer")
        );


        List<ProcessFeedbackCreateResDto> feedbacks = List.of(
                new ProcessFeedbackCreateResDto(
                        1L,
                        "피드백 내용",
                        ProcessFeedbackStatus.OPEN,
                        createdBy,
                        LocalDateTime.of(2026, 1, 24, 0, 0, 0)
                )
        );

        ProcessDetailResDto response = new ProcessDetailResDto(
                processId,
                "1주차 미션",
                "프로세스 내용",
                ProcessStatus.IN_PROGRESS,
                LocalDate.of(2026, 1, 19),
                LocalDate.of(2026, 1, 25),
                0,

                List.of(),
                List.of("디자인"),

                List.of(
                        new AssigneeResDto(1L, "유저1", "https://img.com/1.png"),
                        new AssigneeResDto(2L, "유저2", "https://img.com/2.png")
                ),
                List.of(3L, 4L),

                List.of(
                        new FileResDto(1001L, "spec.pdf", "https://s3.amazonaws.com/nect/spec.pdf", FileExt.PDF, 1024L),
                        new FileResDto(1002L, "image.jpg", "https://s3.amazonaws.com/nect/image.jpg", FileExt.JPG, 2048L)
                ),
                List.of(
                        new LinkResDto(1L, "https://a.com"),
                        new LinkResDto(2L, "https://b.com")
                ),
                List.of(
                        new ProcessTaskItemResDto(1L, "세부작업1", false, 0, null),
                        new ProcessTaskItemResDto(2L, "세부작업2", true, 1, LocalDate.of(2026, 1, 20))
                ),

                feedbacks,

                LocalDateTime.of(2026, 1, 19, 0, 0, 0),
                LocalDateTime.of(2026, 1, 24, 0, 0, 0),
                null
        );

        given(processService.getProcessDetail(eq(projectId), eq(userId), eq(processId)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/processes/{processId}", projectId, processId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("process-detail",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process")
                                        .summary("프로세스 상세 조회")
                                        .description("프로젝트 내 프로세스(카드) 상세 정보를 조회합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("processId").description("프로세스 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").description("응답 상태"),
                                                fieldWithPath("status.statusCode").description("상태 코드"),
                                                fieldWithPath("status.message").description("메시지"),
                                                fieldWithPath("status.description").optional().description("상세 설명(주로 에러 시)"),

                                                fieldWithPath("body").description("응답 바디"),
                                                fieldWithPath("body.process_id").type(NUMBER).description("프로세스 ID"),
                                                fieldWithPath("body.process_title").type(STRING).description("프로세스 제목"),
                                                fieldWithPath("body.process_content").type(STRING).description("프로세스 내용"),
                                                fieldWithPath("body.process_status").type(STRING).description("프로세스 상태"),
                                                fieldWithPath("body.start_date").type(STRING).description("시작일(yyyy-MM-dd)"),
                                                fieldWithPath("body.dead_line").type(STRING).description("마감일(yyyy-MM-dd)"),
                                                fieldWithPath("body.status_order").type(NUMBER).description("상태 내 정렬 순서"),

                                                fieldWithPath("body.role_fields").type(ARRAY).description("역할 분야 목록(RoleField)"),
                                                fieldWithPath("body.custom_fields").type(ARRAY).description("커스텀 분야명 목록"),

                                                fieldWithPath("body.assignees").type(ARRAY).description("담당자 목록"),
                                                fieldWithPath("body.assignees[].user_id").type(NUMBER).description("담당자 유저 ID"),
                                                fieldWithPath("body.assignees[].user_name").type(STRING).description("담당자 이름"),
                                                fieldWithPath("body.assignees[].user_image").type(STRING).description("담당자 이미지 URL"),

                                                fieldWithPath("body.mention_user_ids").type(ARRAY).description("멘션 유저 ID 목록"),

                                                fieldWithPath("body.files").type(ARRAY).description("첨부 파일 목록"),
                                                fieldWithPath("body.files[].file_id").type(NUMBER).description("파일 ID"),
                                                fieldWithPath("body.files[].file_name").type(STRING).description("파일명"),
                                                fieldWithPath("body.files[].file_url").type(STRING).description("파일 URL"),
                                                fieldWithPath("body.files[].file_type").type(STRING).description("파일 확장자(FileExt)"),
                                                fieldWithPath("body.files[].file_size").type(NUMBER).description("파일 크기(byte)"),

                                                fieldWithPath("body.links").type(ARRAY).description("링크 목록"),
                                                fieldWithPath("body.links[].link_id").type(NUMBER).description("링크 ID"),
                                                fieldWithPath("body.links[].url").type(STRING).description("링크 URL"),

                                                fieldWithPath("body.task_items").type(ARRAY).description("업무 항목 목록"),
                                                fieldWithPath("body.task_items[].task_item_id").type(NUMBER).description("업무 항목 ID"),
                                                fieldWithPath("body.task_items[].content").type(STRING).description("업무 내용"),
                                                fieldWithPath("body.task_items[].is_done").type(BOOLEAN).description("완료 여부"),
                                                fieldWithPath("body.task_items[].sort_order").type(NUMBER).description("정렬 순서"),
                                                fieldWithPath("body.task_items[].done_at").optional().type(STRING).description("완료일(yyyy-MM-dd, null 가능)"),

                                                fieldWithPath("body.feedbacks").type(ARRAY).description("피드백 목록"),
                                                fieldWithPath("body.feedbacks[].feedback_id").type(NUMBER).description("피드백 ID"),
                                                fieldWithPath("body.feedbacks[].content").type(STRING).description("피드백 내용"),
                                                fieldWithPath("body.feedbacks[].status").type(STRING).description("피드백 상태"),

                                                fieldWithPath("body.feedbacks[].created_by").type(OBJECT).description("작성자 정보"),
                                                fieldWithPath("body.feedbacks[].created_by.user_id").type(NUMBER).description("작성자 유저 ID"),
                                                fieldWithPath("body.feedbacks[].created_by.user_name").type(STRING).description("작성자 이름"),
                                                fieldWithPath("body.feedbacks[].created_by.role_fields").type(ARRAY).description("작성자 역할 분야 목록(RoleField/CUSTOM)"),

                                                fieldWithPath("body.feedbacks[].created_at").type(STRING).description("피드백 생성일시"),

                                                fieldWithPath("body.created_at").type(STRING).description("생성일시"),
                                                fieldWithPath("body.updated_at").type(STRING).description("수정일시"),
                                                fieldWithPath("body.deleted_at").optional().type(STRING).description("삭제일시(null 가능)")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    @DisplayName("프로세스 기본 정보 수정")
    void updateProcessBasic() throws Exception {
        long projectId = 1L;
        long processId = 10L;
        long userId = 1L;

        ProcessBasicUpdateReqDto request = new ProcessBasicUpdateReqDto(
                "수정된 제목",
                "수정된 내용",
                ProcessStatus.IN_PROGRESS,
                LocalDate.of(2026, 1, 20),
                LocalDate.of(2026, 1, 26),

                List.of(RoleField.FRONTEND, RoleField.BACKEND, RoleField.CUSTOM),
                List.of("AI"),

                List.of(1L, 2L),
                List.of(3L, 4L)
        );

        ProcessBasicUpdateResDto response = new ProcessBasicUpdateResDto(
                processId,
                "수정된 제목",
                "수정된 내용",
                ProcessStatus.IN_PROGRESS,
                LocalDate.of(2026, 1, 20),
                LocalDate.of(2026, 1, 26),

                List.of(RoleField.FRONTEND, RoleField.BACKEND, RoleField.CUSTOM),
                List.of("AI"),

                List.of(1L, 2L),
                List.of(3L, 4L),

                LocalDateTime.of(2026, 1, 24, 0, 0, 0)
        );

        given(processService.updateProcessBasic(eq(projectId), eq(userId), eq(processId), any(ProcessBasicUpdateReqDto.class)))
                .willReturn(response);

        mockMvc.perform(
                        patch("/api/v1/projects/{projectId}/processes/{processId}", projectId, processId)
                                .with(mockUser(userId))
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andDo(document("process-basic-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process")
                                        .summary("프로세스 기본 정보 수정")
                                        .description("프로세스(카드)의 기본 정보(제목/내용/상태/기간/담당자/분야/멘션)를 수정합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("processId").description("프로세스 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("process_title").optional().type(STRING).description("프로세스 제목"),
                                                fieldWithPath("process_content").optional().type(STRING).description("프로세스 내용"),
                                                fieldWithPath("process_status").optional().type(STRING).description("프로세스 상태"),

                                                fieldWithPath("start_date").optional().type(STRING).description("시작일(yyyy-MM-dd)"),
                                                fieldWithPath("dead_line").optional().type(STRING).description("마감일(yyyy-MM-dd)"),

                                                fieldWithPath("assignee_ids").optional().type(ARRAY).description("담당자 ID 목록 (미포함 시 변경 없음, []면 비우기)"),
                                                fieldWithPath("mention_user_ids").optional().type(ARRAY).description("멘션 유저 ID 목록 (미포함 시 변경 없음, []면 비우기)"),

                                                fieldWithPath("role_fields").optional().type(ARRAY).description("역할 분야 목록(RoleField)"),
                                                fieldWithPath("custom_fields").optional().type(ARRAY).description("커스텀 분야명 목록(CUSTOM 선택 시)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.process_id").type(NUMBER).description("프로세스 ID"),
                                                fieldWithPath("body.process_title").type(STRING).description("프로세스 제목"),
                                                fieldWithPath("body.process_content").type(STRING).description("프로세스 내용"),
                                                fieldWithPath("body.process_status").type(STRING).description("프로세스 상태"),
                                                fieldWithPath("body.start_date").type(STRING).description("시작일(yyyy-MM-dd)"),
                                                fieldWithPath("body.dead_line").type(STRING).description("마감일(yyyy-MM-dd)"),

                                                fieldWithPath("body.role_fields").type(ARRAY).description("역할 분야 목록(RoleField)"),
                                                fieldWithPath("body.custom_fields").type(ARRAY).description("커스텀 분야명 목록(CUSTOM 선택 시)"),

                                                fieldWithPath("body.assignee_ids").type(ARRAY).description("담당자 ID 목록"),
                                                fieldWithPath("body.mention_user_ids").type(ARRAY).description("멘션 유저 ID 목록"),
                                                fieldWithPath("body.updated_at").type(STRING).description("수정일시(ISO-8601)")
                                        )
                                        .build()
                        )
                ));

        verify(processService).updateProcessBasic(eq(projectId), eq(userId), eq(processId), any(ProcessBasicUpdateReqDto.class));
    }

    @Test
    @DisplayName("프로세스 삭제")
    void deleteProcess() throws Exception {
        long projectId = 1L;
        long processId = 10L;
        long userId = 1L;

        mockMvc.perform(delete("/api/v1/projects/{projectId}/processes/{processId}", projectId, processId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("process-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process")
                                        .summary("프로세스 삭제")
                                        .description("프로세스(카드)를 삭제합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("processId").description("프로세스 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명(주로 에러 시)"),

                                                fieldWithPath("body").type(NULL).optional().description("응답 바디(없음)")
                                        )
                                        .build()
                        )
                ));

        verify(processService).deleteProcess(eq(projectId), eq(userId), eq(processId));
    }

    @Test
    @DisplayName("주차별 프로세스 조회")
    void getWeekProcesses() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        ProcessWeekResDto response = new ProcessWeekResDto(
                LocalDate.of(2026, 1, 19),
                List.of(),
                List.of()
        );

        given(processService.getWeekProcesses(eq(projectId), eq(userId), any()))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/processes/week", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("start_date", "2026-01-19")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("process-week",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process")
                                        .summary("프로세스 목록 조회(주차별)")
                                        .description("프로젝트의 프로세스를 주차 기준으로 조회합니다. start_date 미입력 시 현재 주차 기준.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .queryParameters(
                                                parameterWithName("start_date").optional().description("주 시작일(yyyy-MM-dd)")
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
                                                fieldWithPath("body.start_date").type(STRING).description("주 시작일(yyyy-MM-dd)"),

                                                subsectionWithPath("body.common_lane").type(ARRAY).description("공통 레인 프로세스 카드 목록"),
                                                subsectionWithPath("body.by_field").type(ARRAY).description("분야별(Field) 그룹 목록")
                                        )
                                        .build()
                        )
                ));

        verify(processService).getWeekProcesses(eq(projectId), eq(userId), any());
    }

    @Test
    @DisplayName("파트별 작업 현황 조회")
    void getPartProcesses() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        ProcessPartResDto response = new ProcessPartResDto(
                "TEAM",
                List.of()
        );

        given(processService.getPartProcesses(eq(projectId), eq(userId), any()))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/processes/part", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("field_id", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("process-part",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process")
                                        .summary("프로세스 목록 조회(파트별)")
                                        .description("파트(분야)별 작업 현황을 조회합니다. field_id 미입력(null) 시 팀 탭.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .queryParameters(
                                                parameterWithName("field_id").optional().description("분야 ID (미입력 시 팀 탭)")
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
                                                fieldWithPath("body.lane_key").type(STRING).description("레인 키(팀/파트 구분 키)"),
                                                subsectionWithPath("body.groups").type(ARRAY).description("상태별 그룹 목록")
                                        )
                                        .build()
                        )
                ));

        verify(processService).getPartProcesses(eq(projectId), eq(userId), any());
    }

    @Test
    @DisplayName("프로세스 위치(정렬) 변경")
    void updateProcessOrder() throws Exception {
        long projectId = 1L;
        long processId = 10L;
        long userId = 1L;

        ProcessOrderUpdateReqDto request = new ProcessOrderUpdateReqDto(
                ProcessStatus.IN_PROGRESS,
                List.of(10L, 11L, 12L),
                "TEAM",
                LocalDate.of(2026, 1, 19),
                LocalDate.of(2026, 1, 25)
        );

        ProcessOrderUpdateResDto response = new ProcessOrderUpdateResDto(
                processId,
                ProcessStatus.IN_PROGRESS,
                1,
                LocalDate.of(2026, 1, 19),
                LocalDate.of(2026, 1, 25)
        );

        given(processService.updateProcessOrder(eq(projectId), eq(userId), eq(processId), any(ProcessOrderUpdateReqDto.class)))
                .willReturn(response);

        mockMvc.perform(patch("/api/v1/projects/{projectId}/processes/{processId}/order", projectId, processId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("process-order-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process")
                                        .summary("프로세스 위치(정렬) 변경")
                                        .description("프로세스의 상태 컬럼 내 정렬/이동 정보를 저장합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("processId").description("프로세스 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("status").type(STRING).description("변경할 프로세스 상태"),
                                                fieldWithPath("ordered_process_ids").type(ARRAY).description("해당 상태 컬럼에서의 프로세스 정렬 ID 목록"),
                                                fieldWithPath("lane_key").type(STRING).description("레인 키(TEAM/파트명 등)"),
                                                fieldWithPath("start_date").type(STRING).description("시작일(yyyy-MM-dd)"),
                                                fieldWithPath("dead_line").type(STRING).description("마감일(yyyy-MM-dd)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.process_id").type(NUMBER).description("대상 프로세스 ID"),
                                                fieldWithPath("body.status").type(STRING).description("변경된 상태"),
                                                fieldWithPath("body.status_order").type(NUMBER).description("상태 내 정렬 순서"),
                                                fieldWithPath("body.start_at").type(STRING).description("시작일(yyyy-MM-dd)"),
                                                fieldWithPath("body.dead_line").type(STRING).description("마감일(yyyy-MM-dd)")
                                        )
                                        .build()
                        )
                ));

        verify(processService).updateProcessOrder(eq(projectId), eq(userId), eq(processId), any(ProcessOrderUpdateReqDto.class));
    }

    @Test
    @DisplayName("프로세스 상태 변경")
    void updateProcessStatus() throws Exception {
        long projectId = 1L;
        long processId = 10L;
        long userId = 1L;

        ProcessStatusUpdateReqDto request = new ProcessStatusUpdateReqDto(ProcessStatus.DONE);

        ProcessStatusUpdateResDto response = new ProcessStatusUpdateResDto(
                processId,
                ProcessStatus.DONE,
                LocalDateTime.of(2026, 1, 24, 0, 0, 0)
        );

        given(processService.updateProcessStatus(eq(projectId), eq(userId), eq(processId), any(ProcessStatusUpdateReqDto.class)))
                .willReturn(response);

        mockMvc.perform(patch("/api/v1/projects/{projectId}/processes/{processId}/status", projectId, processId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN) 
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("process-status-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process")
                                        .summary("프로세스 상태 변경")
                                        .description("프로세스의 작업 상태(ProcessStatus)를 변경합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("processId").description("프로세스 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("status").type(STRING).description("변경할 프로세스 상태")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.process_id").type(NUMBER).description("프로세스 ID"),
                                                fieldWithPath("body.status").type(STRING).description("변경된 프로세스 상태"),
                                                fieldWithPath("body.updated_at").type(STRING).description("수정일시(ISO-8601)")
                                        )
                                        .build()
                        )
                ));

        verify(processService).updateProcessStatus(eq(projectId), eq(userId), eq(processId), any(ProcessStatusUpdateReqDto.class));
    }
}