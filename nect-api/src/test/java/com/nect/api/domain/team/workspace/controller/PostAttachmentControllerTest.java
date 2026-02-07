package com.nect.api.domain.team.workspace.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.workspace.dto.req.PostLinkCreateReqDto;
import com.nect.api.domain.team.workspace.dto.res.PostAttachmentResDto;
import com.nect.api.domain.team.workspace.facade.PostAttachmentFacade;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
import com.nect.core.entity.team.enums.DocumentType;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
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
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class PostAttachmentControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private PostAttachmentFacade postAttachmentFacade;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private UserDetailsServiceImpl userDetailsService;
    @MockitoBean private TokenBlacklistService tokenBlacklistService;

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
    @DisplayName("게시글 첨부 - 파일 업로드 + 첨부")
    void uploadAndAttachFile() throws Exception {
        long projectId = 1L;
        long postId = 100L;
        long userId = 1L;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "dummy".getBytes()
        );

        PostAttachmentResDto response = new PostAttachmentResDto(
                55L,
                DocumentType.FILE,
                "test.pdf",
                null,
                "test.pdf",
                FileExt.PDF,
                1234L,
                "https://download.example.com/test.pdf"
        );

        given(postAttachmentFacade.uploadAndAttachFile(eq(projectId), eq(userId), eq(postId), any()))
                .willReturn(response);

        mockMvc.perform(multipart("/api/v1/projects/{projectId}/boards/posts/{postId}/attachments/files", projectId, postId)
                        .file(file)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("post-attachment-upload-file",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("PostAttachment")
                                        .summary("게시글 파일 업로드 + 첨부")
                                        .description("게시글에 파일을 업로드하고 즉시 첨부합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("postId").description("게시글 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        // multipart라 requestFields 대신 part를 문서화하고 싶으면 requestParts를 쓰는 게 더 정확함.
                                        // (epages apispec에서 requestParts 지원이 애매하면 생략해도 됨)
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.document_id").type(NUMBER).description("문서 ID(SharedDocument ID)"),
                                                fieldWithPath("body.document_type").type(STRING).description("문서 타입(FILE|LINK)"),
                                                fieldWithPath("body.title").type(STRING).description("문서 제목"),

                                                fieldWithPath("body.link_url").optional().type(STRING).description("링크 URL (document_type=LINK일 때)"),

                                                fieldWithPath("body.file_name").optional().type(STRING).description("파일명 (document_type=FILE일 때)"),
                                                fieldWithPath("body.file_ext").optional().type(STRING).description("파일 확장자 (document_type=FILE일 때)"),
                                                fieldWithPath("body.file_size").optional().type(NUMBER).description("파일 크기(byte) (document_type=FILE일 때)"),
                                                fieldWithPath("body.download_url").optional().type(STRING).description("다운로드 URL (document_type=FILE일 때)")
                                        )
                                        .build()
                        )
                ));

        verify(postAttachmentFacade).uploadAndAttachFile(eq(projectId), eq(userId), eq(postId), any());
    }

    @Test
    @DisplayName("게시글 첨부 - 링크 생성 + 첨부")
    void createAndAttachLink() throws Exception {
        long projectId = 1L;
        long postId = 100L;
        long userId = 1L;

        PostLinkCreateReqDto request = new PostLinkCreateReqDto(
                "피그마 링크",
                "https://figma.com/file/xxx"
        );

        PostAttachmentResDto response = new PostAttachmentResDto(
                77L,
                DocumentType.LINK,
                "피그마 링크",
                "https://figma.com/file/xxx",
                null,
                null,
                0L,
                null
        );

        given(postAttachmentFacade.createAndAttachLink(eq(projectId), eq(userId), eq(postId), any(PostLinkCreateReqDto.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/projects/{projectId}/boards/posts/{postId}/attachments/links", projectId, postId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("post-attachment-create-link",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("PostAttachment")
                                        .summary("게시글 링크 생성 + 첨부")
                                        .description("게시글에 링크(SharedDocument)를 생성하고 즉시 첨부합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("postId").description("게시글 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("title").type(STRING).description("링크 제목"),
                                                fieldWithPath("link_url").type(STRING).description("링크 URL")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.document_id").type(NUMBER).description("문서 ID(SharedDocument ID)"),
                                                fieldWithPath("body.document_type").type(STRING).description("문서 타입(FILE|LINK)"),
                                                fieldWithPath("body.title").type(STRING).description("문서 제목"),

                                                fieldWithPath("body.link_url").type(STRING).description("링크 URL"),

                                                fieldWithPath("body.file_name").optional().type(STRING).description("파일명 (document_type=FILE일 때)"),
                                                fieldWithPath("body.file_ext").optional().type(STRING).description("파일 확장자 (document_type=FILE일 때)"),
                                                fieldWithPath("body.file_size").optional().type(NUMBER).description("파일 크기(byte) (document_type=FILE일 때)"),
                                                fieldWithPath("body.download_url").optional().type(STRING).description("다운로드 URL (document_type=FILE일 때)")
                                        )
                                        .build()
                        )
                ));

        verify(postAttachmentFacade).createAndAttachLink(eq(projectId), eq(userId), eq(postId), any(PostLinkCreateReqDto.class));
    }

    @Test
    @DisplayName("게시글 첨부 - 첨부 해제(파일/링크 공통)")
    void detach() throws Exception {
        long projectId = 1L;
        long postId = 100L;
        long documentId = 55L;
        long userId = 1L;

        willDoNothing().given(postAttachmentFacade).detach(eq(projectId), eq(userId), eq(postId), eq(documentId));

        mockMvc.perform(delete("/api/v1/projects/{projectId}/boards/posts/{postId}/attachments/{documentId}", projectId, postId, documentId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("post-attachment-detach",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("PostAttachment")
                                        .summary("게시글 첨부 해제")
                                        .description("게시글에 첨부된 문서(파일/링크)를 해제합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("postId").description("게시글 ID"),
                                                ResourceDocumentation.parameterWithName("documentId").description("문서 ID(SharedDocument ID)")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),
                                                fieldWithPath("body").type(NULL).optional().description("응답 바디(없음)")
                                        )
                                        .build()
                        )
                ));

        verify(postAttachmentFacade).detach(eq(projectId), eq(userId), eq(postId), eq(documentId));
    }
}
