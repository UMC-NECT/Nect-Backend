package com.nect.api.notifications.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.domain.notifications.dto.NotificationListResponse;
import com.nect.api.domain.notifications.dto.NotificationResponse;
import com.nect.api.domain.notifications.service.NotificationService;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
import com.nect.core.entity.notifications.enums.NotificationClassification;
import com.nect.core.entity.notifications.enums.NotificationScope;
import com.nect.core.entity.notifications.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    private static final String AUTH_HEADER = "Authorization";
    private static final String TEST_ACCESS_TOKEN = "Bearer AccessToken";

    @BeforeEach
    void setUpAuth() {
        doNothing().when(jwtUtil).validateToken(anyString());
        given(tokenBlacklistService.isBlacklisted(anyString())).willReturn(false);
        given(jwtUtil.getUserIdFromToken(anyString())).willReturn(1L);
        given(userDetailsService.loadUserByUsername(anyString())).willReturn(
                UserDetailsImpl.builder()
                        .userId(1L)
                        .roles(List.of("ROLE_USER"))
                        .build()
        );
    }

    @Test
    @DisplayName("알림 목록 조회 API")
    void 알림_목록_조회_API() throws Exception {

        given(notificationService.getNotifications(
                any(),
                eq(NotificationScope.MAIN_HOME),
                eq(null),
                eq(20)
        )).willReturn(mockResponse());

        mockMvc.perform(get("/api/v1/notifications")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .param("scope", "MAIN_HOME")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("notifications-list",
                        resource(ResourceSnippetParameters.builder()
                                .tag("알림")
                                .summary("알림 목록 조회")
                                .description("사용자의 알림 목록을 커서 기반 페이징 방식으로 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .queryParameters(
                                        parameterWithName("scope")
                                                .description("알림 범위 (MAIN_HOME, WORKSPACE_ONLY, WORKSPACE_GLOBAL)"),
                                        parameterWithName("cursor")
                                                .optional()
                                                .description("커서 기반 페이징용 알림 ID"),
                                        parameterWithName("size")
                                                .optional()
                                                .description("조회할 알림 개수 (기본값 20)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode")
                                                .description("응답 상태 코드"),
                                        fieldWithPath("status.message")
                                                .description("응답 메시지"),
                                        fieldWithPath("status.description")
                                                .optional()
                                                .description("응답 상세 설명"),

                                        fieldWithPath("body.notifications")
                                                .description("알림 목록"),
                                        fieldWithPath("body.notifications[].mainMessage")
                                                .description("알림 메인 메시지"),
                                        fieldWithPath("body.notifications[].contentMessage")
                                                .optional()
                                                .description("알림 부가 메시지"),
                                        fieldWithPath("body.notifications[].noticeId")
                                                .description("알림 ID"),
                                        fieldWithPath("body.notifications[].targetId")
                                                .description("알림 대상 ID"),
                                        fieldWithPath("body.notifications[].projectId")
                                                .description("프로젝트 ID"),
                                        fieldWithPath("body.notifications[].createdDate")
                                                .description("알림 생성일 (yy.MM.dd)"),
                                        fieldWithPath("body.notifications[].classification")
                                                .optional()
                                                .description("알림 분류 (한글)"),
                                        fieldWithPath("body.notifications[].isRead")
                                                .description("다음 페이지 조회용 커서"),
                                        fieldWithPath("body.notifications[].type")
                                                .description("알림 타입"),
                                        fieldWithPath("body.notifications[].scope")
                                                .description("알림이 가는 화면 대상"),
                                        fieldWithPath("body.nextCursor")
                                                .description("다음 페이지 조회용 커서")
                                )
                                .build()
                        )
                ));
    }

    private NotificationListResponse mockResponse() {
        return NotificationListResponse.builder()
                .notifications(List.of(
                        new NotificationResponse(
                                "홍길동이 나에게 메시지를 보냈습니다.",
                                "“안녕하세요!”",
                                10L,
                                100L,
                                1L,
                                "25.01.20",
                                NotificationClassification.MESSAGE.getClassifyKr(),
                                NotificationType.CHAT_MESSAGE_RECEIVED.name(),
                                NotificationScope.MAIN_HOME.name(),
                                true
                        ),
                        new NotificationResponse(
                                "새로운 팀원 김철수님이 합류했습니다.",
                                null,
                                9L,
                                101L,
                                1L,
                                "25.01.19",
                                NotificationClassification.WORK_SPACE.getClassifyKr(),
                                NotificationType.WORKSPACE_MEMBER_JOINED.name(),
                                NotificationScope.WORKSPACE_ONLY.name(),
                                false
                        )
                ))
                .nextCursor(9L)
                .build();
    }
}
