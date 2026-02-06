package com.nect.api.team.chat.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.chat.dto.req.*;
import com.nect.api.domain.team.chat.dto.res.*;
import com.nect.api.domain.team.chat.service.ChatRoomService;
import com.nect.api.domain.team.chat.service.ChatService;
import com.nect.api.domain.team.chat.service.TeamChatService;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
import com.nect.core.entity.team.chat.enums.ChatRoomType;
import com.nect.core.entity.team.chat.enums.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
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
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class ChatControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatRoomService chatRoomService;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private TeamChatService teamChatService;

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

    // ========== 1. 프로젝트 멤버 조회 ==========

    @Test
    @DisplayName("작업실 채팅 프로젝트 멤버 조회  API")
    void getProjectMembers() throws Exception {
        Long projectId = 1L;

        List<ProjectMemberDto> members = Arrays.asList(
                new ProjectMemberDto(2L, "손", "손흥민", "/images/default-profile.png"),
                new ProjectMemberDto(3L, "미누", "이민우", "/images/default-profile.png")
        );

        given(teamChatService.getProjectMembers(eq(projectId), eq(1L), isNull()))
                .willReturn(members);

        mockMvc.perform(
                        get("/api/v1/chats/projects/{projectId}/members", projectId)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body[0].user_id").value(2))
                .andExpect(jsonPath("$.body[0].nickname").value("손"))
                .andDo(document("project-members-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("작업실 채팅 프로젝트 멤버 조회  API")
                                .description("채팅방 생성 시 초대할 수 있는 프로젝트 멤버 목록을 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("projectId").description("프로젝트 ID")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),
                                        fieldWithPath("body[].user_id").description("사용자 ID"),
                                        fieldWithPath("body[].nickname").description("닉네임"),
                                        fieldWithPath("body[].name").description("이름"),
                                        fieldWithPath("body[].profile_image").description("프로필 이미지 URL")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("작업실 채팅방 생성 시 유저 검색 API")
    void getProjectMembersWithKeyword() throws Exception {
        Long projectId = 1L;
        String keyword = "손";

        List<ProjectMemberDto> members = Arrays.asList(
                new ProjectMemberDto(2L, "손", "손흥민", "/images/default-profile.png")
        );

        given(teamChatService.getProjectMembers(eq(projectId), eq(1L), eq(keyword)))
                .willReturn(members);

        mockMvc.perform(
                        get("/api/v1/chats/projects/{projectId}/members", projectId)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .param("keyword", keyword)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body[0].nickname").value("손"))
                .andDo(document("project-members-search",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("작업실 채팅방 생성 시 유저 검색 API")
                                .description("키워드로 프로젝트 멤버를 검색합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("projectId").description("프로젝트 ID")
                                )
                                .queryParameters(
                                        parameterWithName("keyword").description("검색 키워드")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),
                                        fieldWithPath("body[].user_id").description("사용자 ID"),
                                        fieldWithPath("body[].nickname").description("닉네임"),
                                        fieldWithPath("body[].name").description("이름"),
                                        fieldWithPath("body[].profile_image").description("프로필 이미지 URL")
                                )
                                .build()
                        )
                ));
    }

    // ========== 2. 채팅방 생성 ==========

    @Test
    @DisplayName("작업실 그룹 채팅방 생성 API")
    void createGroupChatRoom() throws Exception {
        GroupChatRoomCreateRequestDto request = new GroupChatRoomCreateRequestDto();
        request.setProjectId(1L);
        request.setRoomName("개발팀 채팅방");
        request.setTargetUserIds(Arrays.asList(2L, 3L, 4L));

        ChatRoomResponseDto response = ChatRoomResponseDto.builder()
                .roomId(10L)
                .projectId(1L)
                .roomName("개발팀 채팅방")
                .roomType(ChatRoomType.GROUP)
                .profileImages(Arrays.asList(
                        "https://example.com/profile1.jpg",
                        "https://example.com/profile2.jpg"
                ))
                .createdAt(LocalDateTime.now())
                .build();

        given(teamChatService.createGroupChatRoom(eq(1L), any(GroupChatRoomCreateRequestDto.class)))
                .willReturn(response);

        mockMvc.perform(
                        post("/api/v1/chats/rooms/group")
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.room_id").value(10))
                .andExpect(jsonPath("$.body.room_name").value("개발팀 채팅방"))
                .andDo(document("chat-room-create-group",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("작업실 그룹 채팅방 생성 API")
                                .description("프로젝트 멤버들과 그룹 채팅방을 생성합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .requestFields(
                                        fieldWithPath("project_id").description("프로젝트 ID"),
                                        fieldWithPath("room_name").description("채팅방 이름 (1~30자)"),
                                        fieldWithPath("target_user_ids").description("초대할 멤버 ID 목록")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),
                                        fieldWithPath("body.room_id").description("생성된 채팅방 ID"),
                                        fieldWithPath("body.room_name").description("채팅방 이름"),
                                        fieldWithPath("body.room_type").description("채팅방 타입 (GROUP)"),
                                        fieldWithPath("body.project_id").description("프로젝트 ID").optional(),
                                        fieldWithPath("body.profile_images").description("참여자 프로필 이미지 목록 (최대 4개)").optional(),
                                        fieldWithPath("body.created_at").description("생성 시간")
                                )
                                .build()
                        )
                ));
    }

    // ========== 3. 채팅방 목록 조회 ==========

    @Test
    @DisplayName("작업실 채팅방 목록 조회 API")
    void getProjectChatRooms() throws Exception {
        Long projectId = 1L;

        List<ChatRoomListDto> rooms = Arrays.asList(
                new ChatRoomListDto(1L, "개발팀", 4, "안녕하세요", LocalDateTime.now(), null, true),
                new ChatRoomListDto(2L, "디자인팀", 3, "확인했습니다", LocalDateTime.now(), null, false)
        );

        given(chatRoomService.getProjectChatRooms(eq(projectId), eq(1L)))
                .willReturn(rooms);

        mockMvc.perform(
                        get("/api/v1/chats/projects/{projectId}/rooms", projectId)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body[0].room_name").value("개발팀"))
                .andDo(document("chat-rooms-list-by-project",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("작업실 채팅방 목록 조회 API")
                                .description("특정 프로젝트의 채팅방 목록을 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("projectId").description("프로젝트 ID")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),
                                        fieldWithPath("body[].room_id").description("채팅방 ID"),
                                        fieldWithPath("body[].room_name").description("채팅방 이름"),
                                        fieldWithPath("body[].member_count").description("멤버 수"),
                                        fieldWithPath("body[].last_message").description("마지막 메시지"),
                                        fieldWithPath("body[].last_message_time").description("마지막 메시지 시간"),
                                        fieldWithPath("body[].profile_images").description("프로필 이미지 목록").optional(),
                                        fieldWithPath("body[].has_new_message").description("새 메시지 여부")
                                )
                                .build()
                        )
                ));
    }

    // ========== 4. 메시지 조회 ==========

    @Test
    @DisplayName("작업실 채팅방 내부 조회 API")
    void getChatMessages() throws Exception {
        Long roomId = 1L;

        List<ChatMessageDto> messages = Arrays.asList(
                ChatMessageDto.builder()
                        .messageId(1L)
                        .roomId(roomId)
                        .userId(2L)
                        .userName("손")
                        .content("안녕하세요")
                        .messageType(MessageType.TEXT)
                        .isPinned(false)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        ChatRoomMessagesResponseDto response = ChatRoomMessagesResponseDto.builder()
                .roomId(roomId)
                .roomName("개발팀")
                .memberCount(4)
                .messages(messages)
                .hasNext(false)
                .build();

        given(chatService.getChatMessages(eq(roomId), eq(1L), isNull(), eq(20)))
                .willReturn(response);

        mockMvc.perform(
                        get("/api/v1/chats/rooms/{room_id}/messages", roomId)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .param("size", "20")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.room_name").value("개발팀"))
                .andExpect(jsonPath("$.body.member_count").value(4))
                .andDo(document("chat-messages-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("작업실 채팅방 내부 조회 API")
                                .description("채팅방 정보와 메시지 목록을 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("room_id").description("채팅방 ID")
                                )
                                .queryParameters(
                                        parameterWithName("size").description("조회할 메시지 개수").optional(),
                                        parameterWithName("lastMessage_id").description("마지막 메시지 ID (이전 메시지 조회)").optional()
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),
                                        fieldWithPath("body.room_id").description("채팅방 ID"),
                                        fieldWithPath("body.room_name").description("채팅방 이름"),
                                        fieldWithPath("body.member_count").description("멤버 수"),
                                        fieldWithPath("body.has_next").description("다음 페이지 존재 여부"),
                                        fieldWithPath("body.messages[]").description("메시지 목록"),
                                        fieldWithPath("body.messages[].message_id").description("메시지 ID"),
                                        fieldWithPath("body.messages[].room_id").description("채팅방 ID"),
                                        fieldWithPath("body.messages[].user_id").description("발신자 ID"),
                                        fieldWithPath("body.messages[].user_name").description("발신자 이름"),
                                        fieldWithPath("body.messages[].profile_image").description("프로필 이미지").optional(),
                                        fieldWithPath("body.messages[].content").description("메시지 내용"),
                                        fieldWithPath("body.messages[].message_type").description("메시지 타입"),
                                        fieldWithPath("body.messages[].is_pinned").description("공지 여부"),
                                        fieldWithPath("body.messages[].created_at").description("생성 시간"),
                                        fieldWithPath("body.messages[].read_count").description("읽음 수").optional(),
                                        fieldWithPath("body.messages[].file_info").description("파일 정보").optional()
                                )
                                .build()
                        )
                ));
    }

    // ========== 5. 메시지 검색 ==========

    @Test
    @DisplayName("작업실 채팅방 내부 메시지 검색 API")
    void searchMessages() throws Exception {
        Long roomId = 1L;
        String keyword = "API";

        List<ChatMessageDto> messages = Arrays.asList(
                ChatMessageDto.builder()
                        .messageId(123L)
                        .roomId(roomId)
                        .userId(2L)
                        .userName("손")
                        .content("API 명세서 확인해주세요")
                        .messageType(MessageType.TEXT)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        ChatMessageSearchResponseDto response = ChatMessageSearchResponseDto.builder()
                .keyword(keyword)
                .totalCount(1)
                .messages(messages)
                .build();

        given(chatService.searchMessages(eq(roomId), eq(1L), eq(keyword), eq(0), eq(20)))
                .willReturn(response);

        mockMvc.perform(
                        get("/api/v1/chats/rooms/{room_id}/messages/search", roomId)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .param("keyword", keyword)
                                .param("page", "0")
                                .param("size", "20")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.keyword").value(keyword))
                .andExpect(jsonPath("$.body.total_count").value(1))
                .andDo(document("chat-messages-search",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("작업실 채팅방 내부 메시지 검색 API")
                                .description("채팅방 내에서 텍스트 메시지를 검색합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("room_id").description("채팅방 ID")
                                )
                                .queryParameters(
                                        parameterWithName("keyword").description("검색 키워드 (2자 이상)"),
                                        parameterWithName("page").description("페이지 번호").optional(),
                                        parameterWithName("size").description("페이지 크기").optional()
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),
                                        fieldWithPath("body.keyword").description("검색 키워드"),
                                        fieldWithPath("body.total_count").description("총 검색 결과 수"),
                                        fieldWithPath("body.messages[]").description("검색된 메시지 목록"),
                                        fieldWithPath("body.messages[].message_id").description("메시지 ID"),
                                        fieldWithPath("body.messages[].room_id").description("채팅방 ID"),
                                        fieldWithPath("body.messages[].user_id").description("발신자 ID"),
                                        fieldWithPath("body.messages[].user_name").description("발신자 이름"),
                                        fieldWithPath("body.messages[].profile_image").description("프로필 이미지").optional(),
                                        fieldWithPath("body.messages[].content").description("메시지 내용"),
                                        fieldWithPath("body.messages[].message_type").description("메시지 타입"),
                                        fieldWithPath("body.messages[].is_pinned").description("공지 여부").optional(),
                                        fieldWithPath("body.messages[].created_at").description("생성 시간"),
                                        fieldWithPath("body.messages[].read_count").description("읽음 수").optional(),
                                        fieldWithPath("body.messages[].file_info").description("파일 정보").optional()
                                )
                                .build()
                        )
                ));
    }

    // ========== 6. 공지사항 ==========

    @Test
    @DisplayName(" 작업실 채팅방 공지사항 등록 API")
    void updateNotice() throws Exception {
        Long messageId = 123L;

        ChatNoticeUpdateRequestDto request = new ChatNoticeUpdateRequestDto();
        request.setIsPinned(true);

        ChatNoticeResponseDto response = ChatNoticeResponseDto.builder()
                .messageId(messageId)
                .roomId(1L)
                .userId(2L)
                .senderName("손")
                .content("중요한 공지사항입니다")
                .messageType(MessageType.TEXT)
                .isPinned(true)
                .registeredAt(LocalDateTime.now())
                .build();

        given(chatService.createNotice(eq(messageId), eq(true), eq(1L)))
                .willReturn(response);

        mockMvc.perform(
                        patch("/api/v1/chats/message/{message_id}/notice", messageId)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.is_pinned").value(true))
                .andDo(document("chat-notice-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("작업실 채팅방 공지사항 등록 API")
                                .description("메시지를 공지사항으로 등록하거나 해제합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("message_id").description("메시지 ID")
                                )
                                .requestFields(
                                        fieldWithPath("is_pinned").description("공지 등록 여부 (true: 등록, false: 해제)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),
                                        fieldWithPath("body.message_id").description("메시지 ID"),
                                        fieldWithPath("body.room_id").description("채팅방 ID"),
                                        fieldWithPath("body.user_id").description("작성자 ID"),
                                        fieldWithPath("body.sender_name").description("작성자 이름"),
                                        fieldWithPath("body.content").description("메시지 내용"),
                                        fieldWithPath("body.message_type").description("메시지 타입"),
                                        fieldWithPath("body.is_pinned").description("공지 여부"),
                                        fieldWithPath("body.registered_at").description("등록 시간")
                                )
                                .build()
                        )
                ));
    }

    // ========== 7. 채팅방 나가기 ==========

    @Test
    @DisplayName("작업실 채팅방 나가기 API")
    void leaveChatRoom() throws Exception {
        Long roomId = 1L;

        ChatRoomLeaveResponseDto response = ChatRoomLeaveResponseDto.builder()
                .roomId(roomId)
                .userId(1L)
                .userName("김민규")
                .message("채팅방을 나갔습니다.")
                .leftAt(LocalDateTime.now())
                .build();

        given(chatRoomService.leaveChatRoom(eq(roomId), eq(1L)))
                .willReturn(response);

        mockMvc.perform(
                        RestDocumentationRequestBuilders.delete("/api/v1/chats/{room_id}/leave", roomId)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.room_id").value(roomId))
                .andExpect(jsonPath("$.body.user_name").value("김민규"))
                .andDo(document("chat-room-leave",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("작업실 채팅방 나가기 API")
                                .description("채팅방을 나갑니다. 나가기 메시지가 전송됩니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("room_id").description("채팅방 ID")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),
                                        fieldWithPath("body.room_id").description("채팅방 ID"),
                                        fieldWithPath("body.user_id").description("나간 사용자 ID"),
                                        fieldWithPath("body.user_name").description("나간 사용자 이름"),
                                        fieldWithPath("body.message").description("결과 메시지"),
                                        fieldWithPath("body.left_at").description("나간 시간")
                                )
                                .build()
                        )
                ));
    }

    // ========== 8. 멤버 초대 ==========

    @Test
    @DisplayName(" 작업실 채팅방 멤버 초대 API")
    void inviteMembers() throws Exception {
        Long roomId = 1L;

        ChatRoomInviteRequestDto request = new ChatRoomInviteRequestDto(
                Arrays.asList(5L, 6L)
        );

        ChatRoomInviteResponseDto response = new ChatRoomInviteResponseDto(
                roomId,
                2,
                Arrays.asList("새멤버1", "새멤버2"),
                Arrays.asList(
                        "https://example.com/profile5.jpg",
                        "https://example.com/profile6.jpg"
                )
        );

        given(teamChatService.inviteMembers(eq(roomId), eq(1L), any(ChatRoomInviteRequestDto.class)))
                .willReturn(response);

        mockMvc.perform(
                        post("/api/v1/chats/rooms/{roomId}/invite", roomId)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.invited_count").value(2))
                .andDo(document("chat-room-invite",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("작업실 채팅방 멤버 초대 API")
                                .description("채팅방에 새로운 멤버를 초대합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("roomId").description("채팅방 ID")
                                )
                                .requestFields(
                                        fieldWithPath("targetUserIds").description("초대할 사용자 ID 목록")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),
                                        fieldWithPath("body.room_id").description("채팅방 ID"),
                                        fieldWithPath("body.invited_count").description("실제 초대된 인원 수"),
                                        fieldWithPath("body.invited_user_names").description("초대된 사용자 이름 목록"),
                                        fieldWithPath("body.profile_images").description("초대된 사용자 프로필 이미지 목록")
                                )
                                .build()
                        )
                ));
    }
}