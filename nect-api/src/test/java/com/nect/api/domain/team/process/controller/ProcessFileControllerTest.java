package com.nect.api.domain.team.process.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.process.dto.req.ProcessFileAttachReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessFileAttachResDto;
import com.nect.api.domain.team.process.service.ProcessAttachmentService;
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

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
@Transactional
class ProcessFileControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProcessAttachmentService processAttachmentService;

    @Test
    @DisplayName("프로세스 파일 첨부")
    void attachFile() throws Exception {
        // given
        long projectId = 1L;
        long processId = 10L;

        ProcessFileAttachReqDto request = new ProcessFileAttachReqDto(999L);
        ProcessFileAttachResDto response = new ProcessFileAttachResDto(999L);

        given(processAttachmentService.attachFile(eq(projectId), eq(processId), any(ProcessFileAttachReqDto.class)))
                .willReturn(response);

        // when, then
        mockMvc.perform(post("/projects/{projectId}/processes/{processId}/files", projectId, processId)
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("process-file-attach",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process-Attachment")
                                        .summary("프로세스 파일 첨부")
                                        .description("프로세스(카드)에 파일을 첨부합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("processId").description("프로세스 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).optional().description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("file_id").type(NUMBER).description("첨부할 파일 ID")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.file_id").type(NUMBER).description("첨부된 파일 ID")
                                        )
                                        .build()
                        )
                ));

        verify(processAttachmentService).attachFile(eq(projectId), eq(processId), any(ProcessFileAttachReqDto.class));
    }

    @Test
    @DisplayName("프로세스 파일 첨부 해제")
    void detachFile() throws Exception {
        // given
        long projectId = 1L;
        long processId = 10L;
        long fileId = 999L;

        willDoNothing().given(processAttachmentService).detachFile(eq(projectId), eq(processId), eq(fileId));

        // when, then
        mockMvc.perform(delete("/projects/{projectId}/processes/{processId}/files/{fileId}", projectId, processId, fileId)
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("process-file-detach",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process-Attachment")
                                        .summary("프로세스 파일 첨부 해제")
                                        .description("프로세스(카드)에 첨부된 파일을 해제합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("processId").description("프로세스 ID"),
                                                ResourceDocumentation.parameterWithName("fileId").description("첨부 해제할 파일 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).optional().description("Bearer Access Token")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().description("상세 설명"),

                                                fieldWithPath("body").type(NULL).optional().description("응답 바디(없음)")
                                        )
                                        .build()
                        )
                ));

        verify(processAttachmentService).detachFile(eq(projectId), eq(processId), eq(fileId));
    }
}
