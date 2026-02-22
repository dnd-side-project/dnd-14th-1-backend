package com.rokyai.dnd14th1backend.promptanalysis.mapper;

import java.util.List;
import java.util.UUID;

import com.rokyai.dnd14th1backend.badge.dto.EarnedBadgeInfo;
import com.rokyai.dnd14th1backend.promptanalysis.domain.PromptAnalysisResult;
import com.rokyai.dnd14th1backend.promptanalysis.domain.PromptAnalysisSuggestion;
import com.rokyai.dnd14th1backend.promptanalysis.dto.PromptAnalysisResultRequest;
import com.rokyai.dnd14th1backend.promptanalysis.dto.PromptAnalysisResultResponse;
import com.rokyai.dnd14th1backend.promptanalysis.dto.PromptAnalysisResultSummary;
import com.rokyai.dnd14th1backend.promptanalysis.dto.PromptSuggestionRequest;

/** PromptAnalysis Entity-DTO 변환 매퍼 */
public final class PromptAnalysisMapper {

    private PromptAnalysisMapper() {}

    /**
     * Request → PromptAnalysisResult 엔티티 변환
     *
     * @param request 분석 결과 요청
     * @param userId 사용자 ID
     * @param xpEarned 계산된 XP
     * @return PromptAnalysisResult 엔티티
     */
    public static PromptAnalysisResult toEntity(
            PromptAnalysisResultRequest request, UUID userId, int xpEarned) {
        PromptAnalysisResult result =
                PromptAnalysisResult.builder()
                        .userId(userId)
                        .estimatedTokenSaving(request.estimatedTokenSaving())
                        .glacierMeltReductionKg(request.glacierMeltReductionKg())
                        .noImprovement(request.noImprovement())
                        .cannotImprove(request.cannotImprove())
                        .xpEarned(xpEarned)
                        .build();

        List<PromptAnalysisSuggestion> suggestions =
                request.suggestions().stream()
                        .map(PromptAnalysisMapper::toSuggestionEntity)
                        .toList();
        result.addSuggestions(suggestions);

        return result;
    }

    /**
     * 분석 결과 → Response 변환
     *
     * @param result 저장된 분석 결과
     * @param totalXp 갱신된 누적 XP
     * @param tier 현재 티어
     * @param progress 티어 내 진행률
     * @param earnedBadges 획득 배지
     * @return 응답 DTO
     */
    public static PromptAnalysisResultResponse toResponse(
            PromptAnalysisResult result,
            long totalXp,
            int tier,
            double progress,
            List<EarnedBadgeInfo> earnedBadges) {
        return new PromptAnalysisResultResponse(
                result.getId(), result.getXpEarned(), totalXp, tier, progress, earnedBadges);
    }

    /** PromptAnalysisResult → PromptAnalysisResultSummary (목록 조회용) */
    public static PromptAnalysisResultSummary toSummary(PromptAnalysisResult result) {
        return new PromptAnalysisResultSummary(
                result.getId(),
                result.getEstimatedTokenSaving(),
                result.getGlacierMeltReductionKg(),
                result.getXpEarned(),
                result.getNoImprovement(),
                result.getCannotImprove(),
                result.getSuggestions().size(),
                result.getCreatedAt());
    }

    private static PromptAnalysisSuggestion toSuggestionEntity(PromptSuggestionRequest request) {
        return PromptAnalysisSuggestion.builder()
                .originalPart(request.originalPart())
                .improvedPart(request.improvedPart())
                .reason(request.reason())
                .build();
    }
}
