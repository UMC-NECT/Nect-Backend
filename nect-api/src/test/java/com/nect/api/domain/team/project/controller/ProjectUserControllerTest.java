package com.nect.api.domain.team.project.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.domain.team.project.dto.ProjectUserFieldReqDto;
import com.nect.api.domain.team.project.dto.ProjectUserFieldResDto;
import com.nect.api.domain.team.project.dto.ProjectUserResDto;
import com.nect.api.domain.team.project.dto.UserProjectDto;
import com.nect.api.domain.team.project.service.ProjectUserService;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.entity.user.enums.RoleField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
public class ProjectUserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    @MockitoBean
    ProjectUserService projectUserService;

    @MockitoBean
    TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

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

    @Test
    void getProjectsByUser() throws Exception {
        UserProjectDto dto = UserProjectDto.builder()
                        .projectId(1L).memberType(ProjectMemberType.MEMBER).build();

        given(projectUserService.findProjectsByUser(anyLong())).willReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/project-users")
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-projects-by-user",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("ProjectUser")
                                .summary("현재 참여하고 있는 프로젝트 조회")
                                .description("로그인한 유저가 현재 참여하고 있는 프로젝트를 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body[].projectId").description("프로젝트 ID"),
                                        fieldWithPath("body[].memberType").description("프로젝트 멤버 타입(MEMBER | LEADER)")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void updateProjectUserField() throws Exception {
        Long projectUserId = 1L;

        ProjectUserFieldResDto resDto = ProjectUserFieldResDto.builder()
                .projectUserId(projectUserId)
                .field(RoleField.CUSTOM)
                .customField("Designer")
                .build();

        given(projectUserService.changeProjectUserFieldInProject(eq(projectUserId), any(ProjectUserFieldReqDto.class)))
                .willReturn(resDto);

        String requestJson = """
                {
                  "field": "CUSTOM",
                  "customField": "Designer"
                }
                """;

        mockMvc.perform(patch("/api/v1/project-users/{projectUserId}/field", projectUserId)
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andDo(document("patch-project-user-field",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("ProjectUser")
                                .summary("프로젝트 멤버 필드(파트) 변경")
                                .description("프로젝트 내 멤버의 필드(파트) 및 커스텀 필드를 변경합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .requestFields(
                                        fieldWithPath("field").description("변경할 필드 식별자"),
                                        fieldWithPath("customField").description("커스텀 필드명 (RoleField.CUSTOM 인 경우 필수)").optional()
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.projectUserId").description("프로젝트 유저 ID"),
                                        fieldWithPath("body.field").description("적용된 필드"),
                                        fieldWithPath("body.customField").description("적용된 커스텀 필드명").optional()
                                )
                                .build()
                        )
                ));
    }

    @Test
    void kickProjectUser() throws Exception {
        ProjectUserResDto resDto = ProjectUserResDto.builder()
                .id(1L)
                .memberStatus(ProjectMemberStatus.KICKED)
                .build();

        given(projectUserService.kickProjectUser(eq(1L)))
                .willReturn(resDto);

        mockMvc.perform(patch("/api/v1/project-users/{projectUserId}/kick", 1L)
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("patch-project-user-kick",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("ProjectUser")
                                .summary("프로젝트 멤버 강퇴")
                                .description("프로젝트에서 특정 멤버를 강퇴합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.id").description("프로젝트 유저 ID"),
                                        fieldWithPath("body.userId").description("유저 ID").optional(),
                                        fieldWithPath("body.projectId").description("프로젝트 ID").optional(),
                                        fieldWithPath("body.field").description("분야").optional(),
                                        fieldWithPath("body.memberType").description("멤버 타입").optional(),
                                        fieldWithPath("body.memberStatus").description("멤버 상태 (KICK)")
                                )
                                .build()
                        )
                ));
    }
}
