package com.nect.api.domain.team.process.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.process.dto.req.*;
import com.nect.api.domain.team.process.dto.res.*;
import com.nect.api.domain.team.process.enums.LaneType;
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

        ProcessCreateResDto response = new ProcessCreateResDto(
                createdProcessId,
                LocalDateTime.of(2026, 1, 19, 0, 0, 0),
                new ProcessCreateResDto.WriterDto(
                        userId,
                        "작성자이름",
                        "작성자닉네임",
                        RoleField.BACKEND,
                        null
                ),
                List.of(
                        new ProcessCreateResDto.AssigneeDto(2L, "담당자이름", "담당자닉", "https://img.com/2.png")
                )
        );

        ProcessCreateReqDto request = new ProcessCreateReqDto(
                "1주차 미션",
                "로그인/회원가입 API 초안 + 문서화",
                ProcessStatus.IN_PROGRESS,

                List.of(2L),
                List.of(RoleField.BACKEND, RoleField.FRONTEND),

                null,
                1,

                LocalDate.of(2026, 1, 19),
                LocalDate.of(2026, 1, 25),

                List.of(),
                List.of(),

                List.of(
                        new ProcessCreateReqDto.ProcessLinkItemReqDto("백엔드 Repo", "https://github.com/nect/nect-backend"),
                        new ProcessCreateReqDto.ProcessLinkItemReqDto("피그마", "https://figma.com/file/xxxxx")
                ),

                List.of(
                        new ProcessTaskItemReqDto("요구사항 정리", false, 1),
                        new ProcessTaskItemReqDto("API 명세 작성", false, 2),
                        new ProcessTaskItemReqDto("컨트롤러/서비스 구현", false, 3)
                )
        );

        given(processService.createProcess(eq(projectId), eq(userId), any(ProcessCreateReqDto.class)))
                .willReturn(response);

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
                                                fieldWithPath("mission_number").optional().type(NUMBER).description("미션 번호(위크미션이면 1..n, 기본형이면 null 가능)"),

                                                fieldWithPath("start_date").type(STRING).description("시작일(yyyy-MM-dd)"),
                                                fieldWithPath("dead_line").type(STRING).description("마감일(yyyy-MM-dd)"),

                                                fieldWithPath("mention_user_ids").type(ARRAY).description("멘션된 유저 ID 목록"),
                                                fieldWithPath("file_ids").type(ARRAY).description("첨부 파일 ID 목록"),

                                                fieldWithPath("links").optional().type(ARRAY).description("첨부 링크 목록"),
                                                fieldWithPath("links[].title").type(STRING).description("링크 제목"),
                                                fieldWithPath("links[].url").type(STRING).description("링크 URL"),

                                                fieldWithPath("task_items").type(ARRAY).description("업무 항목(TaskItem) 목록"),
                                                fieldWithPath("task_items[].content").type(STRING).description("업무 항목 내용"),
                                                fieldWithPath("task_items[].is_done").type(BOOLEAN).description("완료 여부"),
                                                fieldWithPath("task_items[].sort_order").type(NUMBER).description("정렬 순서")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").description("응답 상태"),
                                                fieldWithPath("status.statusCode").description("상태 코드"),
                                                fieldWithPath("status.message").description("메시지"),
                                                fieldWithPath("status.description").optional().description("상세 설명(주로 에러 시)"),

                                                fieldWithPath("body").description("응답 바디"),
                                                fieldWithPath("body.process_id").type(NUMBER).description("생성된 프로세스 ID"),
                                                fieldWithPath("body.created_at").type(STRING).description("생성일시(ISO-8601)"),

                                                fieldWithPath("body.writer").type(OBJECT).description("작성자 정보"),
                                                fieldWithPath("body.writer.user_id").type(NUMBER).description("작성자 유저 ID"),
                                                fieldWithPath("body.writer.name").type(STRING).description("작성자 이름"),
                                                fieldWithPath("body.writer.nickname").type(STRING).description("작성자 닉네임"),
                                                fieldWithPath("body.writer.role_field").type(STRING).description("작성자 역할 분야(RoleField)"),
                                                fieldWithPath("body.writer.custom_field_name").optional().type(STRING).description("작성자 커스텀 분야명(null 가능)"),

                                                fieldWithPath("body.assignees").type(ARRAY).description("담당자 정보 목록"),
                                                fieldWithPath("body.assignees[].user_id").type(NUMBER).description("담당자 유저 ID"),
                                                fieldWithPath("body.assignees[].name").type(STRING).description("담당자 이름"),
                                                fieldWithPath("body.assignees[].nickname").type(STRING).description("담당자 닉네임"),
                                                fieldWithPath("body.assignees[].profile_image_url").type(STRING).description("담당자 프로필 이미지 URL")
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
                "패트",
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

                List.of(),                // role_fields
                List.of("디자인"),        // custom_fields

                List.of(
                        new AssigneeResDto(1L, "유저1", "유저1닉", "https://img.com/1.png"),
                        new AssigneeResDto(2L, "유저2", "유저2닉", "https://img.com/2.png")
                ),
                List.of(3L, 4L),

                List.of(
                        new ProcessTaskItemResDto(1L, "세부작업1", false, 0, null),
                        new ProcessTaskItemResDto(2L, "세부작업2", true, 1, LocalDate.of(2026, 1, 20))
                ),

                feedbacks,

                // attachments (FILE + LINK 통합)
                List.of(
                        new ProcessDetailResDto.AttachmentDto(
                                com.nect.api.domain.team.process.enums.AttachmentType.FILE,
                                1001L,
                                LocalDateTime.of(2026, 1, 23, 12, 0),
                                null, null,
                                "spec.pdf",
                                "https://s3.amazonaws.com/nect/spec.pdf",
                                FileExt.PDF,
                                1024L
                        ),
                        new ProcessDetailResDto.AttachmentDto(
                                com.nect.api.domain.team.process.enums.AttachmentType.LINK,
                                2001L,
                                LocalDateTime.of(2026, 1, 22, 9, 0),
                                "Backend Repo",
                                "https://github.com/nect/nect-backend",
                                null, null, null, null
                        )
                ),

                LocalDateTime.of(2026, 1, 19, 0, 0, 0),
                LocalDateTime.of(2026, 1, 24, 0, 0, 0),
                null
        );

        given(processService.getProcessDetail(eq(projectId), eq(userId), eq(processId), nullable(String.class)))
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
                                        .queryParameters(
                                               parameterWithName("lane_key").optional().description("레인 키 (팀 탭이면 미입력/null, ROLE:XXX, CUSTOM:이름)")
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
                                                fieldWithPath("body.assignees[].nickname").type(STRING).description("담당자 닉네임"),
                                                fieldWithPath("body.assignees[].user_image").type(STRING).description("담당자 이미지 URL"),

                                                fieldWithPath("body.mention_user_ids").type(ARRAY).description("멘션 유저 ID 목록"),

                                                fieldWithPath("body.attachments").type(ARRAY).description("첨부 목록(파일/링크 통합)"),

                                                fieldWithPath("body.attachments[].type").type(STRING).description("첨부 타입(FILE/LINK)"),
                                                fieldWithPath("body.attachments[].id").type(NUMBER).description("첨부 식별자(파일이면 file_id, 링크면 link_id)"),
                                                fieldWithPath("body.attachments[].created_at").type(STRING).description("첨부 생성/첨부 시각"),

                                                fieldWithPath("body.attachments[].title").optional().type(STRING).description("링크 제목(LINK 전용)"),
                                                fieldWithPath("body.attachments[].url").optional().type(STRING).description("링크 URL(LINK 전용)"),

                                                fieldWithPath("body.attachments[].file_name").optional().type(STRING).description("파일명(FILE 전용)"),
                                                fieldWithPath("body.attachments[].file_url").optional().type(STRING).description("파일 URL(FILE 전용)"),
                                                fieldWithPath("body.attachments[].file_type").optional().type(STRING).description("파일 확장자(FileExt, FILE 전용)"),
                                                fieldWithPath("body.attachments[].file_size").optional().type(NUMBER).description("파일 크기(byte, FILE 전용)"),

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
                                                fieldWithPath("body.feedbacks[].created_by.nickname").type(STRING).description("작성자 닉네임"),
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

                1,

                List.of(1L, 2L),
                List.of(3L, 4L)
        );

        List<AssigneeResDto> assignees = List.of(
                new AssigneeResDto(1L, "유저1", "유저1닉", "https://img.com/1.png"),
                new AssigneeResDto(2L, "유저2", "유저2닉", "https://img.com/2.png")
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
                assignees,

                List.of(3L, 4L),

                LocalDateTime.of(2026, 1, 24, 0, 0, 0),

                new ProcessBasicUpdateResDto.WriterDto(
                        1L,
                        "작성자이름",
                        "작성자닉네임",
                        RoleField.BACKEND,
                        null
                )
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
                                                fieldWithPath("custom_fields").optional().type(ARRAY).description("커스텀 분야명 목록(CUSTOM 선택 시)"),
                                                fieldWithPath("mission_number").optional().type(NUMBER).description("미션 번호(미포함 시 변경 없음, null 가능)")
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
                                                fieldWithPath("body.assignees").type(ARRAY).description("담당자 정보 목록"),
                                                fieldWithPath("body.assignees[].user_id").type(NUMBER).description("담당자 유저 ID"),
                                                fieldWithPath("body.assignees[].user_name").type(STRING).description("담당자 이름"),
                                                fieldWithPath("body.assignees[].nickname").type(STRING).description("담당자 닉네임"),
                                                fieldWithPath("body.assignees[].user_image").type(STRING).description("담당자 이미지 URL"),

                                                fieldWithPath("body.mention_user_ids").type(ARRAY).description("멘션 유저 ID 목록"),
                                                fieldWithPath("body.updated_at").type(STRING).description("수정일시(ISO-8601)"),

                                                fieldWithPath("body.writer").type(OBJECT).description("작성자 정보"),
                                                fieldWithPath("body.writer.user_id").type(NUMBER).description("작성자 유저 ID"),
                                                fieldWithPath("body.writer.name").type(STRING).description("작성자 이름"),
                                                fieldWithPath("body.writer.nickname").type(STRING).description("작성자 닉네임"),
                                                fieldWithPath("body.writer.role_field").type(STRING).description("작성자 역할(RoleField)"),
                                                fieldWithPath("body.writer.custom_field_name").optional().type(STRING).description("작성자 커스텀 역할명(null 가능)")
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
    @DisplayName("주차별 프로세스 조회(weeks 지원)")
    void getWeekProcesses() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        ProcessWeekResDto w1 = new ProcessWeekResDto(
                LocalDate.of(2026, 1, 19),
                List.of(),
                List.of()
        );

        ProcessWeekResDto w2 = new ProcessWeekResDto(
                LocalDate.of(2026, 1, 26),
                List.of(),
                List.of()
        );

        ProcessWeeksResDto response = new ProcessWeeksResDto(List.of(w1, w2));

        given(processService.getWeekProcesses(eq(projectId), eq(userId), any(LocalDate.class), eq(2)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/processes/week", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("start_date", "2026-01-19")
                        .param("weeks", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("process-week",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process")
                                        .summary("프로세스 목록 조회(주차별/범위)")
                                        .description("프로젝트의 프로세스를 주차 기준으로 조회합니다. weeks를 통해 여러 주차를 한번에 조회할 수 있습니다. start_date 미입력 시 현재 주차 기준.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .queryParameters(
                                                parameterWithName("start_date").optional().description("기준 주 시작일(yyyy-MM-dd)"),
                                                parameterWithName("weeks").optional().description("조회할 주 개수(기본 1, 최대 12)")
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
                                                fieldWithPath("body.weeks").type(ARRAY).description("주차별 결과 목록"),

                                                fieldWithPath("body.weeks[].start_date").type(STRING).description("주 시작일(yyyy-MM-dd)"),
                                                subsectionWithPath("body.weeks[].common_lane").type(ARRAY).description("공통 레인 프로세스 카드 목록"),
                                                subsectionWithPath("body.weeks[].by_field").type(ARRAY).description("분야별(Field) 그룹 목록")
                                        )
                                        .build()
                        )
                ));

        verify(processService).getWeekProcesses(eq(projectId), eq(userId), any(LocalDate.class), eq(2));
    }

    @Test
    @DisplayName("파트별 작업 현황 조회")
    void getPartProcesses() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        // assignee 샘플
        AssigneeResDto a1 = new AssigneeResDto(1L, "유저1", "유저1닉", "https://img.com/1.png");
        AssigneeResDto a2 = new AssigneeResDto(2L, "유저2", "유저2닉", "https://img.com/2.png");

        // IN_PROGRESS 카드 2개
        ProcessCardResDto p10 = new ProcessCardResDto(
                10L,
                ProcessStatus.IN_PROGRESS,
                "백엔드 API 초안 작성",
                1,
                3,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 10),
                5,
                List.of(RoleField.BACKEND),
                List.of("AI"),
                1,
                List.of(a1, a2) // assignee
        );

        ProcessCardResDto p12 = new ProcessCardResDto(
                12L,
                ProcessStatus.IN_PROGRESS,
                "CI 파이프라인 점검",
                0,
                2,
                LocalDate.of(2026, 2, 3),
                LocalDate.of(2026, 2, 8),
                3,
                List.of(RoleField.BACKEND, RoleField.FRONTEND),
                List.of("DevOps"),
                null,
                List.of(a2)
        );



        ProcessStatusGroupResDto inProgressGroup = new ProcessStatusGroupResDto(
                ProcessStatus.IN_PROGRESS,
                2,
                List.of(p10, p12)
        );

        // PLANNING 카드 1개
        ProcessCardResDto p20 = new ProcessCardResDto(
                20L,
                ProcessStatus.PLANNING,
                "DB 스키마 점검",
                0,
                0,
                null,
                null,
                null,
                List.of(RoleField.BACKEND),
                List.of(),
                null,
                List.of(a1)
        );

        ProcessStatusGroupResDto planningGroup = new ProcessStatusGroupResDto(
                ProcessStatus.PLANNING,
                1,
                List.of(p20)
        );

        // DONE 카드 1개
        ProcessCardResDto p30 = new ProcessCardResDto(
                30L,
                ProcessStatus.DONE,
                "로그인 API 테스트 완료",
                3,
                3,
                LocalDate.of(2026, 1, 20),
                LocalDate.of(2026, 1, 24),
                0,
                List.of(RoleField.BACKEND),
                List.of("Auth"),
                1,
                List.of(a1, a2)
        );

        ProcessStatusGroupResDto doneGroup = new ProcessStatusGroupResDto(
                ProcessStatus.DONE,
                1,
                List.of(p30)
        );

        // BACKLOG 카드 1개
        ProcessCardResDto p40 = new ProcessCardResDto(
                40L,
                ProcessStatus.BACKLOG,
                "리팩토링 후보 정리",
                0,
                1,
                null,
                null,
                null,
                List.of(RoleField.BACKEND),
                List.of("TechDebt"),
                1,
                List.of(a2)
        );

        ProcessStatusGroupResDto backlogGroup = new ProcessStatusGroupResDto(
                ProcessStatus.BACKLOG,
                1,
                List.of(p40)
        );

        ProcessPartResDto response = new ProcessPartResDto(
                "ROLE:BACKEND",
                List.of(planningGroup, inProgressGroup, doneGroup, backlogGroup)
        );

        given(processService.getPartProcesses(eq(projectId), eq(userId), eq("ROLE:BACKEND")))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/processes/part", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("lane_key", "ROLE:BACKEND")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("process-part",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process")
                                        .summary("프로세스 목록 조회(파트별)")
                                        .description("파트(레인)별 작업 현황을 조회합니다. lane_key 미입력(null) 시 팀 탭(전체), ROLE:XXX / CUSTOM:이름 지원")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .queryParameters(
                                                parameterWithName("lane_key").optional()
                                                        .description("레인 키 (미입력/null=TEAM, ROLE:XXX, CUSTOM:이름)")
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
                                                fieldWithPath("body.lane_key").optional().type(STRING).description("레인 키(TEAM이면 null 가능)"),

                                                fieldWithPath("body.groups").type(ARRAY).description("상태별 그룹 목록"),
                                                fieldWithPath("body.groups[].status").type(STRING).description("상태(PLANNING/IN_PROGRESS/DONE/BACKLOG)"),
                                                fieldWithPath("body.groups[].count").type(NUMBER).description("해당 상태의 프로세스 개수"),
                                                fieldWithPath("body.groups[].processes").type(ARRAY).description("프로세스 카드 목록"),

                                                fieldWithPath("body.groups[].processes[].process_id").type(NUMBER).description("프로세스 ID"),
                                                fieldWithPath("body.groups[].processes[].process_status").type(STRING).description("프로세스 상태"),
                                                fieldWithPath("body.groups[].processes[].title").type(STRING).description("프로세스 제목"),
                                                fieldWithPath("body.groups[].processes[].complete_check_list").type(NUMBER).description("완료 체크리스트 수"),
                                                fieldWithPath("body.groups[].processes[].whole_check_list").type(NUMBER).description("전체 체크리스트 수"),
                                                fieldWithPath("body.groups[].processes[].start_date").optional().type(STRING).description("시작일(yyyy-MM-dd, null 가능)"),
                                                fieldWithPath("body.groups[].processes[].dead_line").optional().type(STRING).description("마감일(yyyy-MM-dd, null 가능)"),
                                                fieldWithPath("body.groups[].processes[].left_day").optional().type(NUMBER).description("남은 일수(null 가능)"),

                                                fieldWithPath("body.groups[].processes[].role_fields").type(ARRAY).description("RoleField 목록"),
                                                fieldWithPath("body.groups[].processes[].custom_fields").type(ARRAY).description("커스텀 필드명 목록"),

                                                fieldWithPath("body.groups[].processes[].mission_number").optional().type(VARIES).description("위크미션 번호(미션 프로세스면 1..n, 일반 프로세스면 null)"),

                                                fieldWithPath("body.groups[].processes[].assignee").type(ARRAY).description("담당자 목록"),

                                                fieldWithPath("body.groups[].processes[].assignee").type(ARRAY).description("담당자 목록"),
                                                fieldWithPath("body.groups[].processes[].assignee[].user_id").type(NUMBER).description("담당자 유저 ID"),
                                                fieldWithPath("body.groups[].processes[].assignee[].user_name").type(STRING).description("담당자 이름"),
                                                fieldWithPath("body.groups[].processes[].assignee[].nickname").type(STRING).description("담당자 닉네임"),
                                                fieldWithPath("body.groups[].processes[].assignee[].user_image").type(STRING).description("담당자 이미지 URL")
                                        )
                                        .build()
                        )
                ));

        verify(processService).getPartProcesses(eq(projectId), eq(userId), eq("ROLE:BACKEND"));
    }


    @Test
    @DisplayName("프로세스 위치(순서) 변경")
    void updateProcessOrder() throws Exception {
        long projectId = 1L;
        long processId = 2L;
        long userId = 1L;

        ProcessOrderUpdateReqDto request = new ProcessOrderUpdateReqDto(
                ProcessStatus.IN_PROGRESS,
                List.of(10L, 2L, 12L),
                "ROLE:BACKEND",
                1,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 10)
        );

        ProcessOrderUpdateResDto response = new ProcessOrderUpdateResDto(
                processId,
                ProcessStatus.IN_PROGRESS,
                1,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 10)
        );

        given(processService.updateProcessOrder(eq(projectId), eq(userId), eq(processId), any(ProcessOrderUpdateReqDto.class)))
                .willReturn(response);

        mockMvc.perform(
                        patch("/api/v1/projects/{projectId}/processes/{processId}/order", projectId, processId)
                                .with(mockUser(userId))
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andDo(document("process-order-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process")
                                        .summary("프로세스 위치(순서) 변경")
                                        .description("특정 레인(lane_key) + 상태(status) 내에서 프로세스 카드들의 순서 및 기간(start/deadLine)을 변경합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("processId").description("프로세스 ID(앵커)")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("status").optional().type(STRING).description("대상 프로세스 상태(컬럼)"),
                                                fieldWithPath("ordered_process_ids").optional().type(ARRAY).description("정렬 순서대로 나열한 프로세스 ID 목록"),
                                                fieldWithPath("lane_key").type(STRING).description("레인 키(TEAM, ROLE:XXX, CUSTOM:이름)"),
                                                fieldWithPath("start_date").optional().type(STRING).description("시작일(yyyy-MM-dd, null 가능)"),
                                                fieldWithPath("dead_line").optional().type(STRING).description("마감일(yyyy-MM-dd, null 가능)"),
                                                fieldWithPath("mission_number").optional().type(NUMBER).description("미션 번호(위크미션이면 사용, 기본형이면 null 가능)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.process_id").type(NUMBER).description("프로세스 ID"),
                                                fieldWithPath("body.status").type(STRING).description("프로세스 상태"),
                                                fieldWithPath("body.status_order").type(NUMBER).description("해당 status 내 정렬 순서"),
                                                fieldWithPath("body.groups[].processes[].mission_number").optional().type(NUMBER).description("미션 번호(null 가능)"),
                                                fieldWithPath("body.start_at").optional().type(STRING).description("시작일(yyyy-MM-dd, null 가능)"),
                                                fieldWithPath("body.dead_line").optional().type(STRING).description("마감일(yyyy-MM-dd, null 가능)")
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

    @Test
    @DisplayName("파트별 작업 진행률 요약 조회")
    void getPartProgressSummary() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        ProcessProgressSummaryResDto response = new ProcessProgressSummaryResDto(
                List.of(
                        new LaneProgressResDto(
                                "ROLE:PM",
                                LaneType.ROLE,
                                "PM",
                                6L, 3L, 2L, 11L,
                                55, 27, 18
                        ),
                        new LaneProgressResDto(
                                "ROLE:BACKEND",
                                LaneType.ROLE,
                                "BACKEND",
                                1L, 4L, 7L, 12L,
                                8, 33, 59
                        ),
                        new LaneProgressResDto(
                                "CUSTOM:영상편집",
                                LaneType.CUSTOM,
                                "영상편집",
                                2L, 2L, 0L, 4L,
                                50, 50, 0
                        )
                )
        );

        given(processService.getPartProgressSummary(eq(projectId), eq(userId)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/processes/parts/progress-summary", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("process-part-progress-summary",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process")
                                        .summary("파트별 작업 진행률 요약 조회")
                                        .description("프로젝트의 ROLE/CUSTOM 레인별 프로세스 상태 진행률(PLANNING/IN_PROGRESS/DONE)을 요약 조회합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID")
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
                                                fieldWithPath("body.lanes").type(ARRAY).description("레인별 진행률 목록"),

                                                fieldWithPath("body.lanes[].lane_key").type(STRING).description("레인 키(ROLE:XXX / CUSTOM:이름)"),
                                                fieldWithPath("body.lanes[].lane_type").type(STRING).description("레인 타입(ROLE/CUSTOM)"),
                                                fieldWithPath("body.lanes[].lane_name").type(STRING).description("레인 이름(ROLE enum name 또는 CUSTOM 이름)"),

                                                fieldWithPath("body.lanes[].planning").type(NUMBER).description("계획(PLANNING) 프로세스 개수"),
                                                fieldWithPath("body.lanes[].in_progress").type(NUMBER).description("진행 중(IN_PROGRESS) 프로세스 개수"),
                                                fieldWithPath("body.lanes[].done").type(NUMBER).description("완료(DONE) 프로세스 개수"),
                                                fieldWithPath("body.lanes[].total").type(NUMBER).description("전체(PLANNING+IN_PROGRESS+DONE)"),

                                                fieldWithPath("body.lanes[].planning_rate").type(NUMBER).description("계획 비율(0~100)"),
                                                fieldWithPath("body.lanes[].in_progress_rate").type(NUMBER).description("진행중 비율(0~100)"),
                                                fieldWithPath("body.lanes[].done_rate").type(NUMBER).description("완료 비율(0~100, 합 100 보정)")
                                        )
                                        .build()
                        )
                ));

        verify(processService).getPartProgressSummary(eq(projectId), eq(userId));
    }

}