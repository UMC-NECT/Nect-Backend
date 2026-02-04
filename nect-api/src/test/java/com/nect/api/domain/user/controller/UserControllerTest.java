package com.nect.api.domain.user.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.NectDocumentApiTester;
import com.nect.api.domain.user.dto.AgreeDto;
import com.nect.api.domain.user.dto.DuplicateCheckDto;
import com.nect.api.domain.user.dto.LoginDto;
import com.nect.api.domain.user.dto.ProfileDto;
import com.nect.api.domain.user.dto.SignUpDto;
import com.nect.api.domain.user.enums.CheckType;
import com.nect.core.entity.user.enums.SkillCategory;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends NectDocumentApiTester {

    @Test
    void checkDuplicate() throws Exception {
        // given
        when(userService.checkDuplicate(any(DuplicateCheckDto.DuplicateCheckRequestDto.class))).thenReturn(false);

        // when
        this.mockMvc.perform(post("/api/v1/users/check")
                        .contentType("application/json")
                        .content(toJson(new DuplicateCheckDto.DuplicateCheckRequestDto(
                                CheckType.EMAIL,
                                "test@example.com"
                        ))))
                .andExpect(status().isOk())
                .andDo(document("user-check-duplicate",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("users")
                                        .summary("중복 검사 (이메일/전화번호/닉네임)")
                                        .description("이메일 또는 전화번호, 닉네임의 중복 여부를 확인합니다. available이 true이면 사용 가능, false이면 이미 사용 중입니다. 검사 type은 - EMAIL: 이메일 중복검사, PHONE: 전화번호 중복검사, NICKNAME: 닉네임 중복검사")
                                        .requestFields(
                                                fieldWithPath("type").type(JsonFieldType.STRING).description("검사 타입 (EMAIL, PHONE, NICKNAME)"),
                                                fieldWithPath("value").type(JsonFieldType.STRING).description("검사할 값 (이메일 또는 전화번호, 닉네임)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body.available").type(JsonFieldType.BOOLEAN).description("사용 가능 여부")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void signUp() throws Exception {
        // given
        doNothing().when(userService).signUp(any(SignUpDto.SignUpRequestDto.class));

        // when
        this.mockMvc.perform(post("/api/v1/users/signup")
                        .contentType("application/json")
                        .content(toJson(new SignUpDto.SignUpRequestDto(
                                "test@example.com",
                                "password123",
                                "password123",
                                "김테스트",
                                "01012345678"
                        ))))
                .andExpect(status().isOk())
                .andDo(document("user-signup",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("users")
                                        .summary("회원가입")
                                        .description("새로운 계정을 생성합니다. 닉네임, 생년월일, 직업, 역할 등은 프로필 설정 API에서 입력합니다.")
                                        .requestFields(
                                                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일 (고유값, 이메일 형식)"),
                                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호 (최소 8자)"),
                                                fieldWithPath("passwordConfirm").type(JsonFieldType.STRING).description("비밀번호 확인 (password와 일치해야 함)"),
                                                fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
                                                fieldWithPath("phoneNumber").type(JsonFieldType.STRING).description("전화번호").optional()
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional()
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void login() throws Exception {
        // given
        LoginDto.LoginResponseDto responseDto = LoginDto.LoginResponseDto.of(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                System.currentTimeMillis() + 3600000,
                System.currentTimeMillis() + 86400000,
                false
        );
        when(userService.login(any(LoginDto.LoginRequestDto.class))).thenReturn(responseDto);

        // when
        this.mockMvc.perform(post("/api/v1/users/login")
                        .contentType("application/json")
                        .content(toJson(new LoginDto.LoginRequestDto(
                                "test@example.com",
                                "password123",
                                false
                        ))))
                .andExpect(status().isOk())
                .andDo(document("user-login",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("users")
                                        .summary("로그인")
                                        .description("이메일과 비밀번호로 로그인합니다. 성공 시 액세스 토큰과 리프레시 토큰을 발급합니다. autoLoginEnabled가 true면 자동 로그인 활성화, false면 비활성화입니다.")
                                        .requestFields(
                                                fieldWithPath("email").type(JsonFieldType.STRING).description("로그인 이메일"),
                                                fieldWithPath("password").type(JsonFieldType.STRING).description("로그인 비밀번호"),
                                                fieldWithPath("autoLoginEnabled").type(JsonFieldType.BOOLEAN).description("자동 로그인 여부").optional()
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body.grantType").type(JsonFieldType.STRING).description("토큰 타입 (Bearer)"),
                                                fieldWithPath("body.accessToken").type(JsonFieldType.STRING).description("액세스 토큰 (API 요청 시 Authorization 헤더에 사용)"),
                                                fieldWithPath("body.refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰 (액세스 토큰 만료 시 갱신에 사용)"),
                                                fieldWithPath("body.accessTokenExpiredAt").type(JsonFieldType.NUMBER).description("액세스 토큰 만료 시간 (Unix timestamp)"),
                                                fieldWithPath("body.refreshTokenExpiredAt").type(JsonFieldType.NUMBER).description("리프레시 토큰 만료 시간 (Unix timestamp)"),
                                                fieldWithPath("body.isOnboardingCompleted").type(JsonFieldType.BOOLEAN).description("온보딩 완료 여부")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void logout() throws Exception {
        // given
        doNothing().when(userService).logout(any());

        // when
        this.mockMvc.perform(post("/api/v1/users/logout")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("user-logout",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("users")
                                        .summary("로그아웃")
                                        .description("현재 사용자를 로그아웃합니다. 현재 토큰은 블랙리스트에 추가되어 더 이상 사용할 수 없습니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional()
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void refreshToken() throws Exception {
        // given
        LoginDto.TokenResponseDto responseDto = LoginDto.TokenResponseDto.of(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                System.currentTimeMillis() + 3600000,
                System.currentTimeMillis() + 86400000
        );
        when(userService.refreshToken(anyString())).thenReturn(responseDto);

        // when
        this.mockMvc.perform(post("/api/v1/users/refresh")
                        .contentType("application/json")
                        .content(toJson(new LoginDto.RefreshTokenRequestDto(
                                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                        ))))
                .andExpect(status().isOk())
                .andDo(document("user-refresh-token",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("users")
                                        .summary("토큰 갱신")
                                        .description("리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다. 리프레시 토큰도 함께 갱신됩니다.")
                                        .requestFields(
                                                fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰 (로그인 시 발급받음)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body.grantType").type(JsonFieldType.STRING).description("토큰 타입 (Bearer)"),
                                                fieldWithPath("body.accessToken").type(JsonFieldType.STRING).description("새로운 액세스 토큰"),
                                                fieldWithPath("body.refreshToken").type(JsonFieldType.STRING).description("새로운 리프레시 토큰"),
                                                fieldWithPath("body.accessTokenExpiredAt").type(JsonFieldType.NUMBER).description("액세스 토큰 만료 시간 (Unix timestamp)"),
                                                fieldWithPath("body.refreshTokenExpiredAt").type(JsonFieldType.NUMBER).description("리프레시 토큰 만료 시간 (Unix timestamp)")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void testLogin() throws Exception {
        // given
        LoginDto.TokenResponseDto responseDto = LoginDto.TokenResponseDto.of(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                System.currentTimeMillis() + 3600000,
                System.currentTimeMillis() + 86400000
        );
        when(userService.testLoginByEmail(any(LoginDto.TestLoginRequestDto.class))).thenReturn(responseDto);

        // when
        this.mockMvc.perform(post("/api/v1/users/test-login")
                        .contentType("application/json")
                        .content(toJson(new LoginDto.TestLoginRequestDto(
                                "test@example.com",
                                "a9F3kLmP7wQzX1bC"
                        ))))
                .andExpect(status().isOk())
                .andDo(document("user-test-login",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("users")
                                        .summary("테스트용 로그인")
                                        .description("테스트 환경에서 사용하는 로그인입니다. 이메일과 인증 키를 사용하여 토큰을 발급받습니다. 프로덕션 환경에서는 사용할 수 없습니다.")
                                        .requestFields(
                                                fieldWithPath("email").type(JsonFieldType.STRING).description("테스트할 이메일"),
                                                fieldWithPath("key").type(JsonFieldType.STRING).description("인증 키 (application.yml의 app.auth.key)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body.grantType").type(JsonFieldType.STRING).description("토큰 타입 (Bearer)"),
                                                fieldWithPath("body.accessToken").type(JsonFieldType.STRING).description("액세스 토큰"),
                                                fieldWithPath("body.refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰"),
                                                fieldWithPath("body.accessTokenExpiredAt").type(JsonFieldType.NUMBER).description("액세스 토큰 만료 시간 (Unix timestamp)"),
                                                fieldWithPath("body.refreshTokenExpiredAt").type(JsonFieldType.NUMBER).description("리프레시 토큰 만료 시간 (Unix timestamp)")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void agree() throws Exception {
        // given
        doNothing().when(userService).agree(anyLong(), any(AgreeDto.AgreeRequestDto.class));

        // when
        this.mockMvc.perform(post("/api/v1/users/agree")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType("application/json")
                        .content(toJson(new AgreeDto.AgreeRequestDto(
                                true,
                                true,
                                false
                        ))))
                .andExpect(status().isOk())
                .andDo(document("user-agree",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("users")
                                        .summary("개인정보 동의")
                                        .description("서비스 이용약관과 개인정보 수집 이용에 동의합니다. 두 가지 동의는 필수이며, 마케팅 정보 수신은 선택입니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                        )
                                        .requestFields(
                                                fieldWithPath("termsAgreed").type(JsonFieldType.BOOLEAN).description("서비스 이용약관 동의 (필수)"),
                                                fieldWithPath("privacyAgreed").type(JsonFieldType.BOOLEAN).description("개인정보 수집 이용 동의 (필수)"),
                                                fieldWithPath("marketingAgreed").type(JsonFieldType.BOOLEAN).description("마케팅 정보 수신 동의 (선택)").optional()
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional()
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void getEmail() throws Exception {
        // given
        LoginDto.EmailResponseDto responseDto = new LoginDto.EmailResponseDto("test@example.com");
        when(userService.getEmailByUserId(anyLong())).thenReturn(responseDto);

        // when
        this.mockMvc.perform(get("/api/v1/users/email")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("user-get-email",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("users")
                                        .summary("이메일 조회")
                                        .description("액세스 토큰으로 현재 사용자의 이메일을 조회합니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body.email").type(JsonFieldType.STRING).description("사용자 이메일")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void setupProfile() throws Exception {
        // given
        doNothing().when(userService).setupProfile(anyLong(), any(ProfileDto.ProfileSetupRequestDto.class));

        // when
        this.mockMvc.perform(post("/api/v1/users/profile/setup")
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .contentType("application/json")
                        .content(toJson(new ProfileDto.ProfileSetupRequestDto(
                                "testNickname",
                                "19990315",
                                "EMPLOYEE",
                                "DEVELOPER",
                                List.of(
                                        new ProfileDto.FieldDto("BACKEND", null),
                                        new ProfileDto.FieldDto("FULLSTACK", null)
                                ),
                                List.of(
                                        new ProfileDto.SkillDto(SkillCategory.DEVELOPMENT, "REACT", null),
                                        new ProfileDto.SkillDto(SkillCategory.DEVELOPMENT, "TYPESCRIPT", null)
                                ),
                                List.of("IT_WEB_MOBILE", "FINANCE_FINTECH"),
                                "PORTFOLIO",
                                new ProfileDto.CollaborationStyleDto(3, 4, 2)
                        ))))
                .andExpect(status().isOk())
                .andDo(document("user-setup-profile",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("users")
                                        .summary("프로필 설정")
                                        .description("사용자의 프로필 정보를 설정합니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                        )
                                        .requestFields(
                                                fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임 (필수, 고유값)"),
                                                fieldWithPath("birthDate").type(JsonFieldType.STRING).description("생년월일 (선택, 8자리 형식 YYYYMMDD 예: 19990315)").optional(),
                                                fieldWithPath("job").type(JsonFieldType.STRING).description("직업 (필수, 예: EMPLOYEE, STUDENT, JOB_SEEKER, FREELANCER, BUSINESSMAN, OTHER)"),
                                                fieldWithPath("role").type(JsonFieldType.STRING).description("역할 (필수, 예: DESIGNER, DEVELOPER, PLANNER, MARKETER, OTHER)"),
                                                fieldWithPath("fields[].field").type(JsonFieldType.STRING).description("직종 (필수 - 1개 이상, 예: UI_UX, MOTION_3D, ILLUSTRATION_GRAPHIC 등)"),
                                                fieldWithPath("fields[].customField").type(JsonFieldType.STRING).description("직종 직접입력 (field가 CUSTOM일 때만 사용)").optional(),
                                                fieldWithPath("skills[].skillCategory").type(JsonFieldType.STRING).description("스킬 카테고리 (필수, 예: DESIGN, DEVELOPMENT, PLANNING, MARKETING, OTHER)"),
                                                fieldWithPath("skills[].skill").type(JsonFieldType.STRING).description("스킬 (Enum 값, 예: REACT, JAVA, FIGMA, CUSTOM 등)"),
                                                fieldWithPath("skills[].customSkillName").type(JsonFieldType.STRING).description("스킬 직접입력 (skill이 CUSTOM일 때만 사용)").optional(),
                                                fieldWithPath("interests").type(JsonFieldType.ARRAY).description("관심 분야 배열 (Enum 값, 예: IT_WEB_MOBILE, FINANCE_FINTECH 등) (선택)").optional(),
                                                fieldWithPath("firstGoal").type(JsonFieldType.STRING).description("첫번째 목표 (선택, 예: PORTFOLIO, TEAM_EXPERIENCE, OTHER_FIELD, ETC)").optional(),
                                                fieldWithPath("collaborationStyle.planning").type(JsonFieldType.NUMBER).description("협업 스타일 - 계획형(1) vs 실행형(5) (선택, 1-5)").optional(),
                                                fieldWithPath("collaborationStyle.logic").type(JsonFieldType.NUMBER).description("협업 스타일 - 논리형(1) vs 공감형(5) (선택, 1-5)").optional(),
                                                fieldWithPath("collaborationStyle.leadership").type(JsonFieldType.NUMBER).description("협업 스타일 - 리더형(1) vs 서포터형(5) (선택, 1-5)").optional()
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional()
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void getUserInfo() throws Exception {
        // given
        ProfileDto.UserInfoResponseDto responseDto = new ProfileDto.UserInfoResponseDto(
                "김준형",
                "개발자",
                "test@example.com"
        );
        when(userService.getUserInfo(anyLong())).thenReturn(responseDto);

        // when
        this.mockMvc.perform(get("/api/v1/users/info")
                        .contentType("application/json")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
                .andExpect(status().isOk())
                .andDo(document("user-get-info",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("users")
                                        .summary("사용자 기본 정보 조회")
                                        .description("인증된 사용자의 기본 정보(이름, 역할, 이메일)를 조회합니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body.name").type(JsonFieldType.STRING).description("사용자 이름"),
                                                fieldWithPath("body.role").type(JsonFieldType.STRING).description("사용자 역할 (한국어: 개발자, 디자이너, 기획자, 마케터)"),
                                                fieldWithPath("body.email").type(JsonFieldType.STRING).description("사용자 이메일")
                                        )
                                        .build()
                        )
                ));
    }
}
