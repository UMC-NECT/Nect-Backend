package com.nect.api.domain.team.process.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.domain.team.process.dto.res.ProcessFileUploadAndAttachResDto;
import com.nect.api.domain.team.process.facade.ProcessAttachmentFacade;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
import com.nect.core.entity.team.enums.FileExt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class ProcessFileUploadControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProcessAttachmentFacade processAttachmentFacade;

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

    private Authentication authWithUser(Long userId) {
        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .userId(userId)
                .roles(List.of("ROLE_MEMBER"))
                .build();

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    @Test
    @DisplayName("프로세스 모달 파일 업로드 + 즉시 첨부")
    void uploadAndAttachFile() throws Exception {
        long projectId = 1L;
        long processId = 10L;
        long userId = 1L;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "dummy pdf bytes".getBytes()
        );

        ProcessFileUploadAndAttachResDto res = new ProcessFileUploadAndAttachResDto(
                123L,
                "sample.pdf",
                "https://cdn.example.com/projects/1/files/123_sample.pdf",
                FileExt.PDF,
                1024L
        );

        given(processAttachmentFacade.uploadAndAttachFile(eq(projectId), eq(userId), eq(processId), any()))
                .willReturn(res);

        mockMvc.perform(
                        multipart("/api/v1/projects/{projectId}/processes/{processId}/files/upload", projectId, processId)
                                .file(file)
                                .with(authentication(authWithUser(userId)))
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("process-file-upload-and-attach",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParts(
                                partWithName("file").description("업로드할 파일(MultipartFile)")
                        ),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Process-Attachment")
                                .summary("프로세스 모달 파일 업로드 + 즉시 첨부")
                                .description("프로세스(카드) 모달에서 파일을 업로드하고, 업로드된 파일을 즉시 해당 프로세스에 첨부합니다.")
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
                                        fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                        fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                        fieldWithPath("body.file_id").type(NUMBER).description("업로드 및 첨부된 파일 ID"),
                                        fieldWithPath("body.file_name").type(STRING).description("원본 파일명"),
                                        fieldWithPath("body.file_url").type(STRING).description("파일 URL"),
                                        fieldWithPath("body.file_type").type(STRING).description("파일 확장자(FileExt)"),
                                        fieldWithPath("body.file_size").type(NUMBER).description("파일 크기(bytes)")
                                )
                                .build()
                        )
                ));
    }
}
