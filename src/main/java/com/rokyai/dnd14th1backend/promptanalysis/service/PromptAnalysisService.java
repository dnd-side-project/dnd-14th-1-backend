package com.rokyai.dnd14th1backend.promptanalysis.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import com.rokyai.dnd14th1backend.badge.dto.EarnedBadgeInfo;
import com.rokyai.dnd14th1backend.badge.service.BadgeEventService;
import com.rokyai.dnd14th1backend.common.pagination.CursorPageRequest;
import com.rokyai.dnd14th1backend.common.pagination.CursorPageResponse;
import com.rokyai.dnd14th1backend.crawling.infrastructure.ChatRepository;
import com.rokyai.dnd14th1backend.promptanalysis.domain.PromptAnalysisResult;
import com.rokyai.dnd14th1backend.promptanalysis.dto.PromptAnalysisResultRequest;
import com.rokyai.dnd14th1backend.promptanalysis.dto.PromptAnalysisResultResponse;
import com.rokyai.dnd14th1backend.promptanalysis.dto.PromptAnalysisResultSummary;
import com.rokyai.dnd14th1backend.promptanalysis.infrastructure.PromptAnalysisResultRepository;
import com.rokyai.dnd14th1backend.promptanalysis.mapper.PromptAnalysisMapper;
import com.rokyai.dnd14th1backend.users.domain.UserGameProfile;
import com.rokyai.dnd14th1backend.users.infrastructure.UserGameProfileRepository;
import com.rokyai.dnd14th1backend.users.service.UserGameService;

/** 프롬프트 분석 결과 서비스 */
@Slf4j
@Service
public class PromptAnalysisService {

    private static final double XP_PER_TOKEN = 1.2;

    private final PromptAnalysisResultRepository promptAnalysisResultRepository;
    private final UserGameProfileRepository userGameProfileRepository;
    private final ChatRepository chatRepository;
    private final BadgeEventService badgeEventService;
    private final UserGameService userGameService;

    public PromptAnalysisService(
            PromptAnalysisResultRepository promptAnalysisResultRepository,
            UserGameProfileRepository userGameProfileRepository,
            ChatRepository chatRepository,
            BadgeEventService badgeEventService,
            UserGameService userGameService) {
        this.promptAnalysisResultRepository = promptAnalysisResultRepository;
        this.userGameProfileRepository = userGameProfileRepository;
        this.chatRepository = chatRepository;
        this.badgeEventService = badgeEventService;
        this.userGameService = userGameService;
    }

    /**
     * 프롬프트 분석 결과 제출 + XP 적립 + 배지 체크
     *
     * @param userId 사용자 ID
     * @param request 분석 결과 요청
     * @return 분석 결과 응답 (XP, 티어, 배지 정보)
     */
    @Transactional
    public PromptAnalysisResultResponse submitResult(
            UUID userId, PromptAnalysisResultRequest request) {
        boolean validResult = !request.noImprovement() && !request.cannotImprove();
        int xpEarned = validResult ? (int) (request.estimatedTokenSaving() * XP_PER_TOKEN) : 0;

        PromptAnalysisResult result = PromptAnalysisMapper.toEntity(request, userId, xpEarned);
        promptAnalysisResultRepository.save(result);

        UserGameProfile profile =
                userGameProfileRepository
                        .findByUserId(userId)
                        .orElseGet(
                                () ->
                                        userGameProfileRepository.save(
                                                UserGameProfile.create(userId)));

        List<EarnedBadgeInfo> earnedBadges = List.of();

        if (validResult) {
            profile.addXp(xpEarned);

            long chatOptimizeCount = chatRepository.countOptimizedByUserId(userId);
            long analysisCount = promptAnalysisResultRepository.countValidByUserId(userId);
            long totalOptimizeCount = chatOptimizeCount + analysisCount;

            try {
                earnedBadges =
                        badgeEventService.checkBadgesOnOptimize(
                                userId,
                                request.estimatedTokenSaving(),
                                profile.getTotalXp(),
                                totalOptimizeCount);
            } catch (Exception e) {
                log.warn("배지 체크 중 오류 발생, 무시: userId={}, error={}", userId, e.getMessage());
            }
        }

        int tier = userGameService.calculateTier(profile.getTotalXp());
        double progress = calculateProgress(profile.getTotalXp(), tier);

        return PromptAnalysisMapper.toResponse(
                result, profile.getTotalXp(), tier, progress, earnedBadges);
    }

    /**
     * 사용자의 프롬프트 분석 결과 목록을 cursor 기반으로 조회
     *
     * @param userId 사용자 ID
     * @param pageRequest cursor 페이지네이션 요청
     * @return cursor 기반 페이지네이션 응답
     */
    @Transactional(readOnly = true)
    public CursorPageResponse<PromptAnalysisResultSummary> getResults(
            UUID userId, CursorPageRequest pageRequest) {
        int fetchSize = pageRequest.size() + 1;

        List<PromptAnalysisResult> fetched =
                pageRequest.cursor() == null
                        ? promptAnalysisResultRepository.findByUserIdOrderByIdDesc(
                                userId, fetchSize)
                        : promptAnalysisResultRepository.findByUserIdAndIdLessThanOrderByIdDesc(
                                userId, pageRequest.cursor(), fetchSize);

        List<PromptAnalysisResultSummary> summaries =
                fetched.stream().map(PromptAnalysisMapper::toSummary).toList();

        return CursorPageResponse.of(
                summaries, pageRequest.size(), PromptAnalysisResultSummary::id);
    }

    private double calculateProgress(long totalXp, int tier) {
        int maxTier = 50;
        if (tier >= maxTier) {
            return 1.0;
        }
        long currentTierXp = requiredXp(tier);
        long nextTierXp = requiredXp(tier + 1);
        long xpRange = nextTierXp - currentTierXp;
        if (xpRange <= 0) {
            return 0.0;
        }
        return (double) (totalXp - currentTierXp) / xpRange;
    }

    private long requiredXp(int tier) {
        return (long) (2000 * Math.pow(tier - 1, 2.1));
    }
}
