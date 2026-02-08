package com.nect.api.domain.team.project.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.project.dto.ProjectPartCreateReqDto;
import com.nect.api.domain.team.project.dto.ProjectPartCreateResDto;
import com.nect.api.domain.team.project.dto.ProjectPartUpdateReqDto;
import com.nect.api.domain.team.project.dto.ProjectPartUpdateResDto;
import com.nect.api.domain.team.project.service.ProjectTeamCommandService;
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
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class ProjectPartsCommandControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectTeamCommandService projectTeamCommandService;

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
    @DisplayName("프로젝트 파트 추가 (작업실 팀 레인)")
    void createProjectPart() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        ProjectPartCreateReqDto req = new ProjectPartCreateReqDto(
                RoleField.CUSTOM,
                "기획",
                1
        );

        ProjectPartCreateResDto res = new ProjectPartCreateResDto(
                10L,
                RoleField.CUSTOM,
                "기획",
                "기획",
                1
        );

        given(projectTeamCommandService.createProjectPart(eq(projectId), eq(userId), any(ProjectPartCreateReqDto.class)))
                .willReturn(res);

        mockMvc.perform(post("/api/v1/projects/{projectId}/parts", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andDo(document("project-parts-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Project")
                                        .summary("팀 파트 추가")
                                        .description("작업실(위크미션/파트별 작업현황 등)에서 사용할 프로젝트 파트를 추가합니다.")
                                        .pathParameters(
                                                parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("role_field").type(STRING).description("파트 타입(RoleField). 현재는 CUSTOM 주로 사용"),
                                                fieldWithPath("custom_role_field_name").type(STRING).optional()
                                                        .description("CUSTOM 파트명(직접 입력)"),
                                                fieldWithPath("required_count").type(NUMBER).optional()
                                                        .description("필요 인원(기본 1)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("프로젝트 파트 추가 결과"),
                                                fieldWithPath("body.part_id").type(NUMBER).description("추가된 파트 ID"),
                                                fieldWithPath("body.role_field").type(STRING).description("파트 타입(RoleField)"),
                                                fieldWithPath("body.custom_role_field_name").type(STRING).optional().description("CUSTOM 파트명"),
                                                fieldWithPath("body.part_label").type(STRING).description("표시 라벨(=CUSTOM이면 custom name, 아니면 enum label)"),
                                                fieldWithPath("body.required_count").type(NUMBER).description("필요 인원")
                                        )
                                        .build()
                        )
                ));

        verify(projectTeamCommandService).createProjectPart(eq(projectId), eq(userId), any(ProjectPartCreateReqDto.class));
    }

    @Test
    @DisplayName("프로젝트 파트 수정 (CUSTOM 이름/필요 인원)")
    void updateProjectPart() throws Exception {
        long projectId = 1L;
        long partId = 10L;
        long userId = 1L;

        ProjectPartUpdateReqDto req = new ProjectPartUpdateReqDto(
                "데이터",
                2
        );

        ProjectPartUpdateResDto res = new ProjectPartUpdateResDto(
                partId,
                RoleField.CUSTOM,
                "데이터",
                "데이터",
                2
        );

        given(projectTeamCommandService.updateProjectPart(eq(projectId), eq(partId), eq(userId), any(ProjectPartUpdateReqDto.class)))
                .willReturn(res);

        mockMvc.perform(patch("/api/v1/projects/{projectId}/parts/{partId}", projectId, partId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andDo(document("project-parts-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Project")
                                        .summary("팀 파트 수정")
                                        .description("프로젝트 파트의 CUSTOM 이름 및 필요 인원을 수정합니다. (role_field 자체는 수정 불가)")
                                        .pathParameters(
                                                parameterWithName("projectId").description("프로젝트 ID"),
                                                parameterWithName("partId").description("파트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("custom_role_field_name").type(STRING).optional().description("CUSTOM 파트명(직접 입력)"),
                                                fieldWithPath("required_count").type(NUMBER).optional().description("필요 인원(>=1)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("프로젝트 파트 수정 결과"),
                                                fieldWithPath("body.part_id").type(NUMBER).description("수정된 파트 ID"),
                                                fieldWithPath("body.role_field").type(STRING).description("파트 타입(RoleField)"),
                                                fieldWithPath("body.custom_role_field_name").type(STRING).optional().description("CUSTOM 파트명"),
                                                fieldWithPath("body.part_label").type(STRING).description("표시 라벨(=CUSTOM이면 custom name, 아니면 enum label)"),
                                                fieldWithPath("body.required_count").type(NUMBER).description("필요 인원")
                                        )
                                        .build()
                        )
                ));

        verify(projectTeamCommandService).updateProjectPart(eq(projectId), eq(partId), eq(userId), any(ProjectPartUpdateReqDto.class));
    }
}
