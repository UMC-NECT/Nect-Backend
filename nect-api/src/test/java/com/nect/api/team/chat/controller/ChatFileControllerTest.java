package com.nect.api.team.chat.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.domain.team.chat.dto.res.ChatFileResponseDto;
import com.nect.api.domain.team.chat.dto.res.ChatFileUploadResponseDto;
import com.nect.api.domain.team.chat.dto.res.ChatRoomAlbumResponseDto;
import com.nect.api.domain.team.chat.service.ChatFileService;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
public class ChatFileControllerTest {

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

    private static final String AUTH_HEADER = "Authorization";
    private static final String TEST_ACCESS_TOKEN = "Bearer AccessToken";

    @BeforeEach
    void setUpAuth() {

        doNothing().when(jwtUtil).validateToken(anyString());
        given(tokenBlacklistService.isBlacklisted(anyString())).willReturn(false);
        given(jwtUtil.getUserIdFromToken(anyString())).willReturn(1L);
        given(userDetailsService.loadUserByUsername(anyString())).willReturn(
                UserDetailsImpl.builder()
                        .userId(1L)
                        .roles(List.of("ROLE_USER"))
                        .build()
        );
    }

    @Test
    @DisplayName("파일 업로드 API 테스트")
    void uploadFile() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "test file content".getBytes()
        );

        ChatFileUploadResponseDto response = ChatFileUploadResponseDto.builder()
                .fileId(1L)
                .fileName("test.png")
                .fileUrl("/files/uuid-test.png")
                .fileSize(1024L)
                .fileType("image/png")
                .build();

        given(chatFileService.uploadFile(anyLong(), any()))
                .willReturn(response);

        // When & Then
        mockMvc.perform(multipart("/api/v1/chats/{roomId}/files", 1L)
                        .file(file)
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.file_id").value(1L))
                .andDo(document("chat-file-upload",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("roomId").description("파일을 업로드할 채팅방 ID")
                        ),
                        requestParts(
                                partWithName("file").description("업로드할 이미지 또는 파일 (MultipartFile)")
                        ),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅 파일")
                                .summary("파일 업로드")
                                .description("채팅 메시지에 첨부할 파일을 업로드합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),
                                        fieldWithPath("body.file_id").description("생성된 파일 고유 ID"),
                                        fieldWithPath("body.file_name").description("원본 파일 이름"),
                                        fieldWithPath("body.file_url").description("파일 접근 URL"),
                                        fieldWithPath("body.file_size").description("파일 크기 (bytes)"),
                                        fieldWithPath("body.file_type").description("파일 확장자/타입")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("전체 파일함 조회 API 테스트")
    void getAllChatAlbums() throws Exception {
        // Given
        ChatFileResponseDto file = ChatFileResponseDto.builder()
                .fileName("photo.png")
                .fileUrl("/url")
                .createdAt(LocalDateTime.now())
                .build();

        ChatRoomAlbumResponseDto album = ChatRoomAlbumResponseDto.builder()
                .roomId(1L)
                .roomName("넥트 전체방")
                .files(List.of(file))
                .build();

        given(chatFileService.getChatAlbum(anyLong())).willReturn(List.of(album));

        // When & Then
        mockMvc.perform(get("/api/v1/chats/projects/{projectId}/albums", 1L)
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body[0].room_name").value("넥트 전체방"))
                .andDo(document("chat-all-albums",
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 ID")
                        ),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅 파일")
                                .summary("전체 파일 조회")
                                .description("참여 중인 모든 채팅방의 사진을 그룹화하여 조회합니다. (최근 15일)")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),
                                        fieldWithPath("body[].room_id").description("채팅방 ID"),
                                        fieldWithPath("body[].room_name").description("채팅방 이름"),
                                        fieldWithPath("body[].files[].file_name").description("파일명"),
                                        fieldWithPath("body[].files[].file_url").description("파일 URL"),
                                        fieldWithPath("body[].files[].created_at").description("생성 시간")
                                ).build()
                        )
                ));
    }

    @Test
    @DisplayName("특정 채팅방 상세 파일함 조회 API 테스트(더보기)")
    void getChatRoomDetailAlbum() throws Exception {
        // Given
        ChatFileResponseDto file = ChatFileResponseDto.builder()
                .fileName("detail.jpg")
                .fileUrl("/detail-url")
                .createdAt(LocalDateTime.now())
                .build();

        given(chatFileService.getChatRoomDetailAlbum(anyLong())).willReturn(List.of(file));

        // When & Then
        mockMvc.perform(get("/api/v1/chats/rooms/{roomId}/album", 1L)
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body[0].file_name").value("detail.jpg"))
                .andDo(document("chat-room-detail-album",
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("roomId").description("채팅방 ID")
                        ),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅 파일")
                                .summary("특정 채팅방 상세 파일 조회")
                                .description("특정 채팅방의 모든 사진을 조회합니다. (최근 15일)")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),
                                        fieldWithPath("body[].file_name").description("파일명"),
                                        fieldWithPath("body[].file_url").description("파일 URL"),
                                        fieldWithPath("body[].created_at").description("생성 시간")
                                ).build()
                        )
                ));
    }
}