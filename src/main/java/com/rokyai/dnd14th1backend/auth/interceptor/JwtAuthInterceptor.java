package com.rokyai.dnd14th1backend.auth.interceptor;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import com.rokyai.dnd14th1backend.auth.exception.AuthException;
import com.rokyai.dnd14th1backend.auth.exception.AuthStatus;
import com.rokyai.dnd14th1backend.auth.provider.JwtTokenProvider;

@Slf4j
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final boolean securityEnabled;

    public JwtAuthInterceptor(
            JwtTokenProvider jwtTokenProvider,
            @Value("${app.security.enabled:true}") boolean securityEnabled) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.securityEnabled = securityEnabled;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 인증하지 않고 크롤링 수행 (개발용)
        if (!securityEnabled) {
            log.debug("보안이 비활성화되어 인증을 스킵합니다");
            return true;
        }

        String token = extractToken(request);
        if (token == null) {
            throw new AuthException(AuthStatus.INVALID_OAUTH_REQUEST, "Authorization 헤더가 없습니다");
        }

        if (!jwtTokenProvider.validateToken(token)) {
            throw new AuthException(AuthStatus.EXPIRED_TOKEN);
        }

        String userIdStr = jwtTokenProvider.extractUserId(token);
        try {
            UUID userId = UUID.fromString(userIdStr);
            request.setAttribute("userId", userId);
        } catch (IllegalArgumentException e) {
            throw new AuthException(AuthStatus.INVALID_OAUTH_REQUEST, "잘못된 사용자 ID 형식입니다");
        }

        return true;
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
