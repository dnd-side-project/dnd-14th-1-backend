package com.rokyai.dnd14th1backend.common.pagination;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Cursor 기반 페이지네이션 요청 파라미터.
 *
 * @param cursor 이전 페이지의 마지막 항목 ID (첫 페이지일 경우 null)
 * @param size 페이지 크기 (기본 20, 최대 100)
 */
@Schema(description = "Cursor 기반 페이지네이션 요청")
public record CursorPageRequest(
        @Schema(description = "이전 페이지 마지막 항목의 ID (첫 페이지는 생략)", nullable = true) UUID cursor,
        @Schema(description = "페이지 크기", defaultValue = "20") @Min(1) @Max(100) Integer size) {

    public CursorPageRequest {
        if (size == null) {
            size = 20;
        }
    }
}
