package com.nect.api.domain.matching;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.domain.matching.dto.MatchingReqDto;
import com.nect.api.domain.matching.dto.MatchingResDto;
import com.nect.api.domain.matching.enums.CounterParty;
import com.nect.api.domain.matching.facade.MatchingFacade;
import com.nect.api.domain.matching.service.MatchingService;
import com.nect.api.domain.team.project.dto.ProjectUserResDto;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
import com.nect.core.entity.matching.enums.MatchingRejectReason;
import com.nect.core.entity.matching.enums.MatchingRequestType;
import com.nect.core.entity.matching.enums.MatchingStatus;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
public class MatchingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MatchingFacade matchingFacade;

    @MockitoBean
    MatchingService matchingService;

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
    void requestMatchingByUser() throws Exception {
        MatchingReqDto.matchingReqDto reqDto = new MatchingReqDto.matchingReqDto(RoleField.BACKEND, null);

        given(matchingFacade.createUserToProjectMatching(anyLong(), eq(1L), eq(reqDto)))
                .willReturn(
                        MatchingResDto.MatchingRes.builder()
                                .id(1L)
                                .requestUserId(1L)
                                .targetUserId(1L)
                                .projectId(1L)
                                .field(RoleField.BACKEND)
                                .customField(null)
                                .matchingStatus(MatchingStatus.PENDING)
                                .requestType(MatchingRequestType.USER_TO_PROJECT)
                                .expiresAt(LocalDateTime.parse("2026-01-26T12:30:00"))
                                .build()
                );

        mockMvc.perform(post("/api/v1/matchings/projects/{projectId}", 1L)
                        .with(csrf())
                        .header("Authorization", "Bearer AccessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"field\":\"BACKEND\"}"))
                .andExpect(status().isOk())
                .andDo(document("matching-request-user-to-project",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Matching")
                                .summary("유저 -> 프로젝트 매칭 요청")
                                .description("유저가 특정 프로젝트의 특정 분야(field)에 매칭을 요청합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("projectId").description("프로젝트 ID")
                                )
                                .requestFields(
                                        fieldWithPath("field").type(JsonFieldType.STRING).description("요청 분야")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.id").description("매칭 ID"),
                                        fieldWithPath("body.requestUserId").description("요청자 유저 ID"),
                                        fieldWithPath("body.targetUserId").description("대상 유저 ID"),
                                        fieldWithPath("body.projectId").description("프로젝트 ID"),
                                        fieldWithPath("body.field").description("분야"),
                                        fieldWithPath("body.customField").description("커스텀 분야(default = null)"),
                                        fieldWithPath("body.matchingStatus").description("매칭 상태"),
                                        fieldWithPath("body.requestType").description("요청 타입"),
                                        fieldWithPath("body.expiresAt").description("만료 시각")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void requestMatchingByProject() throws Exception{
        MatchingReqDto.matchingReqDto reqDto = new MatchingReqDto.matchingReqDto(RoleField.BACKEND, null);

        given(matchingFacade.createProjectToUserMatching(anyLong(), eq(1L), eq(1L), eq(reqDto)))
                .willReturn(
                        MatchingResDto.MatchingRes.builder()
                                .id(1L)
                                .requestUserId(1L)
                                .targetUserId(1L)
                                .projectId(1L)
                                .field(RoleField.BACKEND)
                                .customField(null)
                                .matchingStatus(MatchingStatus.PENDING)
                                .requestType(MatchingRequestType.PROJECT_TO_USER)
                                .expiresAt(LocalDateTime.parse("2026-01-26T12:30:00"))
                                .build()
                );

        mockMvc.perform(post("/api/v1/matchings/projects/{projectId}/users/{targetUserId}", 1L, 1L)
                        .with(csrf())
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"field\": \"BACKEND\"}"))
                .andExpect(status().isOk())
                .andDo(document("matching-request-project-to-user",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Matching")
                                .summary("프로젝트 -> 유저 매칭 요청")
                                .description("프로젝트의 리더가 특정 유저를 특정 분야에 매칭을 요청합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("projectId").description("프로젝트 ID"),
                                        parameterWithName("targetUserId").description("요청받는 유저 ID")
                                )
                                .requestFields(
                                        fieldWithPath("field").type(JsonFieldType.STRING).description("요청 분야")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.id").description("매칭 ID"),
                                        fieldWithPath("body.requestUserId").description("요청자 유저 ID"),
                                        fieldWithPath("body.targetUserId").description("대상 유저 ID"),
                                        fieldWithPath("body.projectId").description("프로젝트 ID"),
                                        fieldWithPath("body.field").description("분야"),
                                        fieldWithPath("body.customField").description("커스텀 분야(default = null)"),
                                        fieldWithPath("body.matchingStatus").description("매칭 상태"),
                                        fieldWithPath("body.requestType").description("요청 타입"),
                                        fieldWithPath("body.expiresAt").description("만료 시각")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void cancelMatchingRequest() throws Exception{
        given(matchingFacade.cancelMatching(eq(1L), anyLong()))
                .willReturn(
                        MatchingResDto.MatchingRes.builder()
                                .id(1L)
                                .requestUserId(1L)
                                .targetUserId(1L)
                                .projectId(1L)
                                .field(RoleField.BACKEND)
                                .customField(null)
                                .matchingStatus(MatchingStatus.CANCELED)
                                .requestType(MatchingRequestType.USER_TO_PROJECT)
                                .expiresAt(LocalDateTime.parse("2026-01-26T12:30:00"))
                                .build()
                );

        mockMvc.perform(post("/api/v1/matchings/{matchingId}/cancel", 1L)
                        .with(csrf())
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("matching-cancel",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Matching")
                                .summary("매칭 취소 (유저 -> 프로젝트, 프로젝트 -> 유저 범용 API)")
                                .description("매칭 요청을 한 유저가 해당 매칭을 취소합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("matchingId").description("매칭 ID")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.id").description("매칭 ID"),
                                        fieldWithPath("body.requestUserId").description("요청자 유저 ID"),
                                        fieldWithPath("body.targetUserId").description("대상 유저 ID"),
                                        fieldWithPath("body.projectId").description("프로젝트 ID"),
                                        fieldWithPath("body.field").description("분야"),
                                        fieldWithPath("body.customField").description("커스텀 분야(default = null)"),
                                        fieldWithPath("body.matchingStatus").description("매칭 상태"),
                                        fieldWithPath("body.requestType").description("요청 타입"),
                                        fieldWithPath("body.expiresAt").description("만료 시각")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void acceptMatchingRequest() throws Exception{
        MatchingResDto.MatchingAcceptResDto dto = MatchingResDto.MatchingAcceptResDto.builder()
                        .matching(MatchingResDto.MatchingRes.builder()
                                .id(1L)
                                .requestUserId(1L)
                                .targetUserId(1L)
                                .projectId(1L)
                                .field(RoleField.BACKEND)
                                .customField(null)
                                .matchingStatus(MatchingStatus.ACCEPTED)
                                .requestType(MatchingRequestType.PROJECT_TO_USER)
                                .expiresAt(LocalDateTime.parse("2026-01-26T12:30:00"))
                                .build())
                        .projectUser(ProjectUserResDto.builder()
                                .id(1L)
                                .userId(1L)
                                .projectId(1L)
                                .field(RoleField.BACKEND)
                                .memberType(ProjectMemberType.MEMBER)
                                .memberStatus(ProjectMemberStatus.ACTIVE)
                                .build())
                        .build();

        given(matchingFacade.acceptMatchingRequest(eq(1L), anyLong())).willReturn(dto);

        mockMvc.perform(post("/api/v1/matchings/{matchingId}/accept", 1L)
                        .with(csrf())
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("matching-accept",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Matching")
                                .summary("매칭 수락 (유저 -> 프로젝트, 프로젝트 -> 유저 범용 API)")
                                .description("매칭 요청을 받은 주체(회원, 리더)가 요청을 수락합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("matchingId").description("매칭 ID")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.matching.id").description("매칭 ID"),
                                        fieldWithPath("body.matching.requestUserId").description("요청자 유저 ID"),
                                        fieldWithPath("body.matching.targetUserId").description("대상 유저 ID"),
                                        fieldWithPath("body.matching.projectId").description("프로젝트 ID"),
                                        fieldWithPath("body.matching.field").description("분야"),
                                        fieldWithPath("body.matching.customField").description("커스텀 분야(default = null)"),
                                        fieldWithPath("body.matching.matchingStatus").description("매칭 상태"),
                                        fieldWithPath("body.matching.requestType").description("요청 타입"),
                                        fieldWithPath("body.matching.expiresAt").description("만료 시각"),

                                        fieldWithPath("body.projectUser.id").description("프로젝트 멤버 ID"),
                                        fieldWithPath("body.projectUser.userId").description("유저 ID"),
                                        fieldWithPath("body.projectUser.projectId").description("프로젝트 ID"),
                                        fieldWithPath("body.projectUser.field").description("분야"),
                                        fieldWithPath("body.projectUser.memberType").description("멤버 타입"),
                                        fieldWithPath("body.projectUser.memberStatus").description("멤버 상태")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void rejectMatchingRequest() throws Exception{
        given(matchingFacade.rejectMatching(eq(1L), anyLong(), eq(MatchingRejectReason.OTHER)))
                .willReturn(
                        MatchingResDto.MatchingRes.builder()
                                .id(1L)
                                .requestUserId(1L)
                                .targetUserId(1L)
                                .projectId(1L)
                                .field(RoleField.BACKEND)
                                .customField(null)
                                .matchingStatus(MatchingStatus.REJECTED)
                                .requestType(MatchingRequestType.PROJECT_TO_USER)
                                .expiresAt(LocalDateTime.parse("2026-01-26T12:30:00"))
                                .build()
                );

        mockMvc.perform(post("/api/v1/matchings/{matchingId}/reject", 1L)
                        .with(csrf())
                        .header("Authorization", "Bearer AccessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rejectReason\":\"OTHER\"}"))
                .andExpect(status().isOk())
                .andDo(document("matching-reject",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Matching")
                                .summary("매칭 거절 (유저 -> 프로젝트, 프로젝트 -> 유저 범용 API)")
                                .description("매칭 요청을 받은 주체(회원, 리더)가 요청을 거절합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("matchingId").description("매칭 ID")
                                )
                                .requestFields(
                                        fieldWithPath("rejectReason").type(JsonFieldType.STRING).description("거절 사유")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.id").description("매칭 ID"),
                                        fieldWithPath("body.requestUserId").description("요청자 유저 ID"),
                                        fieldWithPath("body.targetUserId").description("대상 유저 ID"),
                                        fieldWithPath("body.projectId").description("프로젝트 ID"),
                                        fieldWithPath("body.field").description("분야"),
                                        fieldWithPath("body.customField").description("커스텀 분야(default = null)"),
                                        fieldWithPath("body.matchingStatus").description("매칭 상태"),
                                        fieldWithPath("body.requestType").description("요청 타입"),
                                        fieldWithPath("body.expiresAt").description("만료 시각")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void getReceivedMatchingsByProject() throws Exception {
        MatchingResDto.ProjectSummary projectSummary = MatchingResDto.ProjectSummary.builder()
                .title("NECT")
                .description("Project description")
                .imageUrl("https://example.com/image.jpg")
                .currentMembersNum(3)
                .build();

        MatchingResDto.UserSummary userSummary = MatchingResDto.UserSummary.builder()
                .nickname("seoyeon")
                .bio("Designer")
                .field(RoleField.BACKEND)
                .profileUrl("https://example.com/image.jpg")
                .build();

        MatchingResDto.MatchingListRes dto = MatchingResDto.MatchingListRes.builder()
                .counterParty(CounterParty.PROJECT)
                .userMatchings(java.util.List.of(userSummary))
                .projectMatchings(java.util.List.of(projectSummary))
                .build();

        given(matchingService.getReceivedMatchingsByTarget(anyLong(), eq(CounterParty.PROJECT), eq(MatchingStatus.PENDING)))
                .willReturn(dto);

        mockMvc.perform(get("/api/v1/matchings/received")
                        .param("target", "project")
                        .param("status", "pending")
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("matching-get-received",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Matching")
                                .summary("받은 매칭 요청")
                                .description("target에 해당되는 받은(수신) 매칭 요청을 조회합니다. ")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .queryParameters(
                                        parameterWithName("target").description("조회 대상 (project | user)"),
                                        parameterWithName("status").description("매칭 상태 (pending | accepted | rejected | canceled | expired)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.counterParty").description("대상 타입 (PROJECT | USER)"),

                                        fieldWithPath("body.userMatchings").description("유저 매칭 요약 목록(대상이 USER일 때 채워짐)"),
                                        fieldWithPath("body.userMatchings[].nickname").description("닉네임"),
                                        fieldWithPath("body.userMatchings[].bio").description("한줄 소개"),
                                        fieldWithPath("body.userMatchings[].field").description("분야"),
                                        fieldWithPath("body.userMatchings[].profileUrl").description("프로필 URL"),

                                        fieldWithPath("body.projectMatchings").description("프로젝트 매칭 요약 목록(대상이 PROJECT일 때 채워짐)"),
                                        fieldWithPath("body.projectMatchings[].title").description("프로젝트 제목"),
                                        fieldWithPath("body.projectMatchings[].description").description("프로젝트 설명"),
                                        fieldWithPath("body.projectMatchings[].imageUrl").description("프로젝트 대표 이미지"),
                                        fieldWithPath("body.projectMatchings[].currentMembersNum").description("현재 멤버 수")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void getSentMatchingsByUser() throws Exception {
        UserDetailsImpl testUser = new UserDetailsImpl(1L, Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());

        MatchingResDto.UserSummary userSummary = MatchingResDto.UserSummary.builder()
                .nickname("seoyeon")
                .bio("Designer")
                .field(RoleField.BACKEND)
                .profileUrl("https://example.com/avatar.jpg")
                .build();

        MatchingResDto.ProjectSummary projectSummary = MatchingResDto.ProjectSummary.builder()
                .title("NECT")
                .description("Project description")
                .imageUrl("https://example.com/image.jpg")
                .currentMembersNum(3)
                .build();

        MatchingResDto.MatchingListRes dto = MatchingResDto.MatchingListRes.builder()
                .counterParty(CounterParty.USER)
                .userMatchings(java.util.List.of(userSummary))
                .projectMatchings(java.util.List.of(projectSummary))
                .build();

        given(matchingService.getSentMatchingsByTarget(anyLong(), eq(CounterParty.USER), eq(MatchingStatus.PENDING)))
                .willReturn(dto);

        mockMvc.perform(get("/api/v1/matchings/sent")
                        .param("target", "user")
                        .param("status", "pending")
                        .with(authentication(authentication))
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("matching-get-sent",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Matching")
                                .summary("보낸 매칭 요청")
                                .description("보낸(발신) 매칭 요청을 조회합니다. target에 해당되는 매칭 요청만 조회됩니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .queryParameters(
                                        parameterWithName("target").description("조회 대상 (project | user)"),
                                        parameterWithName("status").description("매칭 상태 (pending | accepted | rejected | canceled | expired)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.counterParty").description("대상 타입 (PROJECT | USER)"),

                                        fieldWithPath("body.userMatchings").description("유저 매칭 요약 목록(대상이 USER일 때 채워짐)"),
                                        fieldWithPath("body.userMatchings[].nickname").description("닉네임"),
                                        fieldWithPath("body.userMatchings[].bio").description("한줄 소개"),
                                        fieldWithPath("body.userMatchings[].field").description("분야"),
                                        fieldWithPath("body.userMatchings[].profileUrl").description("프로필 URL"),

                                        fieldWithPath("body.projectMatchings").description("프로젝트 매칭 요약 목록(대상이 PROJECT일 때 채워짐)"),
                                        fieldWithPath("body.projectMatchings[].title").description("프로젝트 제목"),
                                        fieldWithPath("body.projectMatchings[].description").description("프로젝트 설명"),
                                        fieldWithPath("body.projectMatchings[].imageUrl").description("프로젝트 대표 이미지"),
                                        fieldWithPath("body.projectMatchings[].currentMembersNum").description("현재 멤버 수")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void getMatchingsCount() throws Exception {
        UserDetailsImpl testUser = new UserDetailsImpl(1L, Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());

        MatchingResDto.MatchingCounts counts = MatchingResDto.MatchingCounts.builder()
                .receivedCount(2)
                .sentCount(5)
                .build();

        given(matchingService.getMatchingsCount(anyLong())).willReturn(counts);

        mockMvc.perform(get("/api/v1/matchings/count")
                        .with(authentication(authentication))
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("matching-get-count",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Matching")
                                .summary("매칭 요청 개수 조회")
                                .description("보낸/받은 매칭(PENDING) 개수를 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.receivedCount").description("받은(PENDING) 요청 수"),
                                        fieldWithPath("body.sentCount").description("보낸(PENDING) 요청 수")
                                )
                                .build()
                        )
                ));
    }
}
