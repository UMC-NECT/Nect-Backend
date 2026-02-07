package com.nect.api.domain.team.workspace.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.workspace.dto.req.PostCreateReqDto;
import com.nect.api.domain.team.workspace.dto.req.PostUpdateReqDto;
import com.nect.api.domain.team.workspace.dto.res.*;
import com.nect.api.domain.team.workspace.enums.PostSort;
import com.nect.api.domain.team.workspace.facade.PostFacade;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
import com.nect.core.entity.team.workspace.enums.PostType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
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
class PostControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostFacade postFacade;

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
    @DisplayName("게시글 생성")
    void createPost() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        PostCreateReqDto request = new PostCreateReqDto(
                PostType.NOTICE,
                "공지 제목",
                "공지 내용",
                true,
                List.of(2L, 3L)
        );

        PostCreateResDto response = new PostCreateResDto(100L);

        given(postFacade.createPost(eq(projectId), eq(userId), any(PostCreateReqDto.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/projects/{projectId}/boards/posts", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("post-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Post")
                                        .summary("게시글 생성")
                                        .description("프로젝트 게시판에 게시글을 생성합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("post_type").type(STRING).description("게시글 타입"),
                                                fieldWithPath("title").type(STRING).description("제목"),
                                                fieldWithPath("content").type(STRING).description("내용"),
                                                fieldWithPath("is_pinned").type(BOOLEAN).optional().description("상단 고정 여부"),
                                                fieldWithPath("mention_user_ids").type(ARRAY).optional().description("멘션 유저 ID 목록")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.post_id").type(NUMBER).description("생성된 게시글 ID")
                                        )
                                        .build()
                        )
                ));

        verify(postFacade).createPost(eq(projectId), eq(userId), any(PostCreateReqDto.class));
    }

    @Test
    @DisplayName("게시글 상세 조회")
    void getPost() throws Exception {
        long projectId = 1L;
        long postId = 100L;
        long userId = 1L;

        PostGetResDto response = new PostGetResDto(
                postId,
                PostType.NOTICE,
                "공지 제목",
                "공지 내용",
                true,
                7L,
                LocalDateTime.of(2026, 1, 31, 10, 0),
                new PostGetResDto.AuthorDto(1L, "노수민")
        );

        given(postFacade.getPost(eq(projectId), eq(userId), eq(postId)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/boards/posts/{postId}", projectId, postId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("post-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Post")
                                        .summary("게시글 상세 조회")
                                        .description("게시글 단건 상세 정보를 조회합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("postId").description("게시글 ID")
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
                                                fieldWithPath("body.post_id").type(NUMBER).description("게시글 ID"),
                                                fieldWithPath("body.post_type").type(STRING).description("게시글 타입"),
                                                fieldWithPath("body.title").type(STRING).description("제목"),
                                                fieldWithPath("body.content").type(STRING).description("내용"),
                                                fieldWithPath("body.is_pinned").type(BOOLEAN).description("상단 고정 여부"),
                                                fieldWithPath("body.like_count").type(NUMBER).description("좋아요 수"),
                                                fieldWithPath("body.created_at").type(STRING).description("작성 시각(ISO-8601)"),

                                                fieldWithPath("body.author").type(OBJECT).description("작성자 정보"),
                                                fieldWithPath("body.author.user_id").type(NUMBER).description("작성자 유저 ID"),
                                                fieldWithPath("body.author.name").type(STRING).description("작성자 이름")
                                        )
                                        .build()
                        )
                ));

        verify(postFacade).getPost(eq(projectId), eq(userId), eq(postId));
    }

    @Test
    @DisplayName("게시글 목록 조회")
    void getPostList() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        PostListResDto response = new PostListResDto(
                List.of(
                        new PostListResDto.PostSummaryDto(
                                100L,
                                PostType.NOTICE,
                                "공지 제목",
                                "공지 내용...",
                                true,
                                7L,
                                LocalDateTime.of(2026, 1, 31, 10, 0)
                        ),
                        new PostListResDto.PostSummaryDto(
                                101L,
                                PostType.FREE,
                                "자유글",
                                "자유 내용...",
                                false,
                                1L,
                                LocalDateTime.of(2026, 1, 31, 11, 0)
                        )
                ),
                new PostListResDto.PageInfo(0, 20, 2L, 1, false)
        );

        given(postFacade.getPostList(eq(projectId), eq(userId), any(), eq(PostSort.LATEST), eq(0), eq(20)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/boards/posts", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("type", "NOTICE")
                        .param("sort", "LATEST")
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("post-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Post")
                                        .summary("게시글 목록 조회")
                                        .description("프로젝트 게시판의 게시글 목록을 페이징 조회합니다. pinned 우선 정렬이 적용될 수 있습니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .queryParameters(
                                                ResourceDocumentation.parameterWithName("type").optional().description("게시글 타입(미지정 시 전체)"),
                                                ResourceDocumentation.parameterWithName("sort").optional().description("정렬 기준(LATEST|OLDEST|POPULAR)"),
                                                ResourceDocumentation.parameterWithName("page").optional().description("페이지 번호(0부터)"),
                                                ResourceDocumentation.parameterWithName("size").optional().description("페이지 크기(기본 20, 최대 50)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.posts").type(ARRAY).description("게시글 목록"),
                                                fieldWithPath("body.posts[].post_id").type(NUMBER).description("게시글 ID"),
                                                fieldWithPath("body.posts[].post_type").type(STRING).description("게시글 타입"),
                                                fieldWithPath("body.posts[].title").type(STRING).description("제목"),
                                                fieldWithPath("body.posts[].content_preview").type(STRING).description("내용 프리뷰(일부)"),
                                                fieldWithPath("body.posts[].is_pinned").type(BOOLEAN).description("상단 고정 여부"),
                                                fieldWithPath("body.posts[].like_count").type(NUMBER).description("좋아요 수"),
                                                fieldWithPath("body.posts[].created_at").type(STRING).description("작성 시각(ISO-8601)"),

                                                fieldWithPath("body.page_info").type(OBJECT).description("페이지 정보"),
                                                fieldWithPath("body.page_info.page").type(NUMBER).description("현재 페이지(0부터)"),
                                                fieldWithPath("body.page_info.size").type(NUMBER).description("페이지 크기"),
                                                fieldWithPath("body.page_info.total_elements").type(NUMBER).description("전체 요소 수"),
                                                fieldWithPath("body.page_info.total_pages").type(NUMBER).description("전체 페이지 수"),
                                                fieldWithPath("body.page_info.has_next").type(BOOLEAN).description("다음 페이지 존재 여부")
                                        )
                                        .build()
                        )
                ));

        verify(postFacade).getPostList(eq(projectId), eq(userId), any(), eq(PostSort.LATEST), eq(0), eq(20));
    }

    @Test
    @DisplayName("게시글 수정")
    void updatePost() throws Exception {
        long projectId = 1L;
        long postId = 100L;
        long userId = 1L;

        // PostUpdateReqDto에서 is_pinned가 primitive boolean이므로 null(변경 없음) 표현 불가
        PostUpdateReqDto request = new PostUpdateReqDto(
                PostType.FREE,
                "수정 제목",
                "수정 내용",
                false,
                List.of(2L)
        );

        PostUpdateResDto response = new PostUpdateResDto(postId, LocalDateTime.of(2026, 1, 31, 12, 0));

        given(postFacade.updatePost(eq(projectId), eq(userId), eq(postId), any(PostUpdateReqDto.class)))
                .willReturn(response);

        mockMvc.perform(patch("/api/v1/projects/{projectId}/boards/posts/{postId}", projectId, postId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("post-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Post")
                                        .summary("게시글 수정")
                                        .description("게시글을 수정합니다. 작성자만 수정할 수 있습니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("postId").description("게시글 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("post_type").type(STRING).optional().description("게시글 타입(미지정 시 유지)"),
                                                fieldWithPath("title").type(STRING).optional().description("제목(미지정 시 유지)"),
                                                fieldWithPath("content").type(STRING).optional().description("내용(미지정 시 유지)"),
                                                fieldWithPath("is_pinned").type(BOOLEAN).description("상단 고정 여부"),
                                                fieldWithPath("mention_user_ids").type(ARRAY).optional().description("멘션 유저 ID 목록")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.post_id").type(NUMBER).description("게시글 ID"),
                                                fieldWithPath("body.updated_at").type(STRING).description("수정 시각(ISO-8601)")
                                        )
                                        .build()
                        )
                ));

        verify(postFacade).updatePost(eq(projectId), eq(userId), eq(postId), any(PostUpdateReqDto.class));
    }

    @Test
    @DisplayName("게시글 좋아요 토글")
    void togglePostLike() throws Exception {
        long projectId = 1L;
        long postId = 100L;
        long userId = 1L;

        PostLikeToggleResDto response = new PostLikeToggleResDto(postId, true, 8L);

        given(postFacade.togglePostLike(eq(projectId), eq(userId), eq(postId)))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/projects/{projectId}/boards/posts/{postId}/likes", projectId, postId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("post-like-toggle",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Post")
                                        .summary("게시글 좋아요 토글")
                                        .description("게시글 좋아요를 토글합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("postId").description("게시글 ID")
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
                                                fieldWithPath("body.post_id").type(NUMBER).description("게시글 ID"),
                                                fieldWithPath("body.liked").type(BOOLEAN).description("좋아요 상태(true=좋아요됨)"),
                                                fieldWithPath("body.like_count").type(NUMBER).description("요청 처리 후 좋아요 수")
                                        )
                                        .build()
                        )
                ));

        verify(postFacade).togglePostLike(eq(projectId), eq(userId), eq(postId));
    }

    @Test
    @DisplayName("게시글 삭제")
    void deletePost() throws Exception {
        long projectId = 1L;
        long postId = 100L;
        long userId = 1L;

        willDoNothing().given(postFacade).deletePost(eq(projectId), eq(userId), eq(postId));

        mockMvc.perform(delete("/api/v1/projects/{projectId}/boards/posts/{postId}", projectId, postId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("post-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Post")
                                        .summary("게시글 삭제")
                                        .description("게시글을 삭제(soft delete)합니다. 작성자만 삭제할 수 있습니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID"),
                                                ResourceDocumentation.parameterWithName("postId").description("게시글 ID")
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

        verify(postFacade).deletePost(eq(projectId), eq(userId), eq(postId));
    }

    @Test
    @DisplayName("게시글 프리뷰 조회")
    void getPostsPreview() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        PostsPreviewResDto response = new PostsPreviewResDto(
                List.of(
                        new PostsPreviewResDto.Item(
                                100L,
                                PostType.NOTICE,
                                "공지 제목",
                                true,
                                LocalDateTime.of(2026, 1, 31, 10, 0)
                        ),
                        new PostsPreviewResDto.Item(
                                101L,
                                PostType.FREE,
                                "자유글",
                                false,
                                LocalDateTime.of(2026, 1, 31, 11, 0)
                        )
                )
        );

        given(postFacade.getPostsPreview(eq(projectId), eq(userId), any(), eq(4)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/boards/posts/preview", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("type", "NOTICE")
                        .param("limit", "4")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("post-preview",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Post")
                                        .summary("게시글 프리뷰 조회")
                                        .description("게시판 상단에 노출할 게시글 프리뷰(최대 N개)를 조회합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .queryParameters(
                                                ResourceDocumentation.parameterWithName("type").optional().description("게시글 타입(미지정 시 전체)"),
                                                ResourceDocumentation.parameterWithName("limit").optional().description("조회할 개수(기본 4)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                                fieldWithPath("body.posts").type(ARRAY).description("프리뷰 게시글 목록"),
                                                fieldWithPath("body.posts[].post_id").type(NUMBER).description("게시글 ID"),
                                                fieldWithPath("body.posts[].post_type").type(STRING).description("게시글 타입"),
                                                fieldWithPath("body.posts[].title").type(STRING).description("제목"),
                                                fieldWithPath("body.posts[].is_pinned").type(BOOLEAN).description("상단 고정 여부"),
                                                fieldWithPath("body.posts[].created_at").type(STRING).description("작성 시각(ISO-8601)")
                                        )
                                        .build()
                        )
                ));

        verify(postFacade).getPostsPreview(eq(projectId), eq(userId), any(), eq(4));
    }
}
