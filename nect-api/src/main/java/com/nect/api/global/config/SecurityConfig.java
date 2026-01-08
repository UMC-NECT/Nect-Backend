package com.nect.api.global.config;

import com.nect.api.global.jwt.JwtAuthenticationFilter;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.security.UserDetailsServiceImpl;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.core.AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**",
            "/static/**", "/webjars/**",
            "/login/oauth2/**", "/oauth2/**",
            "/actuator/**", "/health", "/error", "/favicon.ico",
            "/api/members/search-name", "/api/members/test-login", "/api/members/refresh",
            "/api/upload/image/**"
    );

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(formLogin -> formLogin.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(EXCLUDE_PATHS.toArray(new String[0])).permitAll()
                        .anyRequest().authenticated()
                )
                // TODO: OAuth2 설정 추가 필요
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint())
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService, tokenBlacklistService, EXCLUDE_PATHS);
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"status\":{\"statusCode\":\"C401\",\"message\":\"Unauthorized\",\"description\":\"" + authException.getMessage() + "\"}}"
            );
        };
    }
}
