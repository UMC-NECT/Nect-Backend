package com.nect.api.domain.team.file.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.file.dto.res.FileDownloadUrlResDto;
import com.nect.api.domain.team.file.dto.res.FileUploadResDto;
import com.nect.api.domain.team.file.service.FileService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class FileControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

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
    @DisplayName("프로젝트 파일 업로드")
    void uploadFile() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "dummy pdf bytes".getBytes()
        );

        FileUploadResDto res = new FileUploadResDto(
                123L,
                "sample.pdf",
                "https://cdn.example.com/projects/1/files/123_sample.pdf",
                FileExt.PDF,
                1024L,
                "https://cdn.example.com/projects/1/files/123_sample.pdf?downloadToken=abc"
        );

        given(fileService.upload(eq(projectId), eq(userId), any()))
                .willReturn(res);

        mockMvc.perform(
                        multipart("/api/v1/projects/{projectId}/files/upload", projectId)
                                .file(file)
                                .with(authentication(authWithUser(userId)))
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("file-upload",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParts(
                                partWithName("file").description("업로드할 파일(MultipartFile)")
                        ),
                        resource(ResourceSnippetParameters.builder()
                                .tag("File")
                                .summary("프로젝트 파일 업로드")
                                .description("프로젝트 파일을 업로드합니다. 업로드 성공 시 file_id/file_url 등을 반환합니다.")
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
                                        fieldWithPath("body.file_id").type(NUMBER).description("업로드된 파일 ID"),
                                        fieldWithPath("body.file_name").type(STRING).description("원본 파일명"),
                                        fieldWithPath("body.file_url").type(STRING).description("파일 URL"),
                                        fieldWithPath("body.file_type").type(STRING).description("파일 확장자(FileExt)"),
                                        fieldWithPath("body.file_size").type(NUMBER).description("파일 크기(bytes)"),
                                        fieldWithPath("body.download_url").type(STRING).description("다운로드 URL")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("프로젝트 파일 다운로드(리다이렉트)")
    void downloadRedirect() throws Exception {
        long projectId = 1L;
        long userId = 1L;
        long documentId = 100L;

        String redirectUrl = "https://cdn.example.com/projects/1/files/100/download?token=abc";

        FileDownloadUrlResDto res = new FileDownloadUrlResDto(
                documentId,
                "spec.pdf",
                FileExt.PDF,
                1024L,
                redirectUrl
        );

        given(fileService.getDownloadUrl(eq(projectId), eq(userId), eq(documentId)))
                .willReturn(res);

        mockMvc.perform(
                        get("/api/v1/projects/{projectId}/files/{documentId}/download", projectId, documentId)
                                .with(authentication(authWithUser(userId)))
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                )
                .andExpect(status().isFound()) // 302
                .andExpect(header().string("Location", redirectUrl))
                .andDo(document("file-download-redirect",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("File")
                                .summary("프로젝트 파일 다운로드(리다이렉트)")
                                .description("파일 다운로드 URL을 조회한 뒤 302(FOUND)로 Location 헤더에 담아 리다이렉트합니다.")
                                .pathParameters(
                                        ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                        ResourceDocumentation.parameterWithName("documentId").description("파일(문서) ID")
                                )
                                .requestHeaders(
                                        headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                )
                                .responseHeaders(
                                        headerWithName("Location").description("다운로드 리다이렉트 URL")
                                )
                                .build()
                        )
                ));

        verify(fileService).getDownloadUrl(eq(projectId), eq(userId), eq(documentId));
    }
}
