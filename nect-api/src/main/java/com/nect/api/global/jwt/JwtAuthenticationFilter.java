package com.nect.api.global.jwt;

import com.nect.api.global.jwt.exception.ExpiredJwtTokenException;
import com.nect.api.global.jwt.exception.InvalidJwtTokenException;
import com.nect.api.global.jwt.exception.JwtClaimsEmptyException;
import com.nect.api.global.jwt.exception.UnsupportedJwtTokenException;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final List<String> excludePaths;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // Skip authentication for excluded paths
            if (shouldSkipAuthentication(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = extractTokenFromRequest(request);

            if (StringUtils.hasText(token)) {
                authenticateToken(token);
            }

            filterChain.doFilter(request, response);

        } catch (InvalidJwtTokenException e) {
            handleJwtException(response, "J002", "Invalid JWT token", e.getMessage());
        } catch (ExpiredJwtTokenException e) {
            handleJwtException(response, "J001", "Expired JWT token", e.getMessage());
        } catch (UnsupportedJwtTokenException e) {
            handleJwtException(response, "J004", "Unsupported JWT token", e.getMessage());
        } catch (JwtClaimsEmptyException e) {
            handleJwtException(response, "J003", "JWT Claims is empty", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in JWT filter: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            handleException(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "C999", "Internal server error", e.getMessage());
        }
    }

    private boolean shouldSkipAuthentication(HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        if (excludePaths.contains("*")) {
            return true;
        }

        return excludePaths.stream().anyMatch(path ->
                pathMatcher.match(path, requestURI)
        );
    }

    private void authenticateToken(String token) {
        jwtUtil.validateToken(token);

        if (tokenBlacklistService.isBlacklisted(token)) {
            throw new InvalidJwtTokenException();
        }

        Long memberId = jwtUtil.getUserIdFromToken(token);

        UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(memberId));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Set Authentication to SecurityContext for member: {}", memberId);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private void handleJwtException(HttpServletResponse response, String statusCode, String message, String description) throws IOException {
        log.error("JWT authentication failed - {}: {}", message, description);
        SecurityContextHolder.clearContext();
        handleException(response, HttpServletResponse.SC_UNAUTHORIZED, statusCode, message, description);
    }

    private void handleException(HttpServletResponse response, int status, String statusCode, String message, String description) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format("""
            {
              "status": {
                "statusCode": "%s",
                "message": "%s",
                "description": "%s"
              }
            }
            """, statusCode, message, description));
    }
}
