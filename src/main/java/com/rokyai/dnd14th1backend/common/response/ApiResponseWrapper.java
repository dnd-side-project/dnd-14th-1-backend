package com.rokyai.dnd14th1backend.common.response;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 컨트롤러 응답을 ApiResponse로 자동 래핑. ApiResponse나 ApiExceptionResponse가 아닌 응답만 래핑 처리. Swagger 관련 경로는 래핑에서
 * 제외.
 */
@RestControllerAdvice(basePackages = "com.rokyai.dnd14th1backend")
public class ApiResponseWrapper implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(
            MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // @SkipApiResponseWrapper 어노테이션이 있으면 래핑 제외
        if (returnType.hasMethodAnnotation(SkipApiResponseWrapper.class)) {
            return false;
        }
        if (returnType.getContainingClass().isAnnotationPresent(SkipApiResponseWrapper.class)) {
            return false;
        }
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        String path = request.getURI().getPath();

        // Swagger 관련 경로는 래핑 제외
        if (isSwaggerPath(path)) {
            return body;
        }

        // 이미 ApiResponse 계열이면 그대로 반환
        if (body instanceof ApiResponse) {
            return body;
        }

        ApiResponse<Object> apiResponse = ApiResponse.success(body);

        // String 반환 타입은 StringHttpMessageConverter를 사용하므로 JSON 문자열로 직접 변환
        if (selectedConverterType.isAssignableFrom(StringHttpMessageConverter.class)) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            try {
                return objectMapper.writeValueAsString(apiResponse);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("ApiResponse 직렬화 실패", e);
            }
        }

        return apiResponse;
    }

    private boolean isSwaggerPath(String path) {
        return path.contains("/swagger-ui")
                || path.contains("/v3/api-docs")
                || path.contains("/swagger-resources");
    }
}
