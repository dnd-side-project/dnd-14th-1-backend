package com.rokyai.dnd14th1backend.users.dto;

/** Chat 최적화 응답 */
public record OptimizeChatResponse(int xpEarned, long totalXp, int tier, double progress) {}
