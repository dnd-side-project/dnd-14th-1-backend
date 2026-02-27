package com.rokyai.dnd14th1backend.users.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import com.rokyai.dnd14th1backend.badge.BadgeTriggerType;
import com.rokyai.dnd14th1backend.badge.dto.BadgeProgressResponse;
import com.rokyai.dnd14th1backend.badge.dto.EarnedBadgeInfo;
import com.rokyai.dnd14th1backend.badge.entity.Badge;
import com.rokyai.dnd14th1backend.badge.repository.BadgeRepository;
import com.rokyai.dnd14th1backend.badge.repository.UserBadgeRepository;
import com.rokyai.dnd14th1backend.badge.service.BadgeEventService;
import com.rokyai.dnd14th1backend.badge.util.BadgeImageUrlResolver;
import com.rokyai.dnd14th1backend.crawling.domain.Chat;
import com.rokyai.dnd14th1backend.crawling.exception.CrawlingErrorStatus;
import com.rokyai.dnd14th1backend.crawling.exception.CrawlingException;
import com.rokyai.dnd14th1backend.crawling.infrastructure.ChatRepository;
import com.rokyai.dnd14th1backend.crawling.infrastructure.ConversationRepository;
import com.rokyai.dnd14th1backend.crawling.infrastructure.CrawlingTaskRepository;
import com.rokyai.dnd14th1backend.users.domain.UserGameProfile;
import com.rokyai.dnd14th1backend.users.dto.OptimizeChatResponse;
import com.rokyai.dnd14th1backend.users.dto.UserGameProfileResponse;
import com.rokyai.dnd14th1backend.users.exception.UserGameErrorStatus;
import com.rokyai.dnd14th1backend.users.exception.UserGameException;
import com.rokyai.dnd14th1backend.users.infrastructure.UserGameProfileRepository;

/** 사용자 게임 서비스 (XP 적립, 티어 계산) */
@Slf4j
@Service
public class UserGameProfileService {

    private static final double XP_PER_TOKEN = 1.2;
    private static final int MAX_TIER = 50;

    private final ConversationRepository conversationRepository;
    private final ChatRepository chatRepository;
    private final UserGameProfileRepository userGameProfileRepository;
    private final BadgeEventService badgeEventService;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final CrawlingTaskRepository crawlingTaskRepository;

    public UserGameProfileService(
            ConversationRepository conversationRepository,
            ChatRepository chatRepository,
            UserGameProfileRepository userGameProfileRepository,
            BadgeEventService badgeEventService,
            BadgeRepository badgeRepository,
            UserBadgeRepository userBadgeRepository,
            CrawlingTaskRepository crawlingTaskRepository) {
        this.conversationRepository = conversationRepository;
        this.chatRepository = chatRepository;
        this.userGameProfileRepository = userGameProfileRepository;
        this.badgeEventService = badgeEventService;
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.crawlingTaskRepository = crawlingTaskRepository;
    }

    @Transactional
    public OptimizeChatResponse optimizeChat(
            UUID userId, UUID conversationId, UUID chatId, int tokenSaving) {
        conversationRepository
                .findByIdAndUserId(conversationId, userId)
                .orElseThrow(
                        () -> new CrawlingException(CrawlingErrorStatus.CONVERSATION_NOT_FOUND));

        Chat chat =
                chatRepository
                        .findByIdAndConversationId(chatId, conversationId)
                        .orElseThrow(
                                () -> new CrawlingException(CrawlingErrorStatus.CHAT_NOT_FOUND));

        if (chat.isOptimized()) {
            throw new UserGameException(UserGameErrorStatus.ALREADY_OPTIMIZED);
        }

        int xpEarned = (int) (tokenSaving * XP_PER_TOKEN);
        chat.applyOptimization(tokenSaving, xpEarned);

        UserGameProfile profile =
                userGameProfileRepository
                        .findByUserId(userId)
                        .orElseGet(
                                () ->
                                        userGameProfileRepository.save(
                                                UserGameProfile.create(userId)));

        profile.addXp(xpEarned);

        long optimizeCount = chatRepository.countOptimizedByUserId(userId);
        List<EarnedBadgeInfo> earnedBadges;
        try {
            earnedBadges =
                    badgeEventService.checkBadgesOnOptimize(
                            userId, tokenSaving, profile.getTotalXp(), optimizeCount);
        } catch (Exception e) {
            log.warn("배지 체크 중 오류 발생, 무시: userId={}, error={}", userId, e.getMessage());
            earnedBadges = List.of();
        }

        int tier = calculateTier(profile.getTotalXp());
        double progress = calculateProgress(profile.getTotalXp(), tier);

        return new OptimizeChatResponse(
                xpEarned, profile.getTotalXp(), tier, progress, earnedBadges);
    }

    @Transactional
    public UserGameProfileResponse getProfile(UUID userId) {
        UserGameProfile profile =
                userGameProfileRepository
                        .findByUserId(userId)
                        .orElseGet(
                                () ->
                                        userGameProfileRepository.save(
                                                UserGameProfile.create(userId)));

        int tier = calculateTier(profile.getTotalXp());
        long currentTierXp = requiredXp(tier);
        long nextTierXp = tier < MAX_TIER ? requiredXp(tier + 1) : requiredXp(MAX_TIER);

        List<BadgeProgressResponse> badgeProgress = buildBadgeProgress(userId, profile);

        return new UserGameProfileResponse(
                profile.getTotalXp(), tier, currentTierXp, nextTierXp, badgeProgress);
    }

    private List<BadgeProgressResponse> buildBadgeProgress(UUID userId, UserGameProfile profile) {
        Set<UUID> earnedBadgeIds = userBadgeRepository.findBadgeIdsByUserId(userId);

        Map<BadgeTriggerType, Long> currentValues =
                Map.of(
                        BadgeTriggerType.OPTIMIZE_COUNT,
                                chatRepository.countOptimizedByUserId(userId),
                        BadgeTriggerType.SINGLE_TOKEN_SAVING,
                                (long) chatRepository.findMaxTokenSavingByUserId(userId),
                        BadgeTriggerType.CRAWLING_COUNT,
                                crawlingTaskRepository.countByUserId(userId),
                        BadgeTriggerType.CUMULATIVE_XP, profile.getTotalXp());

        Map<BadgeTriggerType, List<Badge>> badgesByTriggerType =
                badgeRepository.findAll().stream()
                        .collect(Collectors.groupingBy(Badge::getTriggerType));

        List<BadgeProgressResponse> progressList = new ArrayList<>();
        for (BadgeTriggerType triggerType : BadgeTriggerType.values()) {
            long currentValue = currentValues.getOrDefault(triggerType, 0L);

            List<Badge> badges =
                    badgesByTriggerType.getOrDefault(triggerType, List.of()).stream()
                            .sorted(Comparator.comparingInt(Badge::getTriggerCondition))
                            .toList();

            Badge currentBadge =
                    badges.reversed().stream()
                            .filter(b -> earnedBadgeIds.contains(b.getId()))
                            .findFirst()
                            .orElse(null);

            Badge nextBadge =
                    badges.stream()
                            .filter(b -> !earnedBadgeIds.contains(b.getId()))
                            .findFirst()
                            .orElse(null);

            progressList.add(
                    new BadgeProgressResponse(
                            triggerType,
                            currentValue,
                            currentBadge != null ? currentBadge.getTier() : null,
                            currentBadge != null ? currentBadge.getDescription() : null,
                            currentBadge != null
                                    ? BadgeImageUrlResolver.toPublicUrl(
                                            currentBadge.getEnableImageUrl())
                                    : null,
                            nextBadge != null ? nextBadge.getTriggerCondition() : null,
                            nextBadge != null ? nextBadge.getTier() : null,
                            nextBadge != null ? nextBadge.getDescription() : null,
                            nextBadge != null
                                    ? BadgeImageUrlResolver.toPublicUrl(
                                            nextBadge.getEnableImageUrl())
                                    : null));
        }

        return progressList;
    }

    public int calculateTier(long totalXp) {
        for (int tier = MAX_TIER; tier >= 2; tier--) {
            if (totalXp >= requiredXp(tier)) {
                return tier;
            }
        }
        return 1;
    }

    public long requiredXp(int tier) {
        return (long) (2000 * Math.pow(tier - 1, 2.1));
    }

    public double calculateProgress(long totalXp, int tier) {
        if (tier >= MAX_TIER) {
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
}
