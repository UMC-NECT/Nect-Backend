package com.nect.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.user.service.UserService;
import com.nect.api.global.jwt.JwtAuthenticationFilter;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsServiceImpl;
import com.nect.api.global.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@SpringBootTest
public class NectDocumentApiTester {

    protected MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected JwtUtil jwtUtil;

    @MockitoBean
    protected UserService userService;

    @MockitoBean
    protected UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    protected TokenBlacklistService tokenBlacklistService;

    protected final static String AUTH_HEADER = "Authorization";
    protected final static String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(restDocumentation))
                .addFilter(new JwtAuthenticationFilter(jwtUtil, userDetailsService, tokenBlacklistService, List.of("/swagger-ui/**", "/api-docs/**")))
                .build();

        doNothing().when(jwtUtil).validateToken(anyString());
        given(jwtUtil.getUserIdFromToken(anyString())).willReturn(1L);
        UserDetailsImpl mockUserDetails = UserDetailsImpl.builder()
                .userId(1L)
                .roles(List.of("ROLE_USER"))
                .build();
        given(userDetailsService.loadUserByUsername(anyString())).willReturn(mockUserDetails);

        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                mockUserDetails, null, mockUserDetails.getAuthorities()
        );
        securityContext.setAuthentication(authentication);
    }

    /**
     * 인증이 필요한 API 테스트를 위한 헬퍼 메서드
     */
    protected Authentication createMockAuthentication(Long userId, String email) {
        return new UsernamePasswordAuthenticationToken(
                userId, email, new ArrayList<>()
        );
    }

    /**
     * JSON 변환 헬퍼 메서드
     */
    protected String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}
