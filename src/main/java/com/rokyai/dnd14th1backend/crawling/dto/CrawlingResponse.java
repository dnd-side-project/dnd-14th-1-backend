package com.rokyai.dnd14th1backend.crawling.dto;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

import com.rokyai.dnd14th1backend.badge.dto.EarnedBadgeInfo;
import com.rokyai.dnd14th1backend.crawling.enums.CrawlingStatus;

/** 크롤링 응답 DTO */
@Schema(description = "크롤링 응답")
public record CrawlingResponse(
        @Schema(description = "크롤링 상태", example = "COMPLETED") CrawlingStatus status,
        @Schema(description = "작업 ID") UUID taskId,
        @Schema(description = "대화 (완료 시에만 포함)") ConversationResponse conversation,
        @Schema(description = "이번 이벤트로 새로 획득한 배지 목록 (없으면 빈 배열)")
                List<EarnedBadgeInfo> earnedBadges) {

    /**
     * 완료 응답 생성
     *
     * @param taskId 작업 ID
     * @param conversation 대화
     * @param earnedBadges 새로 획득한 배지 목록
     * @return 완료 응답
     */
    public static CrawlingResponse completed(
            UUID taskId, ConversationResponse conversation, List<EarnedBadgeInfo> earnedBadges) {
        return new CrawlingResponse(CrawlingStatus.COMPLETED, taskId, conversation, earnedBadges);
    }

    /**
     * 진행 중 응답 생성
     *
     * @param taskId 작업 ID
     * @param earnedBadges 새로 획득한 배지 목록
     * @return 진행 중 응답
     */
    public static CrawlingResponse inProgress(UUID taskId, List<EarnedBadgeInfo> earnedBadges) {
        return new CrawlingResponse(CrawlingStatus.IN_PROGRESS, taskId, null, earnedBadges);
    }
}
