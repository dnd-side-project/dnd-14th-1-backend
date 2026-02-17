package com.rokyai.dnd14th1backend.auth.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import com.rokyai.dnd14th1backend.auth.exception.AuthException;
import com.rokyai.dnd14th1backend.auth.exception.AuthStatus;
import com.rokyai.dnd14th1backend.auth.provider.JwtTokenProvider;

/** JWT 토큰을 검증하고 SecurityContext에 인증 정보를 설정하는 필터 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final boolean securityEnabled;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            @Value("${app.security.enabled:true}") boolean securityEnabled) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.securityEnabled = securityEnabled;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!securityEnabled) {
            log.debug("보안이 비활성화되어 인증을 스킵합니다");
            setAuthentication(UUID.fromString("00000000-0000-0000-0000-000000000000"));
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String userIdStr = jwtTokenProvider.extractUserId(token);
            try {
                UUID userId = UUID.fromString(userIdStr);
                setAuthentication(userId);
            } catch (IllegalArgumentException e) {
                throw new AuthException(AuthStatus.INVALID_OAUTH_REQUEST, "잘못된 사용자 ID 형식입니다");
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * SecurityContext에 인증 정보를 설정합니다.
     *
     * @param userId 사용자 ID
     */
    private void setAuthentication(UUID userId) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Authorization 헤더에서 토큰을 추출합니다.
     *
     * @param request HTTP 요청
     * @return JWT 토큰 (없으면 null)
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
