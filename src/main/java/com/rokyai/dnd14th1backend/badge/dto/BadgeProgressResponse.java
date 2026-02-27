package com.rokyai.dnd14th1backend.badge.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.rokyai.dnd14th1backend.badge.BadgeTier;
import com.rokyai.dnd14th1backend.badge.BadgeTriggerType;

/** BadgeTriggerType별 배지 진행도 응답. */
@Schema(description = "배지 달성 진행도 정보")
public record BadgeProgressResponse(
        @Schema(description = "달성 조건 타입") BadgeTriggerType triggerType,
        @Schema(description = "현재 누적/최대 값") long currentValue,
        @Schema(description = "현재 획득 배지 등급 (미획득 시 null)") BadgeTier currentBadgeTier,
        @Schema(description = "현재 획득 배지 설명 (미획득 시 null)") String currentBadgeDescription,
        @Schema(description = "현재 획득 배지 활성 이미지 URL (미획득 시 null)")
                String currentBadgeEnableImageUrl,
        @Schema(description = "다음 배지 달성 조건 (모두 달성 시 null)") Integer nextBadgeTriggerCondition,
        @Schema(description = "다음 배지 등급 (모두 달성 시 null)") BadgeTier nextBadgeTier,
        @Schema(description = "다음 배지 설명 (모두 달성 시 null)") String nextBadgeDescription,
        @Schema(description = "다음 배지 활성 이미지 URL (모두 달성 시 null)")
                String nextBadgeEnableImageUrl) {}
