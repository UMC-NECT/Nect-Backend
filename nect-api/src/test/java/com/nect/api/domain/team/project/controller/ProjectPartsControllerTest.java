package com.nect.api.domain.team.project.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.project.dto.ProjectPartsResDto;
import com.nect.api.domain.team.project.dto.ProjectUsersResDto;
import com.nect.api.domain.team.project.service.ProjectTeamQueryService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class ProjectPartsControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectTeamQueryService projectTeamQueryService;

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

    private <T> T newRecord(Class<T> recordType) {
        try {
            if (!recordType.isRecord()) return null;

            RecordComponent[] components = recordType.getRecordComponents();
            Class<?>[] paramTypes = new Class<?>[components.length];
            Object[] args = new Object[components.length];

            for (int i = 0; i < components.length; i++) {
                Class<?> t = components[i].getType();
                paramTypes[i] = t;
                args[i] = defaultValue(t);
            }

            Constructor<T> ctor = recordType.getDeclaredConstructor(paramTypes);
            ctor.setAccessible(true);
            return ctor.newInstance(args);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to instantiate record: " + recordType.getName(), e);
        }
    }

    private Object defaultValue(Class<?> t) {
        if (t == String.class) return "sample";
        if (t == Long.class || t == long.class) return 1L;
        if (t == Integer.class || t == int.class) return 1;
        if (t == Boolean.class || t == boolean.class) return false;
        if (t == LocalDate.class) return LocalDate.of(2026, 1, 19);
        if (t == LocalDateTime.class) return LocalDateTime.of(2026, 1, 19, 0, 0, 0);

        if (List.class.isAssignableFrom(t)) return List.of();

        if (t.isEnum()) {
            Object[] constants = t.getEnumConstants();
            return (constants != null && constants.length > 0) ? constants[0] : null;
        }

        if (t.isRecord()) {
            @SuppressWarnings("unchecked")
            Class<Object> rt = (Class<Object>) t;
            return newRecord(rt);
        }

        return null;
    }

    @Test
    @DisplayName("팀 파트 조회 (드롭다운)")
    void readProjectParts() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        ProjectPartsResDto response = newRecord(ProjectPartsResDto.class);

        given(projectTeamQueryService.readProjectParts(eq(projectId), eq(userId)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/parts", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("project-parts-read",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Project")
                                        .summary("팀 파트 조회")
                                        .description("현재 프로젝트에 설정된 파트 목록을 조회합니다. (드롭다운)")
                                        .pathParameters(
                                                com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName("projectId")
                                                        .description("프로젝트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                subsectionWithPath("body").type(OBJECT).description("팀 파트 조회 결과")
                                        )
                                        .build()
                        )
                ));

        verify(projectTeamQueryService).readProjectParts(eq(projectId), eq(userId));
    }

    @Test
    @DisplayName("프로젝트 전체 인원 조회 (담당자 드롭다운)")
    void readProjectUsers() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        ProjectUsersResDto response = newRecord(ProjectUsersResDto.class);

        given(projectTeamQueryService.readProjectUsers(eq(projectId), eq(userId)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/users", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("project-users-read",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Project")
                                        .summary("프로젝트 전체 인원 조회")
                                        .description("프로젝트에 속한 전체 인원 목록을 조회합니다. (담당자 드롭다운)")
                                        .pathParameters(
                                                com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName("projectId")
                                                        .description("프로젝트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                subsectionWithPath("body").type(OBJECT).description("프로젝트 전체 인원 조회 결과")
                                        )
                                        .build()
                        )
                ));

        verify(projectTeamQueryService).readProjectUsers(eq(projectId), eq(userId));
    }
}
