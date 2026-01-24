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

/** Swagger/OpenAPI 설정. 공통 응답 스키마를 항상 노출하여 API 문서의 일관성 보장. */
@Configuration
public class SwaggerConfig {

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
                    createApiResponseSchema());
            schemas.put(
                    ApiExceptionResponse.class.getSimpleName(), createApiExceptionResponseSchema());
        };
    }

    /** 모든 API 응답을 ApiResponse 래퍼로 감싸는 커스터마이저. */
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

    /** 원본 데이터 스키마를 ApiResponse 형태로 래핑하여 반환. ApiResponse 클래스의 @Schema 어노테이션에서 메타데이터를 가져와 일관성 유지. */
    @SuppressWarnings("unchecked")
    private Schema<?> wrapWithApiResponse(Schema<?> dataSchema) {
        Class<?> apiResponseClass = com.rokyai.dnd14th1backend.common.response.ApiResponse.class;
        Map<String, Schema<?>> apiResponseSchemas =
                (Map<String, Schema<?>>)
                        (Map<?, ?>) ModelConverters.getInstance().readAll(apiResponseClass);
        Schema<?> baseSchema = apiResponseSchemas.get(apiResponseClass.getSimpleName());

        if (baseSchema != null && baseSchema.getProperties() != null) {
            Schema<Object> wrappedSchema = new Schema<>();
            wrappedSchema.setDescription(baseSchema.getDescription());
            wrappedSchema.setProperties(new java.util.LinkedHashMap<>(baseSchema.getProperties()));
            wrappedSchema.getProperties().put("data", dataSchema);
            wrappedSchema.setRequired(baseSchema.getRequired());
            return wrappedSchema;
        }

        return dataSchema;
    }

    /** ApiExceptionResponse 클래스의 @Schema 어노테이션을 기반으로 예외 응답 스키마 생성. */
    private Schema<?> createApiExceptionResponseSchema() {
        Map<String, Schema> schemas =
                ModelConverters.getInstance().readAll(ApiExceptionResponse.class);
        return schemas.get(ApiExceptionResponse.class.getSimpleName());
    }

    /** ApiResponse 클래스의 @Schema 어노테이션을 기반으로 공통 응답 스키마 생성. */
    private Schema<?> createApiResponseSchema() {
        Map<String, Schema> schemas =
                ModelConverters.getInstance()
                        .readAll(com.rokyai.dnd14th1backend.common.response.ApiResponse.class);
        return schemas.get(
                com.rokyai.dnd14th1backend.common.response.ApiResponse.class.getSimpleName());
    }

    private Info apiInfo() {
        return new Info().title("DND API 1조 Swagger 문서").version("0.0.1");
    }
}
