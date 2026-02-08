package com.nect.api.domain.team.workspace.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.workspace.dto.res.MemberBoardResDto;
import com.nect.api.domain.team.workspace.dto.res.RoleFieldDto;
import com.nect.api.domain.team.workspace.facade.BoardsMemberBoardFacade;
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
class BoardsMemberBoardControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BoardsMemberBoardFacade facade;

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
    @DisplayName("팀원 프로필 보드 조회")
    void getMemberBoard() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        MemberBoardResDto response = new MemberBoardResDto(
                List.of(
                        new MemberBoardResDto.MemberDto(
                                1L,
                                "홍길동",
                                "길동",
                                "https://img.com/u1.png",
                                RoleFieldDto.of(RoleField.BACKEND),
                                ProjectMemberType.MEMBER,
                                new MemberBoardResDto.CountsDto(
                                        2,  // planning
                                        1,  // in_progress
                                        3   // done
                                ),
                                true,
                                3600L,
                                LocalDateTime.of(2026, 1, 31, 10, 0, 0)
                        ),
                        new MemberBoardResDto.MemberDto(
                                2L,
                                "김철수",
                                "철수",
                                "https://img.com/u2.png",
                                RoleFieldDto.of(RoleField.CUSTOM, "기획-운영"),
                                ProjectMemberType.LEADER,
                                new MemberBoardResDto.CountsDto(
                                        1,
                                        4,
                                        2
                                ),
                                false,
                                1800L,
                                null
                        )
                )
        );

        given(facade.getMemberBoard(eq(projectId), eq(userId))).willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/boards/members", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("boards-memberboard-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Boards")
                                        .summary("팀원 프로필 보드 조회")
                                        .description("팀보드 좌측 하단 '팀원 프로필 보드'를 조회합니다. 프로젝트 멤버들의 기본 정보와 멤버별 담당 프로세스 상태 카운트(진행 전/중/완료), 근무 상태 정보를 제공합니다.")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                                fieldWithPath("body").type(OBJECT).description("응답 바디"),

                                                fieldWithPath("body.members").type(ARRAY).description("프로젝트 멤버 목록"),

                                                fieldWithPath("body.members[].user_id").type(NUMBER).description("멤버 유저 ID"),
                                                fieldWithPath("body.members[].name").type(STRING).description("멤버 이름"),
                                                fieldWithPath("body.members[].nickname").type(STRING).description("멤버 닉네임"),
                                                fieldWithPath("body.members[].profile_image_url").optional().type(STRING).description("프로필 이미지 URL (없으면 null)"),

                                                fieldWithPath("body.members[].field").optional().type(OBJECT).description("역할 분야"),
                                                fieldWithPath("body.members[].field.type").optional().type(STRING).description("역할 분야 타입(RoleField enum name)"),
                                                fieldWithPath("body.members[].field.custom_name").optional().type(STRING).description("CUSTOM일 때 직접 입력 값 (CUSTOM이 아니면 null)"),

                                                fieldWithPath("body.members[].member_type").type(STRING).description("프로젝트 멤버 타입(enum)"),

                                                fieldWithPath("body.members[].counts").type(OBJECT).description("멤버별 담당 프로세스 상태 카운트"),
                                                fieldWithPath("body.members[].counts.planning").type(NUMBER).description("진행 전 개수"),
                                                fieldWithPath("body.members[].counts.in_progress").type(NUMBER).description("진행 중 개수"),
                                                fieldWithPath("body.members[].counts.done").type(NUMBER).description("완료 개수"),

                                                fieldWithPath("body.members[].is_working").type(BOOLEAN).description("현재 근무중 여부"),
                                                fieldWithPath("body.members[].today_work_seconds").type(NUMBER).description("오늘 누적 근무 시간(초)"),
                                                fieldWithPath("body.members[].working_started_at").optional().type(STRING).description("근무 시작 시각(yyyy-MM-dd'T'HH:mm:ss) (근무중이 아니면 null)")
                                        )
                                        .build()
                        )
                ));

        verify(facade).getMemberBoard(eq(projectId), eq(userId));
    }
}
