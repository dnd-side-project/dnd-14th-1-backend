package com.rokyai.dnd14th1backend.badge.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

import com.rokyai.dnd14th1backend.badge.BadgeTier;

/** 이벤트 응답에 포함되는 새로 획득한 배지 정보. */
@Schema(description = "획득한 배지 정보")
public record EarnedBadgeInfo(
        @Schema(description = "배지 ID") UUID badgeId,
        @Schema(description = "배지 이름") String name,
        @Schema(description = "배지 등급") BadgeTier tier,
        @Schema(description = "활성 이미지 URL") String enableImageUrl,
        @Schema(description = "비활성 이미지 URL") String disableImageUrl,
        @Schema(description = "획득 일시") LocalDateTime earnedAt) {}
