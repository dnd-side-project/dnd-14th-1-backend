package com.rokyai.dnd14th1backend.badge.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

import com.rokyai.dnd14th1backend.badge.BadgeTier;
import com.rokyai.dnd14th1backend.badge.BadgeTriggerType;

/** 배지 조회 응답. */
@Schema(description = "배지 정보")
public record BadgeResponse(
        @Schema(description = "배지 ID") UUID badgeId,
        @Schema(description = "배지 이름") String name,
        @Schema(description = "배지 설명") String description,
        @Schema(description = "배지 등급") BadgeTier tier,
        @Schema(description = "달성 조건 타입") BadgeTriggerType triggerType,
        @Schema(description = "달성 조건 값") Integer triggerCondition,
        @Schema(description = "활성 이미지 URL") String enableImageUrl,
        @Schema(description = "비활성 이미지 URL") String disableImageUrl) {}
