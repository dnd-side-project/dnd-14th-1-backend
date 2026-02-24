package com.rokyai.dnd14th1backend.users.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

import com.rokyai.dnd14th1backend.badge.BadgeTier;

/** 대표 배지 정보 응답. */
@Schema(description = "대표 배지 정보")
public record RepresentativeBadgeResponse(
        @Schema(description = "배지 ID") UUID badgeId,
        @Schema(description = "배지 이름") String name,
        @Schema(description = "배지 등급") BadgeTier tier,
        @Schema(description = "활성 이미지 URL") String enableImageUrl,
        @Schema(description = "비활성 이미지 URL") String disableImageUrl) {}
