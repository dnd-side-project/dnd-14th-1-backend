package com.rokyai.dnd14th1backend.promptanalysis.dto;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

import com.rokyai.dnd14th1backend.badge.dto.EarnedBadgeInfo;

/** 프롬프트 분석 결과 응답 */
@Schema(description = "프롬프트 분석 결과 제출 응답")
public record PromptAnalysisResultResponse(
        @Schema(description = "분석 결과 ID") UUID resultId,
        @Schema(description = "이번 분석으로 획득한 XP") int xpEarned,
        @Schema(description = "누적 XP") long totalXp,
        @Schema(description = "현재 티어") int tier,
        @Schema(description = "현재 티어 내 진행률 (0.0~1.0)") double progress,
        @Schema(description = "이번 이벤트로 새로 획득한 배지 목록 (없으면 빈 배열)")
                List<EarnedBadgeInfo> earnedBadges) {}
