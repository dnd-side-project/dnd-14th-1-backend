package com.rokyai.dnd14th1backend.crawling.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

import com.rokyai.dnd14th1backend.crawling.domain.CrawlingTask;
import com.rokyai.dnd14th1backend.crawling.enums.CrawlingStatus;
import com.rokyai.dnd14th1backend.crawling.enums.Platform;

/** 크롤링 작업 요약 DTO */
@Schema(description = "크롤링 작업 요약")
public record CrawlingTaskSummary(
        @Schema(description = "작업 ID") UUID taskId,
        @Schema(description = "사용자 ID") UUID userId,
        @Schema(description = "원본 URL") String sourceUrl,
        @Schema(description = "플랫폼") Platform platform,
        @Schema(description = "상태") CrawlingStatus status,
        @Schema(description = "에러 메시지") String errorMessage,
        @Schema(description = "생성 시각") LocalDateTime createdAt) {

    /**
     * CrawlingTask에서의 요약 생성
     *
     * @param task 크롤링 작업
     * @return 작업 요약
     */
    public static CrawlingTaskSummary from(CrawlingTask task) {
        return new CrawlingTaskSummary(
                task.getId(),
                task.getUserId(),
                task.getSourceUrl(),
                task.getPlatform(),
                task.getStatus(),
                task.getErrorMessage(),
                task.getCreatedAt());
    }
}
