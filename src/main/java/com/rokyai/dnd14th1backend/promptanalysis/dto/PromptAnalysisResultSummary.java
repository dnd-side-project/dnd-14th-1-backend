package com.rokyai.dnd14th1backend.promptanalysis.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/** 프롬프트 분석 결과 목록 조회용 요약 DTO */
@Schema(description = "프롬프트 분석 결과 요약")
public record PromptAnalysisResultSummary(
        @Schema(description = "분석 결과 ID") UUID id,
        @Schema(description = "추정 토큰 절약량") int estimatedTokenSaving,
        @Schema(description = "빙하 녹음 감소량 (kg)") double glacierMeltReductionKg,
        @Schema(description = "획득한 XP") int xpEarned,
        @Schema(description = "개선 불가 여부") boolean noImprovement,
        @Schema(description = "이해 불가 여부") boolean cannotImprove,
        @Schema(description = "제안 수") int suggestionCount,
        @Schema(description = "생성 일시") LocalDateTime createdAt) {}
