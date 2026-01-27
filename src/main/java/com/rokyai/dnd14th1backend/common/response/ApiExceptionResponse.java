package com.rokyai.dnd14th1backend.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * API 예외 응답 클래스. 에러 발생 시 customStatusCode를 포함한 상세 응답 제공.
 *
 * @param <T> 에러 상세 정보 타입
 */
@Getter
@Schema(description = "API 예외 응답")
public class ApiExceptionResponse<T> extends ApiResponse<T> {

    @Schema(
            description = "커스텀 상태 코드 (도메인별 에러 구분용)",
            example = "4001",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private final int customStatusCode;

    public ApiExceptionResponse(StatusInterface status) {
        super(status.getHttpStatusCode(), null, status.getDescription());
        this.customStatusCode = status.getCustomStatusCode();
    }

    public ApiExceptionResponse(StatusInterface status, T data) {
        super(status.getHttpStatusCode(), data, status.getDescription());
        this.customStatusCode = status.getCustomStatusCode();
    }

    public ApiExceptionResponse(StatusInterface status, T data, String message) {
        super(status.getHttpStatusCode(), data, message);
        this.customStatusCode = status.getCustomStatusCode();
    }

    /**
     * 에러 응답 생성 (데이터 없음)
     *
     * @param status 상태 정보
     * @return ApiExceptionResponse 인스턴스
     */
    public static <T> ApiExceptionResponse<T> error(StatusInterface status) {
        return new ApiExceptionResponse<>(status);
    }

    /**
     * 에러 응답 생성 (데이터 포함)
     *
     * @param status 상태 정보
     * @param data 에러 상세 데이터
     * @return ApiExceptionResponse 인스턴스
     */
    public static <T> ApiExceptionResponse<T> error(StatusInterface status, T data) {
        return new ApiExceptionResponse<>(status, data);
    }
}
