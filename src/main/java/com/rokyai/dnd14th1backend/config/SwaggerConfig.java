package com.rokyai.dnd14th1backend.config;

import java.util.List;
import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import com.rokyai.dnd14th1backend.common.response.ApiExceptionResponse;
import com.rokyai.dnd14th1backend.common.response.SkipApiResponseWrapper;

/** Swagger/OpenAPI 설정. 공통 응답 스키마를 항상 노출하여 API 문서의 일관성 보장. */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Value("${springdoc.swagger-server-url}")
    private String swaggerServerUrl;

    /** ApiResponse 스키마 캐시. 빈 초기화 시 한 번만 로드하여 모든 API 오퍼레이션에서 재사용. */
    private final Schema<?> cachedApiResponseBaseSchema;

    /** ApiExceptionResponse 스키마 캐시. */
    private final Schema<?> cachedApiExceptionResponseSchema;

    @SuppressWarnings("unchecked")
    public SwaggerConfig() {
        Class<?> apiResponseClass = com.rokyai.dnd14th1backend.common.response.ApiResponse.class;
        Map<String, Schema<?>> apiResponseSchemas =
                (Map<String, Schema<?>>)
                        (Map<?, ?>) ModelConverters.getInstance().readAll(apiResponseClass);
        this.cachedApiResponseBaseSchema =
                findSchemaByName(apiResponseSchemas, apiResponseClass.getSimpleName());

        @SuppressWarnings("rawtypes")
        Map<String, Schema> exceptionSchemas =
                ModelConverters.getInstance().readAll(ApiExceptionResponse.class);
        this.cachedApiExceptionResponseSchema =
                findSchemaByName(exceptionSchemas, ApiExceptionResponse.class.getSimpleName());
    }

    /**
     * 스키마 맵에서 지정된 이름으로 스키마를 찾고, 없으면 첫 번째 항목을 반환. 제네릭 타입은 ModelConverters가 다른 키 이름을 생성할 수 있으므로 대체 탐색
     * 수행.
     *
     * @param schemas 스키마 맵
     * @param expectedName 기대하는 스키마 이름
     * @return 매칭된 스키마 (없으면 null)
     */
    private static <S> S findSchemaByName(Map<String, S> schemas, String expectedName) {
        if (schemas == null || schemas.isEmpty()) {
            return null;
        }
        S schema = schemas.get(expectedName);
        if (schema != null) {
            return schema;
        }
        return schemas.values().iterator().next();
    }

    @Bean
    public OpenAPI openAPI() {
        Server server = new Server();
        server.setUrl(swaggerServerUrl);

        return new OpenAPI()
                .info(apiInfo())
                .components(
                        new io.swagger.v3.oas.models.Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        new SecurityScheme()
                                                .name(SECURITY_SCHEME_NAME)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")))
                .servers(List.of(server));
    }

    /** 공통 응답 스키마를 OpenAPI 문서에 추가하는 커스터마이저. */
    @Bean
    public OpenApiCustomizer commonSchemaCustomizer() {
        return openApi -> {
            if (openApi.getComponents() == null) {
                openApi.setComponents(new io.swagger.v3.oas.models.Components());
            }
            if (openApi.getComponents().getSchemas() == null) {
                openApi.getComponents().setSchemas(new java.util.LinkedHashMap<>());
            }

            Map<String, Schema> schemas = openApi.getComponents().getSchemas();
            schemas.put(
                    com.rokyai.dnd14th1backend.common.response.ApiResponse.class.getSimpleName(),
                    cachedApiResponseBaseSchema);
            schemas.put(
                    ApiExceptionResponse.class.getSimpleName(), cachedApiExceptionResponseSchema);
        };
    }

    /** /api/** 경로에 JWT 인증 요구사항을 자동 적용하는 커스터마이저. /open-api/** 경로는 인증 불필요. */
    @Bean
    public OpenApiCustomizer securityRequirementCustomizer() {
        return openApi -> {
            openApi.getPaths()
                    .forEach(
                            (path, pathItem) -> {
                                if (path.startsWith("/api/")) {
                                    pathItem.readOperations()
                                            .forEach(
                                                    operation ->
                                                            operation.addSecurityItem(
                                                                    new SecurityRequirement()
                                                                            .addList(
                                                                                    SECURITY_SCHEME_NAME)));
                                }
                            });
        };
    }

    /** 모든 API의 2xx 성공 응답을 ApiResponse 래퍼로 감싸는 커스터마이저. */
    @Bean
    public OperationCustomizer apiResponseWrapperCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            // @SkipApiResponseWrapper가 있으면 래핑 제외
            if (handlerMethod.hasMethodAnnotation(SkipApiResponseWrapper.class)
                    || handlerMethod
                            .getBeanType()
                            .isAnnotationPresent(SkipApiResponseWrapper.class)) {
                return operation;
            }

            ApiResponses responses = operation.getResponses();
            if (responses == null) {
                return operation;
            }

            // 모든 2xx 성공 응답을 ApiResponse로 래핑
            responses.forEach(
                    (statusCode, response) -> {
                        if (statusCode.startsWith("2") && response.getContent() != null) {
                            wrapSuccessResponseContent(response.getContent());
                        }
                    });

            return operation;
        };
    }

    /** 성공 응답의 Content에서 JSON 스키마를 찾아 ApiResponse 래퍼로 감싼다. */
    private void wrapSuccessResponseContent(Content content) {
        MediaType mediaType = content.get("application/json");
        if (mediaType == null) {
            mediaType = content.get("*/*");
        }

        if (mediaType != null && mediaType.getSchema() != null) {
            Schema<?> originalSchema = mediaType.getSchema();
            mediaType.setSchema(wrapWithApiResponse(originalSchema));
        }
    }

    /** 캐싱된 ApiResponse 스키마를 기반으로 원본 데이터 스키마를 래핑하여 반환. */
    private Schema<?> wrapWithApiResponse(Schema<?> dataSchema) {
        if (cachedApiResponseBaseSchema != null
                && cachedApiResponseBaseSchema.getProperties() != null) {
            Schema<Object> wrappedSchema = new Schema<>();
            wrappedSchema.setDescription(cachedApiResponseBaseSchema.getDescription());
            wrappedSchema.setProperties(
                    new java.util.LinkedHashMap<>(cachedApiResponseBaseSchema.getProperties()));
            wrappedSchema.getProperties().put("data", dataSchema);
            wrappedSchema.setRequired(cachedApiResponseBaseSchema.getRequired());
            return wrappedSchema;
        }

        return dataSchema;
    }

    private Info apiInfo() {
        return new Info().title("DND API 1조 Swagger 문서").version("0.0.1");
    }
}
