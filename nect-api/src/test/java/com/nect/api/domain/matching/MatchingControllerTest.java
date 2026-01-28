package com.nect.api.domain.matching;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.domain.matching.dto.MatchingResDto;
import com.nect.api.domain.matching.enums.MatchingBox;
import com.nect.api.domain.matching.facade.MatchingFacade;
import com.nect.api.domain.matching.service.MatchingService;
import com.nect.api.domain.team.project.dto.ProjectUserResDto;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.core.entity.matching.enums.MatchingRequestType;
import com.nect.core.entity.matching.enums.MatchingStatus;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.team.enums.ProjectMemberType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
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

    @Test
    void requestMatchingByUser() throws Exception {
        UserDetailsImpl testUser = new UserDetailsImpl(1L, Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());

        given(matchingFacade.createUserToProjectMatching(anyLong(), eq(1L), eq(2L)))
                .willReturn(
                        MatchingResDto.MatchingRes.builder()
                                .id(1L)
                                .requestUserId(1L)
                                .targetUserId(1L)
                                .projectId(1L)
                                .fieldId(2L)
                                .matchingStatus(MatchingStatus.PENDING)
                                .requestType(MatchingRequestType.USER_TO_PROJECT)
                                .expiresAt(LocalDateTime.parse("2026-01-26T12:30:00"))
                                .build()
                );

        mockMvc.perform(post("/matchings/projects/{projectId}", 1L)
                        .with(csrf())
                        .with(authentication(authentication))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fieldId\":2}"))
                .andExpect(status().isOk())
                .andDo(document("matching-request-user-to-project",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("매칭")
                                .summary("유저 -> 프로젝트 매칭 요청")
                                .description("유저가 특정 프로젝트의 특정 분야(field)에 매칭을 요청합니다.")
                                .pathParameters(
                                        parameterWithName("projectId").description("프로젝트 ID")
                                )
                                .requestFields(
                                        fieldWithPath("fieldId").type(NUMBER).description("요청 분야 ID")
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
                                        fieldWithPath("body.fieldId").description("분야 ID"),
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
        UserDetailsImpl testUser = new UserDetailsImpl(1L, Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());

        given(matchingFacade.createProjectToUserMatching(anyLong(), eq(1L), eq(1L), eq(2L)))
                .willReturn(
                        MatchingResDto.MatchingRes.builder()
                                .id(1L)
                                .requestUserId(1L)
                                .targetUserId(1L)
                                .projectId(1L)
                                .fieldId(2L)
                                .matchingStatus(MatchingStatus.PENDING)
                                .requestType(MatchingRequestType.PROJECT_TO_USER)
                                .expiresAt(LocalDateTime.parse("2026-01-26T12:30:00"))
                                .build()
                );

        mockMvc.perform(post("/matchings/projects/{projectId}/users/{targetUserId}", 1L, 1L)
                        .with(csrf())
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fieldId\":2}"))
                .andExpect(status().isOk())
                .andDo(document("matching-request-project-to-user",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("매칭")
                                .summary("프로젝트 -> 유저 매칭 요청")
                                .description("프로젝트의 리더가 특정 유저를 특정 분야에 매칭을 요청합니다.")
                                .pathParameters(
                                        parameterWithName("projectId").description("프로젝트 ID"),
                                        parameterWithName("targetUserId").description("요청받는 유저 ID")
                                )
                                .requestFields(
                                        fieldWithPath("fieldId").type(NUMBER).description("요청 분야 ID")
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
                                        fieldWithPath("body.fieldId").description("분야 ID"),
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
        UserDetailsImpl testUser = new UserDetailsImpl(1L, Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());

        given(matchingService.cancelMatching(eq(1L), anyLong()))
                .willReturn(
                        MatchingResDto.MatchingRes.builder()
                                .id(1L)
                                .requestUserId(1L)
                                .targetUserId(1L)
                                .projectId(1L)
                                .fieldId(2L)
                                .matchingStatus(MatchingStatus.CANCELED)
                                .requestType(MatchingRequestType.USER_TO_PROJECT)
                                .expiresAt(LocalDateTime.parse("2026-01-26T12:30:00"))
                                .build()
                );

        mockMvc.perform(post("/matchings/{matchingId}/cancel", 1L)
                        .with(csrf())
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("matching-cancel",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("매칭")
                                .summary("매칭 취소 (유저 -> 프로젝트, 프로젝트 -> 유저 범용 API)")
                                .description("매칭 요청을 한 유저가 해당 매칭을 취소합니다.")
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
                                        fieldWithPath("body.fieldId").description("분야 ID"),
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
        UserDetailsImpl testUser = new UserDetailsImpl(1L, Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());

        MatchingResDto.MatchingAcceptResDto dto = MatchingResDto.MatchingAcceptResDto.builder()
                        .matching(MatchingResDto.MatchingRes.builder()
                                .id(1L)
                                .requestUserId(1L)
                                .targetUserId(1L)
                                .projectId(1L)
                                .fieldId(1L)
                                .matchingStatus(MatchingStatus.ACCEPTED)
                                .requestType(MatchingRequestType.PROJECT_TO_USER)
                                .expiresAt(LocalDateTime.parse("2026-01-26T12:30:00"))
                                .build())
                        .projectUser(ProjectUserResDto.builder()
                                .id(1L)
                                .userId(1L)
                                .projectId(1L)
                                .fieldId(1L)
                                .memberType(ProjectMemberType.MEMBER)
                                .memberStatus(ProjectMemberStatus.ACTIVE)
                                .build())
                        .build();

        given(matchingFacade.acceptMatchingRequest(eq(1L), anyLong())).willReturn(dto);

        mockMvc.perform(post("/matchings/{matchingId}/accept", 1L)
                        .with(csrf())
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("matching-accept",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("매칭")
                                .summary("매칭 수락 (유저 -> 프로젝트, 프로젝트 -> 유저 범용 API)")
                                .description("매칭 요청을 받은 주체(회원, 리더)가 요청을 수락합니다.")
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
                                        fieldWithPath("body.matching.fieldId").description("분야 ID"),
                                        fieldWithPath("body.matching.matchingStatus").description("매칭 상태"),
                                        fieldWithPath("body.matching.requestType").description("요청 타입"),
                                        fieldWithPath("body.matching.expiresAt").description("만료 시각"),

                                        fieldWithPath("body.projectUser.id").description("프로젝트 멤버 ID"),
                                        fieldWithPath("body.projectUser.userId").description("유저 ID"),
                                        fieldWithPath("body.projectUser.projectId").description("프로젝트 ID"),
                                        fieldWithPath("body.projectUser.fieldId").description("분야 ID"),
                                        fieldWithPath("body.projectUser.memberType").description("멤버 타입"),
                                        fieldWithPath("body.projectUser.memberStatus").description("멤버 상태")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void rejectMatchingRequest() throws Exception{
        UserDetailsImpl testUser = new UserDetailsImpl(1L, Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());

        given(matchingFacade.rejectMatching(eq(1L), anyLong()))
                .willReturn(
                        MatchingResDto.MatchingRes.builder()
                                .id(1L)
                                .requestUserId(1L)
                                .targetUserId(1L)
                                .projectId(1L)
                                .fieldId(1L)
                                .matchingStatus(MatchingStatus.REJECTED)
                                .requestType(MatchingRequestType.PROJECT_TO_USER)
                                .expiresAt(LocalDateTime.parse("2026-01-26T12:30:00"))
                                .build()
                );

        mockMvc.perform(post("/matchings/{matchingId}/reject", 1L)
                        .with(csrf())
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("matching-reject",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("매칭")
                                .summary("매칭 거절 (유저 -> 프로젝트, 프로젝트 -> 유저 범용 API)")
                                .description("매칭 요청을 받은 주체(회원, 리더)가 요청을 거절합니다.")
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
                                        fieldWithPath("body.fieldId").description("분야 ID"),
                                        fieldWithPath("body.matchingStatus").description("매칭 상태"),
                                        fieldWithPath("body.requestType").description("요청 타입"),
                                        fieldWithPath("body.expiresAt").description("만료 시각")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void getMatchingRequest() throws Exception {
        UserDetailsImpl testUser = new UserDetailsImpl(1L, Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());

        MatchingResDto.MatchingRes matching = MatchingResDto.MatchingRes.builder()
                .id(1L)
                .requestUserId(1L)
                .targetUserId(1L)
                .projectId(1L)
                .fieldId(1L)
                .matchingStatus(MatchingStatus.PENDING)
                .requestType(MatchingRequestType.PROJECT_TO_USER)
                .expiresAt(LocalDateTime.parse("2026-01-26T12:30:00"))
                .build();

        MatchingResDto.MatchingListRes dto = MatchingResDto.MatchingListRes.builder()
                .matchings(List.of(matching))
                .pendingRequestCount(1).build();

        given(matchingService.getMatchingsByBox(anyLong(), eq(MatchingBox.RECEIVED))).willReturn(dto);

        mockMvc.perform(get("/matchings")
                        .param("box", "received")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("matching-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("매칭")
                                .summary("매칭 요청 조회")
                                .description("매칭 요청을 받은/보낸 기준으로 조회합니다. Pending은 만료 임박순, EXPIRED는 생성일 최신순으로 정렬됩니다.")
                                .queryParameters(
                                        parameterWithName("box").description("조회함 구분 (received | sent)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.matchings").description("매칭 요청 목록"),
                                        fieldWithPath("body.matchings[].id").description("매칭 ID"),
                                        fieldWithPath("body.matchings[].requestUserId").description("요청자 유저 ID"),
                                        fieldWithPath("body.matchings[].targetUserId").description("대상 유저 ID"),
                                        fieldWithPath("body.matchings[].projectId").description("프로젝트 ID"),
                                        fieldWithPath("body.matchings[].fieldId").description("분야 ID"),
                                        fieldWithPath("body.matchings[].matchingStatus").description("매칭 상태"),
                                        fieldWithPath("body.matchings[].requestType").description("요청 타입"),
                                        fieldWithPath("body.matchings[].expiresAt").description("만료 시각"),

                                        fieldWithPath("body.pendingRequestCount").description("대기 중(PENDING) 요청 개수")
                                )
                                .build()
                        )
                ));
    }
}
