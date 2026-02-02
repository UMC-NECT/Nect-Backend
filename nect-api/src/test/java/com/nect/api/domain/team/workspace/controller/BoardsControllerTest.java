package com.nect.api.domain.team.workspace.controller;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.team.workspace.dto.req.BoardsBasicInfoUpdateReqDto;
import com.nect.api.domain.team.workspace.dto.res.BoardsBasicInfoGetResDto;
import com.nect.api.domain.team.workspace.dto.res.MissionProgressResDto;
import com.nect.api.domain.team.workspace.dto.res.RoleFieldDto;
import com.nect.api.domain.team.workspace.facade.BoardsFacade;
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

import java.time.LocalDate;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class BoardsControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BoardsFacade boardsFacade;

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
    @DisplayName("팀보드 기본 정보 조회")
    void getBasicInfo() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        BoardsBasicInfoGetResDto response = new BoardsBasicInfoGetResDto(
                projectId,
                "프로젝트 제목",
                "프로젝트 설명",
                "공지 텍스트",
                "정기회의 텍스트",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1),
                10L,
                true
        );

        given(boardsFacade.getBoardsBasicInfo(eq(projectId), eq(userId))).willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/boards/basic-info", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("boards-basicinfo-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Boards")
                                        .summary("팀보드 기본 정보 조회")
                                        .description("팀보드 상단의 기본 정보를 조회합니다.")
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
                                                fieldWithPath("body.project_id").type(NUMBER).description("프로젝트 ID"),
                                                fieldWithPath("body.title").type(STRING).description("프로젝트 제목"),
                                                fieldWithPath("body.description").type(STRING).description("프로젝트 설명"),
                                                fieldWithPath("body.notice_text").type(STRING).description("공지"),
                                                fieldWithPath("body.regular_meeting_text").type(STRING).description("정기회의"),
                                                fieldWithPath("body.planned_started_on").type(STRING).description("계획 시작일(yyyy-MM-dd)"),
                                                fieldWithPath("body.planned_ended_on").type(STRING).description("계획 종료일(yyyy-MM-dd)"),
                                                fieldWithPath("body.remaining_days").type(NUMBER).description("남은 일자(0 이상)"),
                                                fieldWithPath("body.can_edit").type(BOOLEAN).description("수정 가능 여부(리더)")
                                        )
                                        .build()
                        )
                ));

        verify(boardsFacade).getBoardsBasicInfo(eq(projectId), eq(userId));
    }

    @Test
    @DisplayName("팀보드 기본 정보 수정")
    void updateBasicInfo() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        BoardsBasicInfoUpdateReqDto request = new BoardsBasicInfoUpdateReqDto(
                "새 공지",
                "새 정기회의"
        );

        willDoNothing().given(boardsFacade).updateBasicInfo(eq(projectId), eq(userId), any(BoardsBasicInfoUpdateReqDto.class));

        mockMvc.perform(patch("/api/v1/projects/{projectId}/boards/basic-info", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("boards-basicinfo-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Boards")
                                        .summary("팀보드 기본 정보 수정")
                                        .description("팀보드 기본 정보를 수정합니다. (리더만 가능)")
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("projectId").description("프로젝트 ID")
                                        )
                                        .requestHeaders(
                                                headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                        )
                                        .requestFields(
                                                fieldWithPath("notice_text").optional().type(STRING).description("공지(미입력 시 변경 없음)"),
                                                fieldWithPath("regular_meeting_text").optional().type(STRING).description("정기회의(미입력 시 변경 없음)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                                fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(STRING).description("메시지"),
                                                fieldWithPath("status.description").optional().type(STRING).description("상세 설명")
                                        )
                                        .build()
                        )
                ));

        verify(boardsFacade).updateBasicInfo(eq(projectId), eq(userId), any(BoardsBasicInfoUpdateReqDto.class));
    }

    @Test
    @DisplayName("미션 완료 개수(진행도) 조회")
    void getMissionProgress() throws Exception {
        long projectId = 1L;
        long userId = 1L;

        MissionProgressResDto.TotalDto total = new MissionProgressResDto.TotalDto(
                12L,
                7L,
                0.5833333333
        );

        List<MissionProgressResDto.TeamDto> teams = List.of(
                new MissionProgressResDto.TeamDto(
                        RoleFieldDto.of(RoleField.BACKEND),
                        5L,
                        3L,
                        0.6
                ),
                new MissionProgressResDto.TeamDto(
                        RoleFieldDto.of(RoleField.CUSTOM, "기획-운영"),
                        7L,
                        4L,
                        0.5714285714
                )
        );

        MissionProgressResDto response = new MissionProgressResDto(total, teams);

        given(boardsFacade.getMissionProgress(eq(projectId), eq(userId))).willReturn(response);

        mockMvc.perform(get("/api/v1/projects/{projectId}/boards/mission-progress", projectId)
                        .with(mockUser(userId))
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("boards-missionprogress-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Boards")
                                        .summary("미션 완료 개수(진행도) 조회")
                                        .description("전체/팀(필드)별 미션 총 개수, 완료 개수, 완료율을 조회합니다.")
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

                                                fieldWithPath("body.total").type(OBJECT).description("전체 집계"),
                                                fieldWithPath("body.total.total_count").type(NUMBER).description("전체 미션 총 개수"),
                                                fieldWithPath("body.total.completed_count").type(NUMBER).description("전체 미션 완료 개수"),
                                                fieldWithPath("body.total.completion_rate").type(NUMBER).description("전체 미션 완료율(0~1)"),

                                                fieldWithPath("body.teams").type(ARRAY).description("팀(필드)별 집계"),

                                                fieldWithPath("body.teams[].field").type(OBJECT).description("팀(필드) 정보"),
                                                fieldWithPath("body.teams[].field.type").type(STRING).description("필드 타입(enum)"),
                                                fieldWithPath("body.teams[].field.custom_name").optional().type(STRING)
                                                        .description("type=CUSTOM일 때 직접 입력 이름"),

                                                fieldWithPath("body.teams[].total_count").type(NUMBER).description("팀 미션 총 개수"),
                                                fieldWithPath("body.teams[].completed_count").type(NUMBER).description("팀 미션 완료 개수"),
                                                fieldWithPath("body.teams[].completion_rate").type(NUMBER).description("팀별 완료율")
                                        )
                                        .build()
                        )
                ));

        verify(boardsFacade).getMissionProgress(eq(projectId), eq(userId));
    }
}
