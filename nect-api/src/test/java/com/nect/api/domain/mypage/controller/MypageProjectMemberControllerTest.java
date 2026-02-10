package com.nect.api.domain.mypage.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.mypage.dto.ReorderProjectMembersRequest;
import com.nect.api.domain.team.project.dto.ProjectUsersResDto;
import com.nect.api.domain.team.project.service.ProjectMemberQueryService;
import com.nect.api.domain.team.project.service.ProjectUserService;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.entity.user.enums.RoleField;
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

import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class MypageProjectMemberControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectMemberQueryService projectMemberQueryService;

    @MockitoBean
    private ProjectUserService projectUserService;

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
    @DisplayName("마이페이지 전용 프로젝트 유저 조회")
    void readProjectUsers() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        ProjectUsersResDto response = new ProjectUsersResDto(List.of(
                new ProjectUsersResDto.UserDto(
                        1L,
                        "홍길동",
                        "gildong",
                        "https://example.com/profile.png",
                        "bio",
                        RoleField.BACKEND,
                        null,
                        "Backend",
                        ProjectMemberType.LEADER
                ),
                new ProjectUsersResDto.UserDto(
                        2L,
                        "김철수",
                        "chulsoo",
                        null,
                        null,
                        RoleField.CUSTOM,
                        "데이터",
                        "데이터",
                        ProjectMemberType.MEMBER
                )
        ));

        given(projectMemberQueryService.readProjectUsers(eq(projectId), eq(userId)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/mypage/projects/{projectId}/users", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("mypage-project-users-read",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("MyPage")
                                        .summary("마이페이지 전용 프로젝트 유저 조회")
                                        .description("마이페이지에서 프로젝트별 유저(멤버) 목록을 조회합니다. (공용 멤버 조회 서비스 재사용)")
                                        .pathParameters(
                                                parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("프로젝트 유저 조회 결과"),
                                                fieldWithPath("body.users").type(ARRAY).description("유저 목록"),
                                                fieldWithPath("body.users[].user_id").type(NUMBER).description("유저 ID"),
                                                fieldWithPath("body.users[].name").type(STRING).description("이름"),
                                                fieldWithPath("body.users[].nickname").type(STRING).description("닉네임"),
                                                fieldWithPath("body.users[].profile_image_url").optional().type(STRING).description("프로필 이미지 URL(프리사인드 URL)"),
                                                fieldWithPath("body.users[].bio").optional().type(STRING).description("소개글(bio)"),

                                                fieldWithPath("body.users[].role_field").type(STRING).description("작업실 기준 파트(RoleField, ProjectUser 기준)"),
                                                fieldWithPath("body.users[].custom_role_field_name").optional().type(STRING).description("CUSTOM 파트명"),
                                                fieldWithPath("body.users[].part_label").type(STRING).description("표시 라벨(part_label)"),

                                                fieldWithPath("body.users[].member_type").type(STRING).description("프로젝트 멤버 타입(LEADER/MEMBER)")
                                        )
                                        .build()
                        )
                ));

        verify(projectMemberQueryService).readProjectUsers(eq(projectId), eq(userId));
    }

    @Test
    @DisplayName("프로젝트 유저 순서 재정렬")
    void reorderProjectUsers() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        String requestJson = """
                {
                  "updates": [
                    {
                      "roleField": "BACKEND",
                      "customRoleField": null,
                      "orderedUserIds": [101, 102, 103]
                    },
                    {
                      "roleField": "FRONTEND",
                      "customRoleField": null,
                      "orderedUserIds": [201, 202]
                    }
                  ]
                }
                """;

        doNothing().when(projectUserService).reorderProjectUsers(eq(projectId), any(ReorderProjectMembersRequest.class));

        mockMvc.perform(post("/api/v1/mypage/projects/{projectId}/users/reorder", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andDo(document("mypage-project-users-reorder",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("MyPage")
                                        .summary("프로젝트 유저 순서 재정렬")
                                        .description("프로젝트 멤버들의 정렬 순서를 지정합니다. userId를 보내주시면 됩니다.")
                                        .pathParameters(
                                                parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("updates").description("정렬 업데이트 목록"),
                                                fieldWithPath("updates[].roleField").description("파트 (RoleField)"),
                                                fieldWithPath("updates[].customRoleField").optional().description("커스텀 파트명 (CUSTOM일 때)"),
                                                fieldWithPath("updates[].orderedUserIds").description("정렬할 User ID 리스트 (순서대로)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").description("응답 상태"),
                                                fieldWithPath("status.statusCode").description("상태 코드"),
                                                fieldWithPath("status.message").description("상태 메시지"),
                                                fieldWithPath("status.description").optional().description("상세 설명")
                                        )
                                        .build()
                        )
                ));
    }
}
