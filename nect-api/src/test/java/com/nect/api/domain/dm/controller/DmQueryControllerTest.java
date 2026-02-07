package com.nect.api.domain.dm.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.domain.dm.dto.DirectMessageDto;
import com.nect.api.domain.dm.dto.DmMessageListResponse;
import com.nect.api.domain.dm.dto.DmRoomListResponse;
import com.nect.api.domain.dm.dto.DmRoomSummaryDto;
import com.nect.api.domain.dm.service.DmService;
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
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class DmQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DmService dmService;

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
    @DisplayName("DM 메시지 조회 API")
    void getMessages() throws Exception {
        given(dmService.getMessages(eq(1L), eq(2L), eq(100L), eq(20)))
                .willReturn(mockMessageListResponse());

        mockMvc.perform(get("/api/v1/dms/messages")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("userId", "2")
                        .param("cursor", "100")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("dm-messages",
                        resource(ResourceSnippetParameters.builder()
                                .tag("개인 메시지")
                                .summary("DM 메시지 조회")
                                .description("상대 유저와의 DM 메시지 목록을 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .queryParameters(
                                        parameterWithName("userId").description("상대 유저 ID"),
                                        parameterWithName("cursor").optional().description("커서 (마지막 메시지 ID)"),
                                        parameterWithName("size").optional().description("조회 개수 (기본 20)")
                                )
                                .responseFields(messageResponseFields())
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("DM 채팅방 목록 조회 API")
    void getRooms() throws Exception {
        given(dmService.getRooms(eq(1L), eq(200L), eq(20)))
                .willReturn(mockRoomListResponse());

        mockMvc.perform(get("/api/v1/dms/rooms")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("cursor", "200")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("dm-rooms",
                        resource(ResourceSnippetParameters.builder()
                                .tag("개인 메시지")
                                .summary("DM 채팅방 목록 조회")
                                .description("로그인 유저의 DM 채팅방 목록을 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .queryParameters(
                                        parameterWithName("cursor").optional().description("커서 (마지막 메시지 ID)"),
                                        parameterWithName("size").optional().description("조회 개수 (기본 20)")
                                )
                                .responseFields(roomResponseFields())
                                .build()
                        )
                ));
    }

    private DmMessageListResponse mockMessageListResponse() {
        List<DirectMessageDto> messages = List.of(
                new DirectMessageDto(
                        10L,
                        1L,
                        "홍길동",
                        "https://example.com/profile/1.png",
                        "안녕하세요!",
                        false,
                        LocalDateTime.of(2024, 1, 1, 12, 0, 0),
                        true
                ),
                new DirectMessageDto(
                        11L,
                        2L,
                        "김철수",
                        "https://example.com/profile/2.png",
                        "반가워요!",
                        false,
                        LocalDateTime.of(2024, 1, 1, 12, 1, 0),
                        false
                )
        );

        return DmMessageListResponse.builder()
                .messages(messages)
                .nextCursor(11L)
                .build();
    }

    private DmRoomListResponse mockRoomListResponse() {
        List<DmRoomSummaryDto> rooms = List.of(
                new DmRoomSummaryDto(
                        2L,
                        2L,
                        "https://example.com/profile/2.png",
                        "Backend",
                        20L,
                        "최근 메시지",
                        LocalDate.of(2024, 1, 2),
                        false
                )
        );

        return DmRoomListResponse.builder()
                .messages(rooms)
                .nextCursor(20L)
                .build();
    }

    private static List<FieldDescriptor> messageResponseFields() {
        return List.of(
                fieldWithPath("status.statusCode").description("응답 상태 코드"),
                fieldWithPath("status.message").description("응답 메시지"),
                fieldWithPath("status.description").optional().description("응답 상세 설명"),
                fieldWithPath("body.messages").description("DM 메시지 목록"),
                fieldWithPath("body.messages[].message_id").description("메시지 ID"),
                fieldWithPath("body.messages[].sender_id").description("보낸 유저 ID"),
                fieldWithPath("body.messages[].sender_name").description("보낸 유저 이름"),
                fieldWithPath("body.messages[].sender_profile_image").description("보낸 유저 프로필 이미지 URL"),
                fieldWithPath("body.messages[].content").description("메시지 내용"),
                fieldWithPath("body.messages[].is_pinned").description("고정 여부"),
                fieldWithPath("body.messages[].created_at").description("메시지 생성 시간"),
                fieldWithPath("body.messages[].is_read").description("읽음 여부"),
                fieldWithPath("body.next_cursor").description("다음 커서 (마지막 메시지 ID)").optional()
        );
    }

    private static List<FieldDescriptor> roomResponseFields() {
        return List.of(
                fieldWithPath("status.statusCode").description("응답 상태 코드"),
                fieldWithPath("status.message").description("응답 메시지"),
                fieldWithPath("status.description").optional().description("응답 상세 설명"),
                fieldWithPath("body.messages").description("DM 채팅방 목록"),
                fieldWithPath("body.messages[].other_user_id").description("상대 유저 ID"),
                fieldWithPath("body.messages[].other_user_name").description("상대 유저 이름"),
                fieldWithPath("body.messages[].other_user_image_url").description("상대 유저 프로필 이미지 URL"),
                fieldWithPath("body.messages[].other_user_role_field").description("상대 유저 역할"),
                fieldWithPath("body.messages[].last_message_id").description("마지막 메시지 ID"),
                fieldWithPath("body.messages[].last_message").description("마지막 메시지 내용"),
                fieldWithPath("body.messages[].last_message_at").description("마지막 메시지 일자"),
                fieldWithPath("body.messages[].is_read").description("읽음 여부"),
                fieldWithPath("body.nextCursor").description("다음 커서 (마지막 메시지 ID)").optional()
        );
    }
}
