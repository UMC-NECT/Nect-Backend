package com.nect.api.domain.team.process.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.process.dto.req.ProcessLinkCreateReqDto;
import com.nect.api.domain.team.process.dto.res.ProcessLinkCreateResDto;
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
import static org.mockito.ArgumentMatchers.*;
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
class ProcessLinkControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProcessAttachmentService processAttachmentService;

    @Test
    @DisplayName("링크 추가")
    void createLink() throws Exception {
        // given
        long projectId = 1L;
        long processId = 10L;

        ProcessLinkCreateReqDto request = new ProcessLinkCreateReqDto(
                "https://example.com"
        );

        ProcessLinkCreateResDto response = new ProcessLinkCreateResDto(
                100L
        );

        given(processAttachmentService.createLink(eq(projectId), eq(processId), any(ProcessLinkCreateReqDto.class)))
                .willReturn(response);

        // when, then
        mockMvc.perform(post("/projects/{projectId}/processes/{processId}/links", projectId, processId)
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("process-link-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process-Attachment")
                                        .summary("링크 추가")
                                        .description("프로세스(카드)에 링크를 추가합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("processId").description("프로세스 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).optional().description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("url").type(STRING).description("추가할 링크 URL")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.link_id").type(NUMBER).description("링크 ID")
                                        )
                                        .build()
                        )
                ));

        verify(processAttachmentService).createLink(eq(projectId), eq(processId), any(ProcessLinkCreateReqDto.class));
    }

    @Test
    @DisplayName("링크 삭제")
    void deleteLink() throws Exception {
        // given
        long projectId = 1L;
        long processId = 10L;
        long linkId = 100L;

        willDoNothing().given(processAttachmentService).deleteLink(eq(projectId), eq(processId), eq(linkId));

        // when, then
        mockMvc.perform(delete("/projects/{projectId}/processes/{processId}/links/{linkId}", projectId, processId, linkId)
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("process-link-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Process-Attachment")
                                        .summary("링크 삭제")
                                        .description("프로세스(카드)에 등록된 링크를 삭제합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("processId").description("프로세스 ID"),
                                                ResourceDocumentation.parameterWithName("linkId").description("삭제할 링크 ID")
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

        verify(processAttachmentService).deleteLink(eq(projectId), eq(processId), eq(linkId));
    }
}
