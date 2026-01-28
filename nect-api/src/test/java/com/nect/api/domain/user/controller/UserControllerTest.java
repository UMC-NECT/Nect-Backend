package com.nect.api.domain.user.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.NectDocumentApiTester;
import com.nect.api.domain.user.dto.AgreeDto;
import com.nect.api.domain.user.dto.DuplicateCheckDto;
import com.nect.api.domain.user.dto.LoginDto;
import com.nect.api.domain.user.dto.SignUpDto;
import com.nect.api.domain.user.enums.CheckType;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;

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
                                        .summary("중복 검사 (이메일/전화번호)")
                                        .description("이메일 또는 전화번호의 중복 여부를 확인합니다. available이 true이면 사용 가능, false이면 이미 사용 중입니다. 검사 type은 - EMAIL: 이메일 중복검사, PHONE: 전화번호 중복검사")
                                        .requestFields(
                                                fieldWithPath("type").type(JsonFieldType.STRING).description("검사 타입 (EMAIL, PHONE)"),
                                                fieldWithPath("value").type(JsonFieldType.STRING).description("검사할 값 (이메일 또는 전화번호)")
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
                                "testuser",
                                "01012345678",
                                LocalDate.of(1999, 1, 1),
                                "EMPLOYEE"
                        ))))
                .andExpect(status().isOk())
                .andDo(document("user-signup",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("users")
                                        .summary("회원가입")
                                        .description("새로운 계정을 생성합니다")
                                        .requestFields(
                                                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일 (고유값, 이메일 형식)"),
                                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호 (최소 8자)"),
                                                fieldWithPath("passwordConfirm").type(JsonFieldType.STRING).description("비밀번호 확인 (password와 일치해야 함)"),
                                                fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
                                                fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                                                fieldWithPath("phoneNumber").type(JsonFieldType.STRING).description("전화번호").optional(),
                                                fieldWithPath("birthDate").type(JsonFieldType.STRING).description("생년월일 (yyyy-MM-dd)").optional(),
                                                fieldWithPath("job").type(JsonFieldType.STRING).description("직업 (EMPLOYEE, STUDENT, JOB_SEEKER, FREELANCER, BUSINESS_OWNER, OTHER)")
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
                System.currentTimeMillis() + 86400000
        );
        when(userService.login(any(LoginDto.LoginRequestDto.class))).thenReturn(responseDto);

        // when
        this.mockMvc.perform(post("/api/v1/users/login")
                        .contentType("application/json")
                        .content(toJson(new LoginDto.LoginRequestDto(
                                "test@example.com",
                                "password123"
                        ))))
                .andExpect(status().isOk())
                .andDo(document("user-login",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("users")
                                        .summary("로그인")
                                        .description("이메일과 비밀번호로 로그인합니다. 성공 시 액세스 토큰과 리프레시 토큰을 발급합니다.")
                                        .requestFields(
                                                fieldWithPath("email").type(JsonFieldType.STRING).description("로그인 이메일"),
                                                fieldWithPath("password").type(JsonFieldType.STRING).description("로그인 비밀번호")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body.grantType").type(JsonFieldType.STRING).description("토큰 타입 (Bearer)"),
                                                fieldWithPath("body.accessToken").type(JsonFieldType.STRING).description("액세스 토큰 (API 요청 시 Authorization 헤더에 사용)"),
                                                fieldWithPath("body.refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰 (액세스 토큰 만료 시 갱신에 사용)"),
                                                fieldWithPath("body.accessTokenExpiredAt").type(JsonFieldType.NUMBER).description("액세스 토큰 만료 시간 (Unix timestamp)"),
                                                fieldWithPath("body.refreshTokenExpiredAt").type(JsonFieldType.NUMBER).description("리프레시 토큰 만료 시간 (Unix timestamp)")
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
        LoginDto.LoginResponseDto responseDto = LoginDto.LoginResponseDto.of(
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
        LoginDto.LoginResponseDto responseDto = LoginDto.LoginResponseDto.of(
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
}
