package com.nect.api.team.chat.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.chat.dto.req.ChatMessageDto;
import com.nect.api.domain.team.chat.dto.req.ChatNoticeUpdateRequestDto;
import com.nect.api.domain.team.chat.dto.res.ChatNoticeResponseDto;
import com.nect.api.domain.team.chat.dto.res.ChatRoomLeaveResponseDto;
import com.nect.api.domain.team.chat.dto.res.ChatRoomListDto;
import com.nect.api.domain.team.chat.service.ChatRoomService;
import com.nect.api.domain.team.chat.service.ChatService;
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
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class ChatMessageControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatRoomService chatRoomService;

    @MockitoBean
    private ChatService chatService;

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
    @DisplayName("채팅방 메시지 조회")
    void getChatMessages() throws Exception {
        List<ChatMessageDto> messages = Arrays.asList(
                ChatMessageDto.builder()
                        .messageId(1L)
                        .roomId(1L)
                        .userName("김민규")
                        .userId(1L)
                        .content("안녕하세요")
                        .messageType(MessageType.TEXT)
                        .isPinned(false)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        given(chatService.getChatMessages(anyLong(), any(), anyInt()))
                .willReturn(messages);

        mockMvc.perform(
                        get("/api/v1/chats/rooms/{room_id}/messages", 1L)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .param("lastMessage_id", "100")
                                .param("size", "20")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("chat-messages-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("채팅방 메시지 조회")
                                .description("특정 채팅방의 과거 메시지 내역을 페이징하여 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("room_id").description("채팅방 ID")
                                )
                                .queryParameters(
                                        parameterWithName("size").description("조회할 메시지 개수").optional(),
                                        parameterWithName("lastMessage_id").description("기준이 되는 마지막 메시지 ID (이보다 이전 메시지 조회)").optional()
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("내 채팅방 목록 조회")
    void getChatRooms() throws Exception {
        List<ChatRoomListDto> rooms = Arrays.asList(
                ChatRoomListDto.builder()
                        .room_id(1L)
                        .room_name("테스트 채팅방")
                        .last_message("마지막 메시지입니다.")
                        .last_message_time(LocalDateTime.now())
                        .has_new_message(true)
                        .profile_image("image.jpg")
                        .build()
        );

        given(chatRoomService.getMyChatRooms(anyLong()))
                .willReturn(rooms);

        mockMvc.perform(
                        get("/api/v1/chats/rooms")
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("chat-rooms-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("내 채팅방 목록 조회")
                                .description("현재 로그인한 사용자가 참여 중인 채팅방 목록을 반환합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("채팅방 나가기 API")
    void leaveChatRoom() throws Exception {
        Long roomId = 1L;
        Long userId = 1L;

        ChatRoomLeaveResponseDto response = ChatRoomLeaveResponseDto.builder()
                .roomId(roomId)
                .userId(userId)
                .userName("김민규")
                .message("채팅방을 나갔습니다.")
                .leftAt(LocalDateTime.now())
                .build();

        given(chatRoomService.leaveChatRoom(anyLong(), anyLong()))
                .willReturn(response);

        mockMvc.perform(
                        RestDocumentationRequestBuilders.delete("/api/v1/chats/{room_id}/leave", roomId)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("chat-room-leave",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("채팅방 나가기")
                                .description("사용자가 참여 중인 채팅방에서 퇴장합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("room_id").description("나갈 채팅방 ID")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),
                                        fieldWithPath("body.room_id").description("채팅방 ID"),
                                        fieldWithPath("body.user_id").description("유저 ID"),
                                        fieldWithPath("body.user_name").description("유저 이름"),
                                        fieldWithPath("body.message").description("퇴장 메시지"),
                                        fieldWithPath("body.left_at").description("퇴장 시간")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("공지사항 설정 API")
    void updateNotice() throws Exception {
        Long messageId = 100L;
        boolean isPinned = true;

        ChatNoticeUpdateRequestDto request = new ChatNoticeUpdateRequestDto();
        request.setIsPinned(isPinned);

        ChatNoticeResponseDto response = ChatNoticeResponseDto.builder()
                .messageId(messageId)
                .roomId(1L)
                .content("공지 내용입니다.")
                .messageType(MessageType.TEXT)
                .senderName("리더")
                .isPinned(isPinned)
                .registeredAt(LocalDateTime.now())
                .build();

        given(chatService.createNotice(eq(messageId), anyBoolean()))
                .willReturn(response);

        mockMvc.perform(
                        RestDocumentationRequestBuilders.patch("/api/v1/chats/message/{message_id}/notice", messageId)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andDo(document("chat-notice-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("메시지 공지 설정/해제")
                                .description("채팅방의 특정 메시지를 공지로 등록하거나 등록된 공지를 해제합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("message_id").description("대상이 되는 메시지 ID")
                                )
                                .requestFields(
                                        fieldWithPath("is_pinned").description("고정 여부 (true: 공지등록, false: 공지해제)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),
                                        fieldWithPath("body.message_id").description("메시지 ID"),
                                        fieldWithPath("body.room_id").description("채팅방 ID"),
                                        fieldWithPath("body.content").description("공지 내용"),
                                        fieldWithPath("body.message_type").description("메시지 타입"),
                                        fieldWithPath("body.sender_name").description("공지 등록자 이름"),
                                        fieldWithPath("body.is_pinned").description("현재 고정 상태"),
                                        fieldWithPath("body.registered_at").description("공지 등록/수정 시간")
                                )
                                .build()
                        )
                ));
    }
}