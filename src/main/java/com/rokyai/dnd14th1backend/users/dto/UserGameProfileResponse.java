package com.rokyai.dnd14th1backend.users.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.rokyai.dnd14th1backend.badge.dto.BadgeProgressResponse;

/** 사용자 게임 프로필 응답 */
@Schema(description = "사용자 게임 프로필 정보")
public record UserGameProfileResponse(
        @Schema(description = "총 누적 XP") long totalXp,
        @Schema(description = "현재 티어") int tier,
        @Schema(description = "현재 티어 시작 XP") long currentTierXp,
        @Schema(description = "다음 티어 필요 XP") long nextTierXp,
        @Schema(description = "배지 달성 진행도 목록") List<BadgeProgressResponse> badgeProgress) {}
