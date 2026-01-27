package com.rokyai.dnd14th1backend.config;

import java.util.List;
import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
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
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;

import com.rokyai.dnd14th1backend.common.response.ApiExceptionResponse;
import com.rokyai.dnd14th1backend.common.response.SkipApiResponseWrapper;

/**
 * Swagger/OpenAPI 설정. 공통 응답 스키마를 항상 노출하여 API 문서의 일관성 보장.
 */
@Configuration
public class SwaggerConfig {

    /**
     * ApiResponse 스키마 캐시. 빈 초기화 시 한 번만 로드하여 모든 API 오퍼레이션에서 재사용.
     */
    private final Schema<?> cachedApiResponseBaseSchema;

    /**
     * ApiExceptionResponse 스키마 캐시.
     */
    private final Schema<?> cachedApiExceptionResponseSchema;

    @SuppressWarnings("unchecked")
    public SwaggerConfig() {
        Class<?> apiResponseClass = com.rokyai.dnd14th1backend.common.response.ApiResponse.class;
        Map<String, Schema<?>> apiResponseSchemas =
            (Map<String, Schema<?>>)
                (Map<?, ?>) ModelConverters.getInstance().readAll(apiResponseClass);
        this.cachedApiResponseBaseSchema = apiResponseSchemas.get(apiResponseClass.getSimpleName());

        Map<String, Schema> exceptionSchemas =
            ModelConverters.getInstance().readAll(ApiExceptionResponse.class);
        this.cachedApiExceptionResponseSchema =
            exceptionSchemas.get(ApiExceptionResponse.class.getSimpleName());
    }

    @Bean
    public OpenAPI openAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:8080");

        return new OpenAPI()
            .info(apiInfo())
            //                .addSecurityItem(
            //                        new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
            //                .components(
            //                        new Components()
            //                                .addSecuritySchemes(
            //                                        SECURITY_SCHEME_NAME,
            //                                        new SecurityScheme()
            //                                                .name(SECURITY_SCHEME_NAME)
            //                                                .type(SecurityScheme.Type.HTTP)
            //                                                .scheme("bearer")
            //                                                .bearerFormat("JWT")
            //                                        ))
            .servers(List.of(server));
    }

    /**
     * 공통 응답 스키마를 OpenAPI 문서에 추가하는 커스터마이저.
     */
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

    /**
     * 모든 API 응답을 ApiResponse 래퍼로 감싸는 커스터마이저.
     */
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

            // 200 응답을 ApiResponse로 래핑
            ApiResponse successResponse = responses.get("200");
            if (successResponse != null && successResponse.getContent() != null) {
                Content content = successResponse.getContent();
                MediaType mediaType = content.get("application/json");
                if (mediaType == null) {
                    mediaType = content.get("*/*");
                }

                if (mediaType != null && mediaType.getSchema() != null) {
                    Schema<?> originalSchema = mediaType.getSchema();
                    Schema<?> wrappedSchema = wrapWithApiResponse(originalSchema);
                    mediaType.setSchema(wrappedSchema);
                }
            }

            return operation;
        };
    }

    /**
     * 캐싱된 ApiResponse 스키마를 기반으로 원본 데이터 스키마를 래핑하여 반환.
     */
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
