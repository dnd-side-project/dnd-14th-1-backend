package com.rokyai.dnd14th1backend.badge.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** 대표 배지 설정 요청. */
@Schema(description = "대표 배지 설정 요청")
public record RepresentBadgeRequest(
        @Schema(description = "대표 배지로 설정할 배지 ID") @NotNull UUID badgeId) {}
