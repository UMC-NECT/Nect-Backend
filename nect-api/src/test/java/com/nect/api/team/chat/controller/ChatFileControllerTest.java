package com.nect.api.team.chat.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.chat.dto.req.ChatMessageDto;
import com.nect.api.domain.team.chat.dto.res.*;
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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
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
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class ChatFileControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("작업실 채팅방 파일 업로드 API")
    void uploadFile() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "test file content".getBytes()
        );

        // ChatMessageDto 구조로 변경
        ChatMessageDto response = ChatMessageDto.builder()
                .messageId(100L)
                .userId(1L)
                .roomId(1L)
                .userName("테스트유저")
                .profileImage("https://example.com/profile.jpg")
                .content("파일 전송")
                .messageType(MessageType.FILE)
                .isPinned(false)
                .createdAt(LocalDateTime.now())
                .readCount(0)
                .build();

        // fileInfo 추가
        ChatFileUploadResponseDto fileInfo = new ChatFileUploadResponseDto(
                1L,
                "test.png",
                "https://r2.example.com/uuid_test.png",
                1024L,
                "image/png"
        );
        response.setFileInfo(fileInfo);

        given(chatFileService.uploadAndSendFile(anyLong(), any(), anyLong()))
                .willReturn(response);

        // When & Then
        mockMvc.perform(multipart("/api/v1/chats/{roomId}/files", 1L)
                        .file(file)
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.message_id").value(100L))
                .andExpect(jsonPath("$.body.message_type").value("FILE"))
                .andExpect(jsonPath("$.body.file_info.file_id").value(1L))
                .andExpect(jsonPath("$.body.file_info.file_name").value("test.png"))
                .andDo(document("chat-file-upload",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅 파일")
                                .summary("파일 업로드 및 전송")
                                .description("채팅 메시지에 첨부할 이미지 파일을 Cloudflare R2에 업로드하고 " +
                                        "실시간으로 채팅방에 전송합니다. " +
                                        "jpg, jpeg, png 형식만 지원하며, 업로드된 파일은 DB에 메타데이터가 저장되고 " +
                                        "5분 유효한 Presigned URL이 반환됩니다. " +
                                        "Redis를 통해 채팅방의 모든 참여자에게 실시간으로 전달됩니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("roomId").description("파일을 업로드할 채팅방 ID")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),
                                        fieldWithPath("body.message_id").description("생성된 메시지 ID"),
                                        fieldWithPath("body.user_id").description("발신자 ID"),
                                        fieldWithPath("body.room_id").description("채팅방 ID"),
                                        fieldWithPath("body.user_name").description("발신자 이름"),
                                        fieldWithPath("body.profile_image").description("발신자 프로필 이미지").optional(),
                                        fieldWithPath("body.content").description("메시지 내용"),
                                        fieldWithPath("body.message_type").description("메시지 타입 (FILE)"),
                                        fieldWithPath("body.is_pinned").description("고정 여부"),
                                        fieldWithPath("body.created_at").description("메시지 생성 시간"),
                                        fieldWithPath("body.read_count").description("읽지 않은 사용자 수"),
                                        fieldWithPath("body.file_info").description("파일 정보"),
                                        fieldWithPath("body.file_info.file_id").description("생성된 파일 고유 ID"),
                                        fieldWithPath("body.file_info.file_name").description("원본 파일 이름"),
                                        fieldWithPath("body.file_info.file_url").description("Presigned URL (5분 유효)"),
                                        fieldWithPath("body.file_info.file_size").description("파일 크기 (bytes)"),
                                        fieldWithPath("body.file_info.file_type").description("파일 MIME 타입")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("작업실 채팅방 파일 업로드 API 클라우드 조회 API")
    void getProjectAlbum() throws Exception {
        // Given
        ChatFileResponseDto file1 = new ChatFileResponseDto(
                "photo1.png",
                "https://r2.example.com/uuid_photo1.png",
                LocalDateTime.now()
        );

        ChatFileResponseDto file2 = new ChatFileResponseDto(
                "photo2.jpg",
                "https://r2.example.com/uuid_photo2.jpg",
                LocalDateTime.now()
        );

        ChatRoomAlbumResponseDto album1 = new ChatRoomAlbumResponseDto(
                1L,
                "넥트 전체방",
                "GROUP",
                24,
                List.of(file1, file2)
        );

        ChatRoomAlbumResponseDto album2 = new ChatRoomAlbumResponseDto(
                2L,
                "디자인팀",
                "GROUP",
                15,
                List.of(file1)
        );

        given(chatFileService.getChatAlbum(anyLong(), anyInt(), anyLong()))
                .willReturn(List.of(album1, album2));

        // When & Then
        mockMvc.perform(
                        get("/api/v1/chats/projects/{projectId}/albums", 1L)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .param("limit", "9")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body[0].room_id").value(1L))
                .andExpect(jsonPath("$.body[0].room_name").value("넥트 전체방"))
                .andExpect(jsonPath("$.body[0].file_count").value(24))
                .andDo(document("chat-project-albums",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅 파일")
                                .summary("프로젝트 앨범 요약 조회")
                                .description("프로젝트 내 모든 채팅방의 파일을 채팅방별로 그룹핑하여 조회합니다. " +
                                        "각 채팅방당 최근 N개(기본 9개)만 반환하며, " +
                                        "더보기 버튼 표시 여부 판단을 위해 전체 파일 개수도 제공합니다. " +
                                        "최근 15일 이내 업로드된 파일만 포함됩니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("projectId").description("프로젝트 ID")
                                )
                                .queryParameters(
                                        parameterWithName("limit").description("각 채팅방당 최대 파일 개수 (기본: 9)").optional()
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),
                                        fieldWithPath("body[].room_id").description("채팅방 ID"),
                                        fieldWithPath("body[].room_name").description("채팅방 이름"),
                                        fieldWithPath("body[].room_type").description("채팅방 타입 (GROUP, DIRECT)"),
                                        fieldWithPath("body[].file_count").description("해당 채팅방의 전체 파일 개수 (최근 15일)"),
                                        fieldWithPath("body[].files[].file_name").description("원본 파일명"),
                                        fieldWithPath("body[].files[].file_url").description("Presigned URL (5분 유효)"),
                                        fieldWithPath("body[].files[].created_at").description("파일 업로드 시간")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("작업실 특정 채팅방 상세 앨범 조회 API")
    void getChatRoomAlbumDetail() throws Exception {
        // Given
        ChatFileResponseDto file1 = new ChatFileResponseDto(
                "detail1.jpg",
                "https://r2.example.com/uuid_detail1.jpg",
                LocalDateTime.now()
        );

        ChatFileResponseDto file2 = new ChatFileResponseDto(
                "detail2.png",
                "https://r2.example.com/uuid_detail2.png",
                LocalDateTime.now()
        );

        ChatRoomAlbumDetailDto response = new ChatRoomAlbumDetailDto(
                1L,
                "넥트 전체방",
                List.of(file1, file2),
                50,
                0,
                3,
                true
        );

        given(chatFileService.getChatRoomAlbumDetail(anyLong(), anyInt(), anyInt(), anyLong()))
                .willReturn(response);

        // When & Then
        mockMvc.perform(
                        get("/api/v1/chats/rooms/{roomId}/album", 1L)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .param("page", "0")
                                .param("size", "20")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.room_id").value(1L))
                .andExpect(jsonPath("$.body.room_name").value("넥트 전체방"))
                .andExpect(jsonPath("$.body.total_count").value(50))
                .andExpect(jsonPath("$.body.has_next").value(true))
                .andDo(document("chat-room-detail-album",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅 파일")
                                .summary("특정 채팅방 상세 앨범 조회")
                                .description("특정 채팅방의 파일을 페이징하여 조회합니다. " +
                                        "더보기 버튼 클릭 시 page 파라미터를 증가시켜 반복 호출하며, " +
                                        "has_next가 false가 될 때까지 호출 가능합니다. " +
                                        "최근 15일 이내 업로드된 파일만 조회됩니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("roomId").description("채팅방 ID")
                                )
                                .queryParameters(
                                        parameterWithName("page").description("페이지 번호 (0부터 시작, 기본: 0)").optional(),
                                        parameterWithName("size").description("페이지당 파일 개수 (기본: 20)").optional()
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),
                                        fieldWithPath("body.room_id").description("채팅방 ID"),
                                        fieldWithPath("body.room_name").description("채팅방 이름"),
                                        fieldWithPath("body.files[].file_name").description("원본 파일명"),
                                        fieldWithPath("body.files[].file_url").description("Presigned URL (5분 유효)"),
                                        fieldWithPath("body.files[].created_at").description("파일 업로드 시간"),
                                        fieldWithPath("body.total_count").description("전체 파일 개수 (최근 15일)"),
                                        fieldWithPath("body.current_page").description("현재 페이지 번호"),
                                        fieldWithPath("body.total_pages").description("전체 페이지 수"),
                                        fieldWithPath("body.has_next").description("다음 페이지 존재 여부")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("작업실 채팅방 파일 삭제 API ")
    void deleteFile() throws Exception {
        // Given
        Long fileId = 1L;

        doNothing().when(chatFileService).deleteFile(anyLong(), anyLong());

        // When & Then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.delete("/api/v1/chats/files/{fileId}", fileId)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("chat-file-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅 파일")
                                .summary("파일 삭제")
                                .description("업로드된 파일을 Cloudflare R2 스토리지와 DB에서 완전히 삭제합니다. " +
                                        "삭제 후 해당 파일의 Presigned URL은 즉시 접근 불가능해집니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("fileId").description("삭제할 파일 ID")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional()
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("작업실 채팅방 파일 상세 조회 API (이미지 뷰어용)")
    void getFileDetail() throws Exception {
        // Given
        ChatFileDetailDto response = ChatFileDetailDto.builder()
                .fileId(1L)
                .fileName("photo.png")
                .fileUrl("https://r2.example.com/uuid_photo.png")
                .fileSize(1024L)
                .fileType("image/png")
                .createdAt(LocalDateTime.now())
                .build();

        given(chatFileService.getFileDetail(anyLong(), anyLong()))
                .willReturn(response);

        // When & Then
        mockMvc.perform(
                        get("/api/v1/chats/files/{fileId}", 1L)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.file_id").value(1L))
                .andExpect(jsonPath("$.body.file_name").value("photo.png"))
                .andDo(document("chat-file-detail",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅 파일")
                                .summary("파일 상세 조회 (이미지 뷰어용)")
                                .description("이미지 클릭 시 모달 뷰어에 표시할 파일 정보를 조회합니다. " +
                                        "파일 메타데이터와 함께 5분 유효한 Presigned URL이 반환됩니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("fileId").description("조회할 파일 ID")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),
                                        fieldWithPath("body.file_id").description("파일 ID"),
                                        fieldWithPath("body.file_name").description("원본 파일명"),
                                        fieldWithPath("body.file_url").description("이미지 표시용 Presigned URL (5분 유효)"),
                                        fieldWithPath("body.file_size").description("파일 크기 (bytes)"),
                                        fieldWithPath("body.file_type").description("파일 MIME 타입"),
                                        fieldWithPath("body.created_at").description("파일 업로드 시간")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("작업실 채팅방 파일 다운로드 API")
    void downloadFile() throws Exception {
        // Given
        String downloadUrl = "https://r2.example.com/uuid_photo.png?expires=300";

        given(chatFileService.getDownloadUrl(anyLong(), anyLong()))
                .willReturn(downloadUrl);

        // When & Then
        mockMvc.perform(
                        get("/api/v1/chats/files/{fileId}/download", 1L)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                )
                .andExpect(status().is3xxRedirection())
                .andDo(document("chat-file-download",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅 파일")
                                .summary("파일 다운로드")
                                .description("파일을 사용자 컴퓨터에 다운로드합니다. " +
                                        "Presigned URL로 리다이렉트되며, 브라우저가 파일 다운로드를 처리합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("fileId").description("다운로드할 파일 ID")
                                )
                                .build()
                        )
                ));
    }
}