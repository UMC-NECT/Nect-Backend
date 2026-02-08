package com.nect.api.domain.mypage.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.mypage.dto.*;
import com.nect.api.domain.mypage.service.UserTeamRoleQueryService;
import com.nect.api.domain.mypage.service.UserTeamRoleService;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
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
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class UserTeamRoleControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserTeamRoleService userTeamRoleService;

    @MockitoBean
    private UserTeamRoleQueryService userTeamRoleQueryService;

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
    @DisplayName("마이페이지 팀 파트 생성(추가)")
    void createUserTeamRole() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        UserTeamRoleCreateReqDto request = new UserTeamRoleCreateReqDto(
                RoleField.CUSTOM,
                "데이터",
                2
        );

        UserTeamRoleCreateResDto response = new UserTeamRoleCreateResDto(
                10L,
                RoleField.CUSTOM,
                "데이터",
                "데이터",
                2
        );

        given(userTeamRoleService.create(eq(projectId), eq(userId), any(UserTeamRoleCreateReqDto.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/mypage/projects/{projectId}/team-roles", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("mypage-team-role-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("MyPage")
                                        .summary("마이페이지 팀 파트 생성")
                                        .description("마이페이지에서 프로젝트별 팀 파트를 생성합니다. (리더만 가능)")
                                        .pathParameters(
                                                parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("role_field").type(STRING).description("파트 타입(RoleField)"),
                                                fieldWithPath("custom_role_field_name").optional().type(STRING).description("CUSTOM 파트명(직접 입력)"),
                                                fieldWithPath("required_count").optional().type(NUMBER).description("모집 인원(기본 1)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("마이페이지 팀 파트 생성 결과"),
                                                fieldWithPath("body.team_role_id").type(NUMBER).description("생성된 팀 파트 ID"),
                                                fieldWithPath("body.role_field").type(STRING).description("파트 타입(RoleField)"),
                                                fieldWithPath("body.custom_role_field_name").optional().type(STRING).description("CUSTOM 파트명"),
                                                fieldWithPath("body.part_label").type(STRING).description("표시 라벨(part_label)"),
                                                fieldWithPath("body.required_count").type(NUMBER).description("모집 인원")
                                        )
                                        .build()
                        )
                ));

        verify(userTeamRoleService).create(eq(projectId), eq(userId), any(UserTeamRoleCreateReqDto.class));
    }

    @Test
    @DisplayName("마이페이지 팀 파트 수정(CUSTOM만 가능)")
    void updateUserTeamRole() throws Exception {
        long projectId = 1L;
        long userTeamRoleId = 10L;
        long userId = 1L;

        UserTeamRoleUpdateReqDto request = new UserTeamRoleUpdateReqDto(
                "데이터분석",
                3
        );

        UserTeamRoleUpdateResDto response = new UserTeamRoleUpdateResDto(
                userTeamRoleId,
                RoleField.CUSTOM,
                "데이터분석",
                "데이터분석",
                3
        );

        given(userTeamRoleService.update(eq(projectId), eq(userId), eq(userTeamRoleId), any(UserTeamRoleUpdateReqDto.class)))
                .willReturn(response);

        mockMvc.perform(patch("/api/v1/mypage/projects/{projectId}/team-roles/{userTeamRoleId}", projectId, userTeamRoleId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("mypage-team-role-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("MyPage")
                                        .summary("마이페이지 팀 파트 수정")
                                        .description("마이페이지에서 프로젝트별 팀 파트를 수정합니다. (CUSTOM만 가능, 리더만 가능)")
                                        .pathParameters(
                                                parameterWithName("projectId").description("프로젝트 ID"),
                                                parameterWithName("userTeamRoleId").description("마이페이지 팀 파트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("custom_role_field_name").optional().type(STRING).description("CUSTOM 파트명(직접 입력)"),
                                                fieldWithPath("required_count").optional().type(NUMBER).description("모집 인원(1 이상)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("마이페이지 팀 파트 수정 결과"),
                                                fieldWithPath("body.user_team_role_id").type(NUMBER).description("수정된 팀 파트 ID"),
                                                fieldWithPath("body.role_field").type(STRING).description("파트 타입(RoleField)"),
                                                fieldWithPath("body.custom_role_field_name").optional().type(STRING).description("CUSTOM 파트명"),
                                                fieldWithPath("body.part_label").type(STRING).description("표시 라벨(part_label)"),
                                                fieldWithPath("body.required_count").type(NUMBER).description("모집 인원")
                                        )
                                        .build()
                        )
                ));
        verify(userTeamRoleService).update(eq(projectId), eq(userId), eq(userTeamRoleId), any(UserTeamRoleUpdateReqDto.class));
    }

    @Test
    @DisplayName("마이페이지 파트 목록 조회")
    void readMyPageParts() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        UserTeamRolesResDto response = new UserTeamRolesResDto(List.of(
                new UserTeamRolesResDto.PartDto(
                        10L,
                        RoleField.BACKEND,
                        null,
                        "Backend",
                        1
                ),
                new UserTeamRolesResDto.PartDto(
                        11L,
                        RoleField.CUSTOM,
                        "데이터",
                        "데이터",
                        2
                )
        ));

        given(userTeamRoleQueryService.readMyPageParts(eq(projectId), eq(userId)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/mypage/projects/{projectId}/team-roles", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("mypage-team-role-read",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("MyPage")
                                        .summary("마이페이지 파트 목록 조회")
                                        .description("마이페이지에서 프로젝트별 팀 파트(칩) 목록을 조회합니다. (프로젝트 ACTIVE 멤버 가능)")
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

                                                fieldWithPath("body").type(OBJECT).description("마이페이지 파트 목록 조회 결과"),
                                                fieldWithPath("body.parts").type(ARRAY).description("파트 목록"),
                                                fieldWithPath("body.parts[].id").type(NUMBER).description("마이페이지 팀 파트 ID"),
                                                fieldWithPath("body.parts[].role_field").type(STRING).description("파트 타입(RoleField)"),
                                                fieldWithPath("body.parts[].custom_role_field_name").optional().type(STRING).description("CUSTOM 파트명"),
                                                fieldWithPath("body.parts[].label").type(STRING).description("표시 라벨(label)"),
                                                fieldWithPath("body.parts[].required_count").type(NUMBER).description("모집 인원")
                                        )
                                        .build()
                        )
                ));

        verify(userTeamRoleQueryService).readMyPageParts(eq(projectId), eq(userId));
    }
}
