package com.rokyai.dnd14th1backend.badge.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

import com.rokyai.dnd14th1backend.badge.BadgeTier;
import com.rokyai.dnd14th1backend.badge.BadgeTriggerType;

/** 유저 보유 배지 응답. */
@Schema(description = "유저 보유 배지 정보")
public record UserBadgeResponse(
        @Schema(description = "배지 ID") UUID badgeId,
        @Schema(description = "배지 이름") String name,
        @Schema(description = "배지 설명") String description,
        @Schema(description = "배지 등급") BadgeTier tier,
        @Schema(description = "달성 조건 타입") BadgeTriggerType triggerType,
        @Schema(description = "달성 조건 값") Integer triggerCondition,
        @Schema(description = "활성 이미지 URL") String enableImageUrl,
        @Schema(description = "비활성 이미지 URL") String disableImageUrl,
        @Schema(description = "획득 일시") LocalDateTime earnedAt,
        @Schema(description = "대표 배지 여부") boolean isRepresentative) {}
