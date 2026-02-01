package com.nect.api.domain.notifications.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
import com.nect.core.repository.matching.RecruitmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class NotificationEnumsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private RecruitmentRepository recruitmentRepository;

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
    @DisplayName("알림 분류 enum 조회 API")
    void 알림_분류_enum_조회_API() throws Exception {
        mockMvc.perform(get("/api/v1/enums/notifications/classifications")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("notifications-enums-classifications",
                        resource(ResourceSnippetParameters.builder()
                                .tag("알림")
                                .summary("알림 분류 enum 조회")
                                .description("알림 분류(NotificationClassification) enum 목록을 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .responseFields(enumListResponseFields())
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("알림 범위 enum 조회 API")
    void 알림_범위_enum_조회_API() throws Exception {
        mockMvc.perform(get("/api/v1/enums/notifications/scopes")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("notifications-enums-scopes",
                        resource(ResourceSnippetParameters.builder()
                                .tag("알림")
                                .summary("알림 범위 enum 조회")
                                .description("알림 범위(NotificationScope) enum 목록을 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .responseFields(enumListResponseFields())
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("알림 타입 MATCHING_REJECTED enum 조회 API")
    void 알림_타입_매칭_거절_enum_조회_API() throws Exception {
        mockMvc.perform(get("/api/v1/enums/notifications/types/matching-rejected")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("notifications-enums-types-matching-rejected",
                        resource(ResourceSnippetParameters.builder()
                                .tag("알림")
                                .summary("알림 타입 MATCHING_REJECTED 조회")
                                .description("MATCHING_REJECTED 알림 타입 메시지 포맷을 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .responseFields(matchingRejectedResponseFields())
                                .build()
                        )
                ));
    }

    private static List<FieldDescriptor> enumListResponseFields() {
        return List.of(
                fieldWithPath("status.statusCode").description("응답 상태 코드"),
                fieldWithPath("status.message").description("응답 메시지"),
                fieldWithPath("status.description").optional().description("응답 상세 설명"),
                fieldWithPath("body").description("enum 목록"),
                fieldWithPath("body[].value").description("enum 값"),
                fieldWithPath("body[].label").optional().description("표시 라벨")
        );
    }

    private static List<FieldDescriptor> matchingRejectedResponseFields() {
        return List.of(
                fieldWithPath("status.statusCode").description("응답 상태 코드"),
                fieldWithPath("status.message").description("응답 메시지"),
                fieldWithPath("status.description").optional().description("응답 상세 설명"),
                fieldWithPath("body.value").description("enum 값"),
                fieldWithPath("body.mainMessageFormat").description("메인 메시지 포맷"),
                fieldWithPath("body.contentMessageFormat").optional().description("서브 메시지 포맷"),
                fieldWithPath("body.hasContent").description("서브 메시지 제공 여부")
        );
    }
}
