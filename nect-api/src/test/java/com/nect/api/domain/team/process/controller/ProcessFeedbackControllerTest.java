package com.nect.api.domain.team.process.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.process.dto.req.ProcessFeedbackCreateReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessFeedbackUpdateReqDto;
import com.nect.api.domain.team.process.dto.res.FeedbackCreatedByResDto;
import com.nect.api.domain.team.process.dto.res.ProcessFeedbackCreateResDto;
import com.nect.api.domain.team.process.dto.res.ProcessFeedbackDeleteResDto;
import com.nect.api.domain.team.process.dto.res.ProcessFeedbackUpdateResDto;
import com.nect.api.domain.team.process.service.ProcessFeedbackService;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
import com.nect.core.entity.team.process.enums.ProcessFeedbackStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class ProcessFeedbackControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProcessFeedbackService processFeedbackService;

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
    @DisplayName("피드백 생성")
    void createFeedback() throws Exception {
        long projectId = 1L;
        long processId = 10L;
        long userId = 1L;

        ProcessFeedbackCreateReqDto request = new ProcessFeedbackCreateReqDto(
                "피드백 내용입니다."
        );

        FeedbackCreatedByResDto createdBy = new FeedbackCreatedByResDto(
                userId,
                "임시유저",
                List.of()
        );

        ProcessFeedbackCreateResDto response = new ProcessFeedbackCreateResDto(
                100L,
                "피드백 내용입니다.",
                ProcessFeedbackStatus.OPEN,
                createdBy,
                LocalDateTime.of(2026, 1, 25, 10, 0)
        );

        given(processFeedbackService.createFeedback(eq(projectId), eq(userId), eq(processId), any(ProcessFeedbackCreateReqDto.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/projects/{projectId}/processes/{processId}/feedbacks", projectId, processId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("process-feedback-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process-Feedback")
                                        .summary("피드백 생성")
                                        .description("프로세스(카드)에 피드백을 생성합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("processId").description("프로세스 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("content").type(STRING).description("피드백 내용")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.feedback_id").type(NUMBER).description("피드백 ID"),
                                                fieldWithPath("body.content").type(STRING).description("피드백 내용"),
                                                fieldWithPath("body.status").type(STRING).description("피드백 상태"),

                                                fieldWithPath("body.created_by").type(OBJECT).description("작성자 정보"),
                                                fieldWithPath("body.created_by.user_id").type(NUMBER).description("작성자 유저 ID"),
                                                fieldWithPath("body.created_by.user_name").type(STRING).description("작성자 이름"),
                                                fieldWithPath("body.created_by.field_ids").type(ARRAY).description("작성자 분야 ID 목록"),

                                                fieldWithPath("body.created_at").type(STRING).description("생성일시(ISO-8601)")
                                        )
                                        .build()
                        )
                ));

        verify(processFeedbackService).createFeedback(eq(projectId), eq(userId), eq(processId), any(ProcessFeedbackCreateReqDto.class));
    }

    @Test
    @DisplayName("피드백 수정")
    void updateFeedback() throws Exception {
        long projectId = 1L;
        long processId = 10L;
        long feedbackId = 100L;
        long userId = 1L;

        ProcessFeedbackUpdateReqDto request = new ProcessFeedbackUpdateReqDto(
                "수정된 피드백 내용"
        );

        FeedbackCreatedByResDto createdBy = new FeedbackCreatedByResDto(
                userId,
                "임시유저",
                List.of()
        );

        ProcessFeedbackUpdateResDto response = new ProcessFeedbackUpdateResDto(
                feedbackId,
                "수정된 피드백 내용",
                ProcessFeedbackStatus.OPEN,
                createdBy,
                LocalDateTime.of(2026, 1, 25, 10, 0),
                LocalDateTime.of(2026, 1, 26, 11, 0)
        );

        given(processFeedbackService.updateFeedback(eq(projectId), eq(userId), eq(processId), eq(feedbackId), any(ProcessFeedbackUpdateReqDto.class)))
                .willReturn(response);

        mockMvc.perform(patch("/api/v1/projects/{projectId}/processes/{processId}/feedbacks/{feedbackId}", projectId, processId, feedbackId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("process-feedback-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process-Feedback")
                                        .summary("피드백 수정")
                                        .description("피드백 내용을 수정합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("processId").description("프로세스 ID"),
                                                ResourceDocumentation.parameterWithName("feedbackId").description("피드백 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("content").type(STRING).description("수정할 피드백 내용")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.feedback_id").type(NUMBER).description("피드백 ID"),
                                                fieldWithPath("body.content").type(STRING).description("피드백 내용"),
                                                fieldWithPath("body.status").type(STRING).description("피드백 상태"),

                                                fieldWithPath("body.created_by").type(OBJECT).description("작성자 정보"),
                                                fieldWithPath("body.created_by.user_id").type(NUMBER).description("작성자 유저 ID"),
                                                fieldWithPath("body.created_by.user_name").type(STRING).description("작성자 이름"),
                                                fieldWithPath("body.created_by.field_ids").type(ARRAY).description("작성자 분야 ID 목록"),

                                                fieldWithPath("body.created_at").type(STRING).description("생성일시(ISO-8601)"),
                                                fieldWithPath("body.updated_at").type(STRING).description("수정일시(ISO-8601)")
                                        )
                                        .build()
                        )
                ));

        verify(processFeedbackService).updateFeedback(eq(projectId), eq(userId), eq(processId), eq(feedbackId), any(ProcessFeedbackUpdateReqDto.class));
    }

    @Test
    @DisplayName("피드백 삭제")
    void deleteFeedback() throws Exception {
        long projectId = 1L;
        long processId = 10L;
        long feedbackId = 100L;
        long userId = 1L;

        ProcessFeedbackDeleteResDto response = new ProcessFeedbackDeleteResDto(feedbackId);

        given(processFeedbackService.deleteFeedback(eq(projectId), eq(userId), eq(processId), eq(feedbackId)))
                .willReturn(response);

        mockMvc.perform(delete("/api/v1/projects/{projectId}/processes/{processId}/feedbacks/{feedbackId}", projectId, processId, feedbackId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("process-feedback-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process-Feedback")
                                        .summary("피드백 삭제")
                                        .description("피드백을 삭제합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("processId").description("프로세스 ID"),
                                                ResourceDocumentation.parameterWithName("feedbackId").description("피드백 ID")
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
                                                fieldWithPath("body.deleted_feedback_id").type(NUMBER).description("삭제된 피드백 ID")
                                        )
                                        .build()
                        )
                ));

        verify(processFeedbackService).deleteFeedback(eq(projectId), eq(userId), eq(processId), eq(feedbackId));
    }
}
