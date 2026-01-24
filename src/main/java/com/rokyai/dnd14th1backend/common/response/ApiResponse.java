package com.rokyai.dnd14th1backend.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * API 공통 응답 래퍼 클래스. 모든 API 응답은 이 형식으로 자동 래핑됨.
 *
 * @param <T> 응답 데이터 타입
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "API 공통 응답")
public class ApiResponse<T> {

    @Schema(
            description = "HTTP 상태 코드",
            example = "200",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private int status;

    @Schema(description = "응답 데이터", requiredMode = Schema.RequiredMode.REQUIRED)
    private T data;

    @Schema(description = "응답 메시지", example = "성공", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    /**
     * 성공 응답 생성 (데이터 포함)
     *
     * @param data 응답 데이터
     * @return ApiResponse 인스턴스
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, data, "성공");
    }

    /**
     * 성공 응답 생성 (커스텀 메시지)
     *
     * @param data 응답 데이터
     * @param message 응답 메시지
     * @return ApiResponse 인스턴스
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(200, data, message);
    }
}
