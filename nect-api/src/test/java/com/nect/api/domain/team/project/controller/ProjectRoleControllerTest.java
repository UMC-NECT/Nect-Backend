package com.nect.api.domain.team.project.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.project.dto.ProjectPartsResDto;
import com.nect.api.domain.team.project.service.ProjectRoleQueryService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class ProjectRoleControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectRoleQueryService projectRoleQueryService;

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
    @DisplayName("작업실 전용 프로젝트 파트 목록 조회")
    void readProjectParts() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        ProjectPartsResDto response = new ProjectPartsResDto(
                List.of(
                        new ProjectPartsResDto.PartDto(
                                1L,
                                RoleField.BACKEND,
                                null,
                                "Backend",
                                2
                        ),
                        new ProjectPartsResDto.PartDto(
                                2L,
                                RoleField.CUSTOM,
                                "데이터",
                                "데이터",
                                1
                        )
                )
        );

        given(projectRoleQueryService.readProjectParts(eq(projectId), eq(userId)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/roles", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("project-role-read-parts",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Project")
                                        .summary("프로젝트 파트 목록 조회(작업실)")
                                        .description("작업실에서 프로젝트의 파트(역할) 목록을 조회합니다.")
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

                                                fieldWithPath("body").type(OBJECT).description("프로젝트 파트 목록 조회 결과"),
                                                fieldWithPath("body.parts").type(ARRAY).description("파트 목록"),

                                                fieldWithPath("body.parts[].part_id").type(NUMBER).description("파트 ID"),

                                                fieldWithPath("body.parts[].role_field").type(STRING).description("파트 타입(RoleField)"),
                                                fieldWithPath("body.parts[].custom_role_field_name").optional().type(STRING).description("CUSTOM 파트명"),
                                                fieldWithPath("body.parts[].part_label").type(STRING).description("표시 라벨(part_label)"),
                                                fieldWithPath("body.parts[].required_count").type(NUMBER).description("모집 인원")
                                        )
                                        .build()
                        )
                ));

        verify(projectRoleQueryService).readProjectParts(eq(projectId), eq(userId));
    }
}
