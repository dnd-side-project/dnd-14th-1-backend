package com.rokyai.dnd14th1backend.crawling.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

import com.rokyai.dnd14th1backend.crawling.domain.CrawlingTask;
import com.rokyai.dnd14th1backend.crawling.enums.CrawlingStatus;

/** 크롤링 상태 응답 DTO */
@Schema(description = "크롤링 상태 응답")
public record CrawlingStatusResponse(
        @Schema(description = "작업 ID") UUID taskId,
        @Schema(description = "크롤링 상태", example = "IN_PROGRESS") CrawlingStatus status,
        @Schema(description = "에러 메시지 (실패 시에만 포함)") String errorMessage) {

    /**
     * CrawlingTask에서의 상태 응답 생성
     *
     * @param task 크롤링 작업
     * @return 상태 응답
     */
    public static CrawlingStatusResponse from(CrawlingTask task) {
        return new CrawlingStatusResponse(task.getId(), task.getStatus(), task.getErrorMessage());
    }
}
