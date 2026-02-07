package com.nect.api.team.chat.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.domain.team.chat.dto.req.ChatMessageDto;
import com.nect.api.domain.team.chat.dto.res.ChatFileDetailDto;
import com.nect.api.domain.team.chat.dto.res.ChatFileResponseDto;
import com.nect.api.domain.team.chat.dto.res.ChatFileUploadResponseDto;
import com.nect.api.domain.team.chat.dto.res.ChatRoomAlbumDetailDto;
import com.nect.api.domain.team.chat.dto.res.ChatRoomAlbumResponseDto;
import com.nect.api.domain.team.chat.service.ChatFileService;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
import com.nect.core.entity.team.chat.enums.MessageType;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class ChatFileControllerTest {

    private static final String AUTH_HEADER = "Authorization";
    private static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatFileService chatFileService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUpAuth() {
        doNothing().when(jwtUtil).validateToken(any());
        given(tokenBlacklistService.isBlacklisted(any())).willReturn(false);
        given(jwtUtil.getUserIdFromToken(any())).willReturn(1L);
        given(userDetailsService.loadUserByUsername(any())).willReturn(
                UserDetailsImpl.builder()
                        .userId(1L)
                        .roles(List.of("ROLE_USER"))
                        .build()
        );
    }

    private RequestPostProcessor mockUser(Long userId) {
        UserDetailsImpl principal = UserDetailsImpl.builder()
                .userId(userId)
                .roles(List.of("ROLE_USER"))
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                "",
                principal.getAuthorities()
        );

        return authentication(auth);
    }

    @Test
    @DisplayName("채팅 파일 업로드")
    void uploadFile() throws Exception {
        long roomId = 10L;
        long userId = 1L;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "design.png",
                MediaType.IMAGE_PNG_VALUE,
                "dummy image bytes".getBytes()
        );

        ChatFileUploadResponseDto fileInfo = new ChatFileUploadResponseDto(
                200L,
                "design.png",
                "https://cdn.example.com/chat/files/200_design.png",
                2048L,
                "PNG"
        );

        ChatMessageDto response = ChatMessageDto.builder()
                .messageId(1000L)
                .userId(userId)
                .roomId(roomId)
                .userName("민우")
                .profileImage("/images/default-profile.png")
                .content("파일 업로드")
                .messageType(MessageType.FILE)
                .isPinned(false)
                .createdAt(LocalDateTime.of(2025, 1, 10, 12, 0))
                .readCount(0)
                .fileInfo(fileInfo)
                .build();

        given(chatFileService.uploadAndSendFile(eq(roomId), any(), eq(userId)))
                .willReturn(response);

        mockMvc.perform(
                        multipart("/api/v1/chats/{roomId}/files", roomId)
                                .file(file)
                                .with(mockUser(userId))
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.message_id").value(1000))
                .andDo(document("chat-file-upload",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParts(
                                partWithName("file").description("업로드할 파일(MultipartFile)")
                        ),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("채팅 파일 업로드")
                                .description("채팅방에 파일을 업로드하고 파일 메시지를 전송합니다.")
                                .pathParameters(
                                        ResourceDocumentation.parameterWithName("roomId").description("채팅방 ID")
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
                                        fieldWithPath("body.message_id").type(NUMBER).description("메시지 ID"),
                                        fieldWithPath("body.user_id").type(NUMBER).description("보낸 사람 유저 ID"),
                                        fieldWithPath("body.room_id").type(NUMBER).description("채팅방 ID"),
                                        fieldWithPath("body.user_name").type(STRING).description("보낸 사람 이름"),
                                        fieldWithPath("body.profile_image").type(STRING).description("프로필 이미지 URL"),
                                        fieldWithPath("body.content").type(STRING).description("메시지 내용"),
                                        fieldWithPath("body.message_type").type(STRING).description("메시지 타입"),
                                        fieldWithPath("body.is_pinned").type(BOOLEAN).description("고정 여부"),
                                        fieldWithPath("body.created_at").type(STRING).description("생성일시(ISO-8601)"),
                                        fieldWithPath("body.read_count").type(NUMBER).description("읽음 수"),
                                        fieldWithPath("body.file_info").type(OBJECT).description("파일 정보"),
                                        fieldWithPath("body.file_info.file_id").type(NUMBER).description("파일 ID"),
                                        fieldWithPath("body.file_info.file_name").type(STRING).description("파일명"),
                                        fieldWithPath("body.file_info.file_url").type(STRING).description("파일 URL"),
                                        fieldWithPath("body.file_info.file_size").type(NUMBER).description("파일 크기(bytes)"),
                                        fieldWithPath("body.file_info.file_type").type(STRING).description("파일 확장자")
                                )
                                .build()
                        )
                ));

        verify(chatFileService).uploadAndSendFile(eq(roomId), any(), eq(userId));
    }

    @Test
    @DisplayName("채팅 파일 삭제")
    void deleteFile() throws Exception {
        long fileId = 55L;
        long userId = 1L;

        doNothing().when(chatFileService).deleteFile(eq(fileId), eq(userId));

        mockMvc.perform(
                        delete("/api/v1/chats/files/{fileId}", fileId)
                                .with(mockUser(userId))
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("chat-file-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("채팅 파일 삭제")
                                .description("채팅 파일을 삭제합니다.")
                                .pathParameters(
                                        ResourceDocumentation.parameterWithName("fileId").description("파일 ID")
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

        verify(chatFileService).deleteFile(eq(fileId), eq(userId));
    }

    @Test
    @DisplayName("프로젝트 채팅 앨범 조회")
    void getProjectAlbum() throws Exception {
        long projectId = 3L;
        long userId = 1L;

        List<ChatRoomAlbumResponseDto> response = List.of(
                new ChatRoomAlbumResponseDto(
                        10L,
                        "개발팀",
                        "GROUP",
                        2,
                        List.of(
                                new ChatFileResponseDto(
                                        "design.png",
                                        "https://cdn.example.com/chat/files/design.png",
                                        LocalDateTime.of(2025, 1, 10, 11, 0)
                                ),
                                new ChatFileResponseDto(
                                        "spec.pdf",
                                        "https://cdn.example.com/chat/files/spec.pdf",
                                        LocalDateTime.of(2025, 1, 9, 18, 30)
                                )
                        )
                )
        );

        given(chatFileService.getChatAlbum(eq(projectId), eq(9), eq(userId)))
                .willReturn(response);

        mockMvc.perform(
                        get("/api/v1/chats/projects/{projectId}/albums", projectId)
                                .with(mockUser(userId))
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .param("limit", "9")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body[0].room_id").value(10))
                .andDo(document("chat-project-album",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("프로젝트 채팅 앨범 조회")
                                .description("프로젝트 내 채팅방의 최근 파일 목록을 조회합니다.")
                                .pathParameters(
                                        ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID")
                                )
                                .queryParameters(
                                        parameterWithName("limit").description("채팅방별 파일 조회 개수")
                                )
                                .requestHeaders(
                                        headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                )
                                .responseFields(
                                        fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                        fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                        fieldWithPath("status.message").type(STRING).description("메시지"),
                                        fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                        fieldWithPath("body").type(ARRAY).description("응답 바디"),
                                        fieldWithPath("body[].room_id").type(NUMBER).description("채팅방 ID"),
                                        fieldWithPath("body[].room_name").type(STRING).description("채팅방 이름"),
                                        fieldWithPath("body[].room_type").type(STRING).description("채팅방 타입"),
                                        fieldWithPath("body[].file_count").type(NUMBER).description("파일 개수"),
                                        fieldWithPath("body[].files").type(ARRAY).description("파일 목록"),
                                        fieldWithPath("body[].files[].file_name").type(STRING).description("파일명"),
                                        fieldWithPath("body[].files[].file_url").type(STRING).description("파일 URL"),
                                        fieldWithPath("body[].files[].created_at").type(STRING).description("생성일시(ISO-8601)")
                                )
                                .build()
                        )
                ));

        verify(chatFileService).getChatAlbum(eq(projectId), eq(9), eq(userId));
    }

    @Test
    @DisplayName("채팅방 앨범 상세 조회")
    void getChatRoomAlbumDetail() throws Exception {
        long roomId = 10L;
        long userId = 1L;

        ChatRoomAlbumDetailDto response = new ChatRoomAlbumDetailDto(
                roomId,
                "개발팀",
                List.of(
                        new ChatFileResponseDto(
                                "design.png",
                                "https://cdn.example.com/chat/files/design.png",
                                LocalDateTime.of(2025, 1, 10, 11, 0)
                        )
                ),
                1,
                0,
                1,
                false
        );

        given(chatFileService.getChatRoomAlbumDetail(eq(roomId), eq(0), eq(20), eq(userId)))
                .willReturn(response);

        mockMvc.perform(
                        get("/api/v1/chats/rooms/{roomId}/album", roomId)
                                .with(mockUser(userId))
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .param("page", "0")
                                .param("size", "20")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("chat-room-album-detail",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("채팅방 앨범 상세 조회")
                                .description("채팅방의 파일 앨범 상세 목록을 페이징 조회합니다.")
                                .pathParameters(
                                        ResourceDocumentation.parameterWithName("roomId").description("채팅방 ID")
                                )
                                .queryParameters(
                                        parameterWithName("page").description("페이지 번호(0부터 시작)"),
                                        parameterWithName("size").description("페이지당 항목 수")
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
                                        fieldWithPath("body.room_id").type(NUMBER).description("채팅방 ID"),
                                        fieldWithPath("body.room_name").type(STRING).description("채팅방 이름"),
                                        fieldWithPath("body.files").type(ARRAY).description("파일 목록"),
                                        fieldWithPath("body.files[].file_name").type(STRING).description("파일명"),
                                        fieldWithPath("body.files[].file_url").type(STRING).description("파일 URL"),
                                        fieldWithPath("body.files[].created_at").type(STRING).description("생성일시(ISO-8601)"),
                                        fieldWithPath("body.total_count").type(NUMBER).description("총 파일 수"),
                                        fieldWithPath("body.current_page").type(NUMBER).description("현재 페이지"),
                                        fieldWithPath("body.total_pages").type(NUMBER).description("총 페이지 수"),
                                        fieldWithPath("body.has_next").type(BOOLEAN).description("다음 페이지 존재 여부")
                                )
                                .build()
                        )
                ));

        verify(chatFileService).getChatRoomAlbumDetail(eq(roomId), eq(0), eq(20), eq(userId));
    }

    @Test
    @DisplayName("채팅 파일 상세 조회")
    void getFileDetail() throws Exception {
        long fileId = 55L;
        long userId = 1L;

        ChatFileDetailDto response = ChatFileDetailDto.builder()
                .fileId(fileId)
                .fileName("design.png")
                .fileUrl("https://cdn.example.com/chat/files/design.png")
                .fileSize(2048L)
                .fileType("PNG")
                .createdAt(LocalDateTime.of(2025, 1, 10, 12, 10))
                .build();

        given(chatFileService.getFileDetail(eq(fileId), eq(userId)))
                .willReturn(response);

        mockMvc.perform(
                        get("/api/v1/chats/files/{fileId}", fileId)
                                .with(mockUser(userId))
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("chat-file-detail",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("채팅 파일 상세 조회")
                                .description("채팅 파일의 상세 정보를 조회합니다.")
                                .pathParameters(
                                        ResourceDocumentation.parameterWithName("fileId").description("파일 ID")
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
                                        fieldWithPath("body.file_id").type(NUMBER).description("파일 ID"),
                                        fieldWithPath("body.file_name").type(STRING).description("파일명"),
                                        fieldWithPath("body.file_url").type(STRING).description("파일 URL"),
                                        fieldWithPath("body.file_size").type(NUMBER).description("파일 크기(bytes)"),
                                        fieldWithPath("body.file_type").type(STRING).description("파일 확장자"),
                                        fieldWithPath("body.created_at").type(STRING).description("생성일시(ISO-8601)")
                                )
                                .build()
                        )
                ));

        verify(chatFileService).getFileDetail(eq(fileId), eq(userId));
    }

    @Test
    @DisplayName("채팅 파일 다운로드(리다이렉트)")
    void downloadFile() throws Exception {
        long fileId = 55L;
        long userId = 1L;
        String redirectUrl = "https://cdn.example.com/chat/files/55/download?token=abc";

        given(chatFileService.getDownloadUrl(eq(fileId), eq(userId)))
                .willReturn(redirectUrl);

        mockMvc.perform(
                        get("/api/v1/chats/files/{fileId}/download", fileId)
                                .with(mockUser(userId))
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                )
                .andExpect(status().isFound())
                .andExpect(header().string("Location", redirectUrl))
                .andDo(document("chat-file-download-redirect",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("채팅 파일 다운로드(리다이렉트)")
                                .description("파일 다운로드 URL을 조회한 뒤 302(FOUND)로 Location 헤더에 담아 리다이렉트합니다.")
                                .pathParameters(
                                        ResourceDocumentation.parameterWithName("fileId").description("파일 ID")
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

        verify(chatFileService).getDownloadUrl(eq(fileId), eq(userId));
    }
}
