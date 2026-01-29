package com.nect.api.domain.team.process.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.process.dto.req.*;
import com.nect.api.domain.team.process.dto.res.*;
import com.nect.api.domain.team.process.service.ProcessService;
import com.nect.core.entity.team.enums.FileExt;
import com.nect.core.entity.team.process.enums.ProcessFeedbackStatus;
import com.nect.core.entity.team.process.enums.ProcessStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;

import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
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

    @Test
    @DisplayName("프로세스 생성")
    void createProcess() throws Exception {
        // given
        long projectId = 1L;
        long createdProcessId = 10L;

        ProcessCreateReqDto request = new ProcessCreateReqDto(
                "1주차 미션",                 // process_title
                "프로세스 내용",              // process_content
                ProcessStatus.IN_PROGRESS,    // process_status
                List.of(),                    // assignee_ids  (기존: List.of(1L, 2L))
                List.of(),                    // field_ids     (기존: List.of(101L, 102L))
                LocalDate.of(2026, 1, 19),    // start_date
                LocalDate.of(2026, 1, 25),    // dead_line
                List.of(),                    // mention_user_ids (기존: List.of(3L, 4L))
                List.of(),                    // file_ids         (기존: List.of(1001L, 1002L)
                List.of(),                    // links            (기존: List.of("https://a.com"))
                List.of()                     // task_items       (기존: 2개)
        );

        given(processService.createProcess(anyLong(), any(ProcessCreateReqDto.class)))
                .willReturn(createdProcessId);

        // when, then
        mockMvc.perform(post("/api/v1/projects/{projectId}/processes", projectId)
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
                                        .requestFields(
                                                fieldWithPath("process_title").type(STRING).description("프로세스 제목"),
                                                fieldWithPath("process_content").type(STRING).description("프로세스 내용"),
                                                fieldWithPath("process_status").type(STRING).description("프로세스 상태"),
                                                fieldWithPath("assignee_ids").type(ARRAY).description("담당자 ID 목록"),
                                                fieldWithPath("field_ids").type(ARRAY).description("분야 ID 목록"),
                                                fieldWithPath("start_date").type(STRING).description("시작일(yyyy-MM-dd)"),
                                                fieldWithPath("dead_line").type(STRING).description("마감일(yyyy-MM-dd)"),
                                                fieldWithPath("mention_user_ids").type(ARRAY).description("멘션 유저 ID 목록"),
                                                fieldWithPath("file_ids").type(ARRAY).description("파일 ID 목록"),
                                                fieldWithPath("links").type(ARRAY).description("링크 목록"),
                                                fieldWithPath("task_items").type(ARRAY).description("하위 작업 목록")
//                                                fieldWithPath("task_items[].content").type(STRING).description("하위 작업 내용"),
//                                                fieldWithPath("task_items[].is_done").type(BOOLEAN).description("완료 여부"),
//                                                fieldWithPath("task_items[].sort_order").type(NUMBER).description("정렬 순서")
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
    }

    @Test
    @DisplayName("프로세스 상세 조회")
    void getProcessDetail() throws Exception {
        // given
        long projectId = 1L;
        long processId = 10L;

        ProcessDetailResDto response = new ProcessDetailResDto(
                processId,
                "1주차 미션",
                "프로세스 내용",
                ProcessStatus.IN_PROGRESS,
                LocalDate.of(2026, 1, 19),
                LocalDate.of(2026, 1, 25),
                0,
                List.of(101L, 102L),
                List.of(
                        new AssigneeResDto(1L, "유저1", "https://img.com/1.png"),
                        new AssigneeResDto(2L, "유저2", "https://img.com/2.png")
                ),
                List.of(3L, 4L),
                List.of(
                        new FileResDto(
                                1001L,
                                "spec.pdf",
                                "https://s3.amazonaws.com/nect/spec.pdf",
                                FileExt.PDF,
                                1024L
                        ),
                        new FileResDto(
                                1002L,
                                "image.jpg",
                                "https://s3.amazonaws.com/nect/image.jpg",
                                FileExt.JPG,
                                2048L
                        )
                ),
                List.of(
                        new LinkResDto(1L, "https://a.com"),
                        new LinkResDto(2L, "https://b.com")
                ),
                List.of(
                        new ProcessTaskItemResDto(1L, "세부작업1", false, 0, null),
                        new ProcessTaskItemResDto(2L, "세부작업2", true, 1, LocalDate.of(2026, 1, 20))
                ),
                List.of(
                        new ProcessFeedbackCreateResDto(
                                1L,
                                "피드백 내용",
                                ProcessFeedbackStatus.OPEN,
                                LocalDateTime.of(2026, 1, 24, 0, 0, 0)
                        )
                ),
                LocalDateTime.of(2026, 1, 19, 0, 0, 0),
                LocalDateTime.of(2026, 1, 24, 0, 0, 0),
                null
        );

        given(processService.getProcessDetail(anyLong(), anyLong()))
                .willReturn(response);


        // when, then
        mockMvc.perform(get("/api/v1/projects/{projectId}/processes/{processId}", projectId, processId)
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

                                                fieldWithPath("body.field_ids").type(ARRAY).description("분야 ID 목록"),

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
        // given
        long projectId = 1L;
        long processId = 10L;

        var request = objectMapper.createObjectNode();
        request.put("process_title", "수정된 제목");
        request.put("process_content", "수정된 내용");
        request.put("process_status", "IN_PROGRESS");
        request.put("start_date", "2026-01-20");
        request.put("dead_line", "2026-01-26");

        ProcessBasicUpdateResDto response = new ProcessBasicUpdateResDto(
                processId,
                "수정된 제목",
                "수정된 내용",
                ProcessStatus.IN_PROGRESS,
                LocalDate.of(2026, 1, 20),
                LocalDate.of(2026, 1, 26),
                List.of(),  // field_ids도 문서 예시 맞춰서
                List.of(),
                List.of(),
                LocalDateTime.of(2026, 1, 24, 0, 0, 0)
        );

        given(processService.updateProcessBasic(anyLong(), anyLong(), any(ProcessBasicUpdateReqDto.class)))
                .willReturn(response);

        // when, then
        mockMvc.perform(patch("/api/v1/projects/{projectId}/processes/{processId}", projectId, processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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
                                        .requestFields(
                                                fieldWithPath("process_title").optional().type(STRING).description("프로세스 제목"),
                                                fieldWithPath("process_content").optional().type(STRING).description("프로세스 내용"),
                                                fieldWithPath("process_status").optional().type(STRING).description("프로세스 상태"),
                                                fieldWithPath("assignee_ids").optional().type(ARRAY).description("담당자 ID 목록 (미포함 시 변경 없음, []면 비우기)"),
                                                fieldWithPath("field_ids").optional().type(ARRAY).description("분야 ID 목록 (미포함 시 변경 없음, []면 비우기)"),
                                                fieldWithPath("start_date").optional().type(STRING).description("시작일(yyyy-MM-dd)"),
                                                fieldWithPath("dead_line").optional().type(STRING).description("마감일(yyyy-MM-dd)"),
                                                fieldWithPath("mention_user_ids").optional().type(ARRAY).description("멘션 유저 ID 목록 (미포함 시 변경 없음, []면 비우기)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().description("상세 설명"),

                                                fieldWithPath("body").description("응답 바디"),
                                                fieldWithPath("body.process_id").type(NUMBER).description("프로세스 ID"),
                                                fieldWithPath("body.process_title").type(STRING).description("프로세스 제목"),
                                                fieldWithPath("body.process_content").type(STRING).description("프로세스 내용"),
                                                fieldWithPath("body.process_status").type(STRING).description("프로세스 상태"),
                                                fieldWithPath("body.start_date").type(STRING).description("시작일(yyyy-MM-dd)"),
                                                fieldWithPath("body.dead_line").type(STRING).description("마감일(yyyy-MM-dd)"),
                                                fieldWithPath("body.field_ids").type(ARRAY).description("분야 ID 목록"),
                                                fieldWithPath("body.assignee_ids").type(ARRAY).description("담당자 ID 목록"),
                                                fieldWithPath("body.mention_user_ids").type(ARRAY).description("멘션 유저 ID 목록"),
                                                fieldWithPath("body.updated_at").type(STRING).description("수정일시")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    @DisplayName("프로세스 삭제")
    void deleteProcess() throws Exception {
        long projectId = 1L;
        long processId = 10L;

        mockMvc.perform(delete("/api/v1/projects/{projectId}/processes/{processId}", projectId, processId)
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
                                        .responseFields(
                                                fieldWithPath("status").description("응답 상태"),
                                                fieldWithPath("status.statusCode").description("상태 코드"),
                                                fieldWithPath("status.message").description("메시지"),
                                                fieldWithPath("status.description").optional().description("상세 설명(주로 에러 시)")
                                        )
                                        .build()
                        )
                ));
        verify(processService).deleteProcess(projectId, processId);
    }

    @Test
    @DisplayName("주차별 프로세스 조회")
    void getWeekProcesses() throws Exception {
        // given
        long projectId = 1L;

        ProcessWeekResDto response = new ProcessWeekResDto(
                LocalDate.of(2026, 1, 19),
                List.of(), // common_lane 비워둠
                List.of()  // by_field 비워둠
        );

        given(processService.getWeekProcesses(eq(projectId), any()))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/api/v1/projects/{projectId}/processes/week", projectId)
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
                                                headerWithName(AUTH_HEADER).optional().description("Bearer Access Token")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.start_date").type(STRING).description("주 시작일(yyyy-MM-dd)"),

                                                // 중첩 DTO 내부 필드를 아직 모르니 subsection으로 처리
                                                subsectionWithPath("body.common_lane").type(ARRAY).description("공통 레인 프로세스 카드 목록"),
                                                subsectionWithPath("body.by_field").type(ARRAY).description("분야별(Field) 그룹 목록")
                                        )
                                        .build()
                        )
                ));

    }


    @Test
    @DisplayName("파트별 작업 현황 조회")
    void getPartProcesses() throws Exception {
        // given
        long projectId = 1L;

        ProcessPartResDto response = new ProcessPartResDto(
                1L,
                List.of() // status_groups 비워둠
        );

        given(processService.getPartProcesses(eq(projectId), any()))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/api/v1/projects/{projectId}/processes/part", projectId)
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
                                                headerWithName(AUTH_HEADER).optional().description("Bearer Access Token")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.field_id").optional().type(NUMBER).description("분야 ID (팀 탭이면 null 가능)"),

                                                // 중첩 DTO 내부는 subsection 처리
                                                subsectionWithPath("body.status_groups").type(ARRAY).description("상태별 그룹 목록")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    @DisplayName("프로세스 위치(정렬) 변경")
    void updateProcessOrder() throws Exception {
        // given
        long projectId = 1L;
        long processId = 10L;

        ProcessOrderUpdateReqDto request = new ProcessOrderUpdateReqDto(
                ProcessStatus.IN_PROGRESS,
                List.of(10L, 11L, 12L),
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

        given(processService.updateProcessOrder(eq(projectId), eq(processId), any(ProcessOrderUpdateReqDto.class)))
                .willReturn(response);

        // when, then
        mockMvc.perform(patch("/api/v1/projects/{projectId}/processes/{processId}/order", projectId, processId)
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
                                                headerWithName(AUTH_HEADER).optional().description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("status").type(STRING).description("변경할 프로세스 상태"),
                                                fieldWithPath("ordered_process_ids").type(ARRAY).description("해당 상태 컬럼에서의 프로세스 정렬 ID 목록"),
                                                fieldWithPath("start_date").type(STRING).description("시작일(yyyy-MM-dd)"),
                                                fieldWithPath("dead_line").type(STRING).description("마감일(yyyy-MM-dd)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().description("상세 설명"),


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

    }

    @Test
    @DisplayName("프로세스 상태 변경")
    void updateProcessStatus() throws Exception {
        // given
        long projectId = 1L;
        long processId = 10L;

        ProcessStatusUpdateReqDto request = new ProcessStatusUpdateReqDto(ProcessStatus.DONE);

        ProcessStatusUpdateResDto response = new ProcessStatusUpdateResDto(
                processId,
                ProcessStatus.DONE,
                LocalDateTime.of(2026, 1, 24, 0, 0, 0)
        );

        given(processService.updateProcessStatus(eq(projectId), eq(processId), any(ProcessStatusUpdateReqDto.class)))
                .willReturn(response);

        // when, then
        mockMvc.perform(patch("/api/v1/projects/{projectId}/processes/{processId}/status", projectId, processId)
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
                                                headerWithName(AUTH_HEADER).optional().description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("status").type(STRING).description("변경할 프로세스 상태")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().description("상세 설명"),


                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.process_id").type(NUMBER).description("프로세스 ID"),
                                                fieldWithPath("body.status").type(STRING).description("변경된 프로세스 상태"),
                                                fieldWithPath("body.updated_at").type(STRING).description("수정일시")
                                        )
                                        .build()
                        )
                ));

    }


}