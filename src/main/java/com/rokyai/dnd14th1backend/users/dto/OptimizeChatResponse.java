package com.rokyai.dnd14th1backend.users.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.rokyai.dnd14th1backend.badge.dto.EarnedBadgeInfo;

/** Chat 최적화 응답 */
public record OptimizeChatResponse(
        int xpEarned,
        long totalXp,
        int tier,
        double progress,
        @Schema(description = "이번 이벤트로 새로 획득한 배지 목록 (없으면 빈 배열)")
                List<EarnedBadgeInfo> earnedBadges) {}
