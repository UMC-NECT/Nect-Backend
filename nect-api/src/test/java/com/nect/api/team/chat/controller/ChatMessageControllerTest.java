package com.nect.api.team.chat.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.chat.dto.req.ChatMessageDTO;
import com.nect.api.domain.team.chat.dto.req.ChatNoticeUpdateRequestDTO;
import com.nect.api.domain.team.chat.dto.res.ChatNoticeResponseDTO;
import com.nect.api.domain.team.chat.dto.res.ChatRoomLeaveResponseDTO;
import com.nect.api.domain.team.chat.dto.res.ChatRoomListDTO;
import com.nect.api.domain.team.chat.service.ChatRoomService;
import com.nect.api.domain.team.chat.service.ChatService;
import com.nect.core.entity.team.chat.enums.MessageType;
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
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
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

    @Test
    @DisplayName("채팅방 메시지 조회")
    void getChatMessages() throws Exception {

        List<ChatMessageDTO> messages = Arrays.asList(
                ChatMessageDTO.builder()
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



        mockMvc.perform(get("/chats/rooms/{room_id}/messages", 1L)
                        .param("lastMessage_id", "100")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andDo(document("chat-messages-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("채팅방 메시지 조회")
                                .description("채팅방 메시지를 조회합니다")
                                .pathParameters(
                                        parameterWithName("room_id").description("채팅방 ID")
                                )
                                .queryParameters(
                                        parameterWithName("size").description("조회할 메시지 개수").optional(),
                                        parameterWithName("lastMessage_id").description("마지막 메시지 ID").optional()
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("내 채팅방 목록 조회")
    void getChatRooms() throws Exception {

        List<ChatRoomListDTO> rooms = Arrays.asList(
                ChatRoomListDTO.builder()
                        .room_id(1L)
                        .room_name("채팅방")
                        .last_message("메시지")
                        .last_message_time(LocalDateTime.now())
                        .has_new_message(true)
                        .profile_image("image.jpg")
                        .build()
        );

        given(chatRoomService.getMyChatRooms(anyLong()))
                .willReturn(rooms);


        mockMvc.perform(get("/chats/rooms")
                        .param("user_id", "1"))
                .andExpect(status().isOk())
                .andDo(document("chat-rooms-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅방 목록 조회")
                                .summary("내 채팅방 목록 조회")
                                .description("내가 속한 채팅방 목록을 조회합니다")
                                .queryParameters(
                                        parameterWithName("user_id").description("사용자 ID")
                                )
                                .build()
                        )
                ));
    }



    @Test
    @DisplayName("채팅방 나가기 API 테스트")
    void leaveChatRoom() throws Exception {
        // given
        Long roomId = 1L;
        Long userId = 1L;

        ChatRoomLeaveResponseDTO response = ChatRoomLeaveResponseDTO.builder()
                .roomId(roomId)
                .userId(userId)
                .userName("TestUser")
                .message("채팅방을 나갔습니다.")
                .leftAt(LocalDateTime.now())
                .build();

        given(chatRoomService.leaveChatRoom(anyLong(), anyLong()))
                .willReturn(response);


        mockMvc.perform(RestDocumentationRequestBuilders.delete("/chats/{room_id}/leave", roomId)
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("chat-room-leave",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("채팅방 나가기")
                                .description("사용자가 해당 채팅방에서 나갑니다.")
                                .pathParameters(
                                        parameterWithName("room_id").description("나갈 채팅방 ID")
                                )
                                .queryParameters(
                                        parameterWithName("userId").description("나가는 유저 ID (임시)").optional()
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
    @DisplayName("채팅방 공지사항 등록/수정 API 테스트")
    void updateNotice() throws Exception {
        // given
        Long messageId = 100L;
        boolean isPinned = true;

        // Request DTO
        ChatNoticeUpdateRequestDTO request = new ChatNoticeUpdateRequestDTO();
        try {
            java.lang.reflect.Field field = request.getClass().getDeclaredField("isPinned");
            field.setAccessible(true);
            field.set(request, isPinned);
        } catch (Exception e) {
        }


        ChatNoticeResponseDTO response = ChatNoticeResponseDTO.builder()
                .messageId(messageId)
                .roomId(1L)
                .content("이것은 공지사항입니다.")
                .messageType(MessageType.TEXT)
                .senderName("Manager")
                .isPinned(isPinned)
                .registeredAt(LocalDateTime.now())
                .build();

        given(chatService.createNotice(eq(messageId), anyBoolean()))
                .willReturn(response);


        mockMvc.perform(RestDocumentationRequestBuilders.patch("/chats/message/{message_id}/notice", messageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("chat-notice-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅")
                                .summary("공지사항 등록/해제")
                                .description("특정 메시지를 공지사항으로 등록하거나 해제합니다.")
                                .pathParameters(
                                        parameterWithName("message_id").description("공지로 등록할 메시지 ID")
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
                                        fieldWithPath("body.content").description("메시지 내용"),
                                        fieldWithPath("body.message_type").description("메시지 타입"),
                                        fieldWithPath("body.sender_name").description("보낸 사람 이름"),
                                        fieldWithPath("body.is_pinned").description("공지 등록 여부"),
                                        fieldWithPath("body.registered_at").description("등록 시간")
                                )
                                .build()
                        )
                ));
    }

}