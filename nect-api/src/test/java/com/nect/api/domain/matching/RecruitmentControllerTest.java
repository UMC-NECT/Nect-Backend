package com.nect.api.domain.matching;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.domain.matching.dto.RecruitmentResDto;
import com.nect.api.domain.matching.service.RecruitmentService;
import com.nect.api.domain.team.project.dto.RecruitingProjectResDto;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
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
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
public class RecruitmentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RecruitmentService recruitmentService;

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
    void getRecruitingFields() throws Exception {
        RecruitmentResDto.RecruitingFieldDto dto = RecruitmentResDto.RecruitingFieldDto.builder()
                        .field(RoleField.BACKEND).customField(null).build();

        given(recruitmentService.findRecruitingFields(eq(1L))).willReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/recruitments/{projectId}", 1L)
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-recruiting-fields",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Recruitment")
                                .summary("지원 가능한 분야 조회")
                                .description("해당 프로젝트의 지원 가능한 분야를 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("projectId").description("프로젝트 ID")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터(모집 중인 분야 목록)"),
                                        fieldWithPath("body[].field").description("분야"),
                                        fieldWithPath("body[].customField").description("커스텀 분야(default = null)")
                                        )
                                .build()
                        )
                ));
    }

    @Test
    void getMyRecruitingProjectAsLeader() throws Exception {
        RecruitingProjectResDto dto = new RecruitingProjectResDto(123L, "Awesome Project", "project desc");
        given(recruitmentService.getMyRecruitingProjectAsLeader(anyLong())).willReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/recruitments/leader")
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-recruiting-projects-leader",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Recruitment")
                                .summary("내가 리더인 모집중 프로젝트 조회")
                                .description("로그인한 사용자가 리더인 모집중 프로젝트 목록을 반환합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터(프로젝트 목록)"),
                                        fieldWithPath("body[].projectId").description("프로젝트 ID"),
                                        fieldWithPath("body[].title").description("프로젝트 제목"),
                                        fieldWithPath("body[].description").description("프로젝트 설명")
                                )
                                .build()
                        )
                ));
    }
}
