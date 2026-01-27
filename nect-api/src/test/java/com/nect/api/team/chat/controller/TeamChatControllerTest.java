package com.nect.api.team.chat.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.chat.dto.req.ChatRoomCreateRequestDTO;
import com.nect.api.domain.team.chat.dto.req.GroupChatRoomCreateRequestDTO;
import com.nect.api.domain.team.chat.dto.res.ChatRoomResponseDTO;
import com.nect.api.domain.team.chat.dto.res.ProjectMemberResponseDTO;
import com.nect.api.domain.team.chat.service.TeamChatService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.core.entity.team.chat.enums.ChatRoomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
public class TeamChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TeamChatService teamChatService;

    @Test
    @DisplayName("프로젝트 팀원 조회")
    void getProjectMembers() throws Exception {
        List<ProjectMemberResponseDTO> users = Arrays.asList(
                ProjectMemberResponseDTO.builder()
                        .userId(2L)
                        .username("김민규")
                        .build()
        );

        given(teamChatService.getProjectMembers(anyLong()))
                .willReturn(users);

        mockMvc.perform(
                        get("/chats/rooms/{projectId}/users", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(UserDetailsImpl.builder().userId(1L).roles(List.of("ROLE_USER")).build()))
                )
                .andExpect(status().isOk())
                .andDo(document("project-members-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅방")
                                .summary("프로젝트 팀원 조회")
                                .description("특정 프로젝트에 속한 팀원 목록을 조회합니다.")
                                .pathParameters(
                                        parameterWithName("projectId").description("프로젝트 ID")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("응답 코드"),
                                        fieldWithPath("status.message").description("응답 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),
                                        fieldWithPath("body[]").description("팀원 리스트"),
                                        fieldWithPath("body[].user_id").description("팀원 유저 ID"),
                                        fieldWithPath("body[].username").description("팀원 이름"),
                                        fieldWithPath("body[].profile_image")
                                                .type(JsonFieldType.STRING)
                                                .description("프로필 이미지 URL")
                                                .optional()
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("1:1 채팅방 생성")
    void createPersonalChatRoom() throws Exception {
        ChatRoomCreateRequestDTO request = new ChatRoomCreateRequestDTO();
        request.setProject_id(1L);
        request.setTarget_user_id(2L);

        ChatRoomResponseDTO response = ChatRoomResponseDTO.builder()
                .roomId(10L)
                .projectId(1L)
                .roomName("김민규")
                .roomType(ChatRoomType.DIRECT)
                .createdAt(LocalDateTime.now())
                .build();

        given(teamChatService.createOneOnOneChatRoom(anyLong(), any(ChatRoomCreateRequestDTO.class)))
                .willReturn(response);

        mockMvc.perform(
                        post("/chats/rooms/personal")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user(UserDetailsImpl.builder().userId(1L).roles(List.of("ROLE_USER")).build()))
                )
                .andExpect(status().isOk())
                .andDo(document("chat-room-create-personal",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅방")
                                .summary("1:1 채팅방 생성")
                                .description("동일 프로젝트 팀원과 1:1 채팅방을 생성합니다. 이미 존재하면 기존 방을 반환합니다.")
                                .requestFields(
                                        fieldWithPath("project_id").description("프로젝트 ID"),
                                        fieldWithPath("target_user_id").description("상대방 유저 ID")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("응답 코드"),
                                        fieldWithPath("status.message").description("응답 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),
                                        fieldWithPath("body.room_id").description("채팅방 ID"),
                                        fieldWithPath("body.project_id").description("프로젝트 ID"),
                                        fieldWithPath("body.room_name").description("상대방 이름"),
                                        fieldWithPath("body.room_type").description("방 타입 (DIRECT)"),
                                        fieldWithPath("body.profile_image").description("상대방 프로필 이미지").optional(),
                                        fieldWithPath("body.created_at").description("생상 일시")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("그룹 채팅방 생성")
    void createGroupChatRoom() throws Exception {
        GroupChatRoomCreateRequestDTO request = new GroupChatRoomCreateRequestDTO();
        request.setProjectId(1L);
        request.setRoomName("백엔드 개발팀");
        request.setTargetUserIds(Arrays.asList(1L, 2L, 3L));

        ChatRoomResponseDTO response = ChatRoomResponseDTO.builder()
                .roomId(20L)
                .projectId(1L)
                .roomName("백엔드 개발팀")
                .roomType(ChatRoomType.GROUP)
                .createdAt(LocalDateTime.now())
                .build();

        given(teamChatService.createGroupChatRoom(anyLong(), any(GroupChatRoomCreateRequestDTO.class)))
                .willReturn(response);

        mockMvc.perform(
                        post("/chats/rooms/group")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user(UserDetailsImpl.builder().userId(1L).roles(List.of("ROLE_USER")).build()))
                )
                .andExpect(status().isOk())
                .andDo(document("chat-room-create-group",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("채팅방")
                                .summary("그룹 채팅방 생성")
                                .description("프로젝트 내 여러 팀원을 초대하여 그룹 채팅방을 생성합니다.")
                                .requestFields(
                                        fieldWithPath("project_id").description("프로젝트 ID"),
                                        fieldWithPath("room_name").description("채팅방 이름"),
                                        fieldWithPath("target_user_ids").description("초대 유저 ID 리스트")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("응답 코드"),
                                        fieldWithPath("status.message").description("응답 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),
                                        fieldWithPath("body.room_id").description("채팅방 ID"),
                                        fieldWithPath("body.project_id").description("프로젝트 ID"),
                                        fieldWithPath("body.room_name").description("채팅방 이름"),
                                        fieldWithPath("body.room_type").description("방 타입 (GROUP)"),
                                        fieldWithPath("body.profile_image").description("채팅방 이미지").optional(),
                                        fieldWithPath("body.created_at").description("생성 일시")
                                )
                                .build()
                        )
                ));
    }
}