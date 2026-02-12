package com.rokyai.dnd14th1backend.users.dto;

/** 사용자 게임 프로필 응답 */
public record UserGameProfileResponse(
        long totalXp, int tier, long currentTierXp, long nextTierXp) {}
