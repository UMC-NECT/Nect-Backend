package com.nect.api.domain.team.workspace.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.workspace.dto.req.SharedDocumentNameUpdateReqDto;
import com.nect.api.domain.team.workspace.dto.res.SharedDocumentNameUpdateResDto;
import com.nect.api.domain.team.workspace.dto.res.SharedDocumentsGetResDto;
import com.nect.api.domain.team.workspace.dto.res.SharedDocumentsPreviewResDto;
import com.nect.api.domain.team.workspace.enums.SharedDocumentsSort;
import com.nect.api.domain.team.workspace.facade.BoardsSharedDocumentFacade;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

    @Test
    @DisplayName("공유 문서함 조회")
    void getSharedDocuments() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        int page = 0;
        int size = 20;
        DocumentType type = DocumentType.FILE;
        SharedDocumentsSort sort = SharedDocumentsSort.RECENT;

        // SharedDocumentsGetResDto 구조를 여기서 정확히 모르므로 mock으로 대체 (body는 {} 로 직렬화)
        SharedDocumentsGetResDto response = org.mockito.Mockito.mock(SharedDocumentsGetResDto.class);

        given(facade.getDocuments(eq(projectId), eq(userId), eq(page), eq(size), eq(type), eq(sort)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/boards/shared-documents", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("type", type.name())
                        .param("sort", sort.name())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("boards-shared-documents-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Boards")
                                        .summary("공유 문서함 조회")
                                        .description("공유 문서함 목록을 조회합니다. (페이징/타입/정렬 지원)")
                                        .pathParameters(
                                                parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .queryParameters(
                                                parameterWithName("page").optional().description("페이지 번호(기본 0)"),
                                                parameterWithName("size").optional().description("페이지 크기(기본 20)"),
                                                parameterWithName("type").optional().description("문서 타입(FILE/LINK)"),
                                                parameterWithName("sort").optional().description("정렬 기준(기본 RECENT)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.page").type(NUMBER).description("현재 페이지"),
                                                fieldWithPath("body.size").type(NUMBER).description("페이지 크기"),
                                                fieldWithPath("body.total_elements").type(NUMBER).description("전체 요소 수"),
                                                fieldWithPath("body.total_pages").type(NUMBER).description("전체 페이지 수"),
                                                fieldWithPath("body.documents").type(ARRAY).description("문서 목록")
                                        )
                                        .build()
                        )
                ));

        verify(facade).getDocuments(eq(projectId), eq(userId), eq(page), eq(size), eq(type), eq(sort));
    }

    @Test
    @DisplayName("문서 이름(표시명) 수정")
    void renameSharedDocument() throws Exception {
        long projectId = 1L;
        long userId = 1L;
        long documentId = 100L;

        // 요청: title 기준(하위호환이 필요하면 name도 같이 넣어도 됨)
        SharedDocumentNameUpdateReqDto req = new SharedDocumentNameUpdateReqDto("새 문서 제목", null);

        SharedDocumentNameUpdateResDto res = new SharedDocumentNameUpdateResDto(documentId, "새 문서 제목");

        given(facade.rename(eq(projectId), eq(userId), eq(documentId), any(SharedDocumentNameUpdateReqDto.class)))
                .willReturn(res);

        mockMvc.perform(patch("/api/v1/projects/{projectId}/boards/shared-documents/{documentId}/name", projectId, documentId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("boards-shared-documents-rename",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Boards")
                                        .summary("문서 표시명(title) 변경")
                                        .description("공유 문서의 표시명(title)을 변경합니다. (하위호환: name도 지원 가능)")
                                        .pathParameters(
                                                parameterWithName("projectId").description("프로젝트 ID"),
                                                parameterWithName("documentId").description("문서 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("title").optional().type(STRING).description("변경할 표시명(title)"),
                                                fieldWithPath("name").optional().type(STRING).description("하위호환용 필드(name) - title이 없을 때 사용")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.document_id").type(NUMBER).description("문서 ID"),
                                                fieldWithPath("body.title").type(STRING).description("변경된 표시명(title)")
                                        )
                                        .build()
                        )
                ));

        verify(facade).rename(eq(projectId), eq(userId), eq(documentId), any(SharedDocumentNameUpdateReqDto.class));
    }

    @Test
    @DisplayName("문서 삭제")
    void deleteSharedDocument() throws Exception {
        long projectId = 1L;
        long userId = 1L;
        long documentId = 200L;

        doNothing().when(facade).delete(eq(projectId), eq(userId), eq(documentId));

        mockMvc.perform(delete("/api/v1/projects/{projectId}/boards/shared-documents/{documentId}", projectId, documentId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("boards-shared-documents-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Boards")
                                        .summary("문서 삭제")
                                        .description("공유 문서를 삭제(소프트 삭제)합니다.")
                                        .pathParameters(
                                                parameterWithName("projectId").description("프로젝트 ID"),
                                                parameterWithName("documentId").description("문서 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명")
                                        )
                                        .build()
                        )
                ));

        verify(facade).delete(eq(projectId), eq(userId), eq(documentId));
    }
}
