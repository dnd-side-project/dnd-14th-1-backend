package com.rokyai.dnd14th1backend.promptanalysis.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** 프롬프트 분석 결과 요청 */
@Schema(description = "프롬프트 분석 결과 제출 요청")
public record PromptAnalysisResultRequest(
        @Schema(description = "프롬프트 분석 결과 및 개선 제안 리스트") @Valid @NotNull List<PromptSuggestionRequest> suggestions,
        @Schema(description = "원본 대비 개선 프롬프트의 추정 토큰 절약량") @NotNull @Min(0)
                Integer estimatedTokenSaving,
        @Schema(description = "절약 토큰 기반 빙하 녹음 감소량 (kg 단위)") @NotNull Double glacierMeltReductionKg,
        @Schema(description = "이미 충분히 정제된 프롬프트여서 개선사항이 없을 때 true") @NotNull Boolean noImprovement,
        @Schema(description = "프롬프트가 이해 불가하여 개선이 어려울 때 true") @NotNull Boolean cannotImprove) {}
