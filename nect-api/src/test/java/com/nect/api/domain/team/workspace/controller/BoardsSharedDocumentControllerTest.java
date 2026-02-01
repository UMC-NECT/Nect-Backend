package com.nect.api.domain.team.workspace.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.workspace.dto.res.SharedDocumentsPreviewResDto;
import com.nect.api.domain.team.workspace.facade.BoardsSharedDocumentFacade;
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
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class BoardsSharedDocumentControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BoardsSharedDocumentFacade facade;

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
    @DisplayName("공유 문서함 프리뷰 조회")
    void getSharedDocumentsPreview() throws Exception {
        long projectId = 1L;
        long userId = 1L;
        int limit = 4;

        SharedDocumentsPreviewResDto.UploaderDto uploader =
                new SharedDocumentsPreviewResDto.UploaderDto(
                        10L,
                        "홍길동",
                        "길동",
                        null
                );

        List<SharedDocumentsPreviewResDto.DocumentDto> docs = List.of(
                new SharedDocumentsPreviewResDto.DocumentDto(
                        100L,
                        true,
                        "API 명세서",
                        "api-spec.pdf",
                        FileExt.PDF,
                        "https://example.com/files/100",
                        1024L,
                        LocalDateTime.of(2026, 2, 1, 12, 0),
                        uploader
                ),
                new SharedDocumentsPreviewResDto.DocumentDto(
                        101L,
                        false,
                        "디자인 가이드",
                        "design.png",
                        FileExt.PNG,
                        "https://example.com/files/101",
                        2048L,
                        LocalDateTime.of(2026, 2, 2, 9, 30),
                        uploader
                )
        );

        SharedDocumentsPreviewResDto response = new SharedDocumentsPreviewResDto(docs);

        given(facade.getPreview(eq(projectId), eq(userId), eq(limit))).willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/boards/shared-documents/preview", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("limit", String.valueOf(limit))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("boards-shared-documents-preview-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Boards")
                                        .summary("공유 문서함 프리뷰 조회")
                                        .description("팀보드 가운데 '공유 문서함' 카드에 표시할 문서 목록을 조회합니다. (기본 limit=4)")
                                        .pathParameters(
                                                parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .queryParameters(
                                                parameterWithName("limit").optional().description("프리뷰로 가져올 문서 개수(기본 4)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.documents").type(ARRAY).description("문서 목록"),

                                                fieldWithPath("body.documents[].document_id").type(NUMBER).description("문서 ID"),
                                                fieldWithPath("body.documents[].is_pinned").type(BOOLEAN).description("고정 여부"),
                                                fieldWithPath("body.documents[].title").type(STRING).description("문서 제목"),
                                                fieldWithPath("body.documents[].file_name").type(STRING).description("파일명"),
                                                fieldWithPath("body.documents[].file_ext").type(STRING).description("파일 확장자"),
                                                fieldWithPath("body.documents[].file_url").type(STRING).description("파일 URL"),
                                                fieldWithPath("body.documents[].file_size").type(NUMBER).description("파일 크기(byte)"),
                                                fieldWithPath("body.documents[].created_at").type(STRING).description("업로드 시각(ISO-8601)"),

                                                fieldWithPath("body.documents[].uploader").type(OBJECT).description("업로더 정보"),
                                                fieldWithPath("body.documents[].uploader.user_id").type(NUMBER).description("업로더 유저 ID"),
                                                fieldWithPath("body.documents[].uploader.name").type(STRING).description("업로더 이름"),
                                                fieldWithPath("body.documents[].uploader.nickname").type(STRING).description("업로더 닉네임"),
                                                fieldWithPath("body.documents[].uploader.profile_image_url").optional().type(STRING).description("업로더 프로필 이미지 URL")
                                        )
                                        .build()
                        )
                ));

        verify(facade).getPreview(eq(projectId), eq(userId), eq(limit));
    }
}
