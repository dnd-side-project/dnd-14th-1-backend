package com.rokyai.dnd14th1backend.auth.filter;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.rokyai.dnd14th1backend.auth.exception.AuthStatus;
import com.rokyai.dnd14th1backend.common.response.ApiExceptionResponse;

/** 인증 실패 시 프로젝트 표준 에러 응답 형식으로 반환하는 EntryPoint */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException {
        AuthStatus status =
                (AuthStatus)
                        request.getAttribute(
                                JwtAuthenticationFilter.AUTH_EXCEPTION_STATUS_ATTRIBUTE);
        if (status == null) {
            status = AuthStatus.EXPIRED_TOKEN;
        }

        response.setStatus(status.getHttpStatusCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiExceptionResponse<String> errorResponse =
                new ApiExceptionResponse<>(status, null, status.getDescription());
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
