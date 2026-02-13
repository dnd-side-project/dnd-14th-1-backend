package com.rokyai.dnd14th1backend.crawling.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

import com.rokyai.dnd14th1backend.crawling.enums.CrawlingStatus;

/** 크롤링 응답 DTO */
@Schema(description = "크롤링 응답")
public record CrawlingResponse(
        @Schema(description = "크롤링 상태", example = "COMPLETED") CrawlingStatus status,
        @Schema(description = "작업 ID") UUID taskId,
        @Schema(description = "대화 (완료 시에만 포함)") ConversationResponse conversation) {

    /**
     * 완료 응답 생성
     *
     * @param taskId 작업 ID
     * @param conversation 대화
     * @return 완료 응답
     */
    public static CrawlingResponse completed(UUID taskId, ConversationResponse conversation) {
        return new CrawlingResponse(CrawlingStatus.COMPLETED, taskId, conversation);
    }

    /**
     * 진행 중 응답 생성
     *
     * @param taskId 작업 ID
     * @return 진행 중 응답
     */
    public static CrawlingResponse inProgress(UUID taskId) {
        return new CrawlingResponse(CrawlingStatus.IN_PROGRESS, taskId, null);
    }
}
