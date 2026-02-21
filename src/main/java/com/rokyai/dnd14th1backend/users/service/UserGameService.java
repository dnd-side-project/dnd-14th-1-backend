package com.rokyai.dnd14th1backend.users.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rokyai.dnd14th1backend.badge.dto.EarnedBadgeInfo;
import com.rokyai.dnd14th1backend.badge.service.BadgeEventService;
import com.rokyai.dnd14th1backend.crawling.domain.Chat;
import com.rokyai.dnd14th1backend.crawling.exception.CrawlingErrorStatus;
import com.rokyai.dnd14th1backend.crawling.exception.CrawlingException;
import com.rokyai.dnd14th1backend.crawling.infrastructure.ChatRepository;
import com.rokyai.dnd14th1backend.crawling.infrastructure.ConversationRepository;
import com.rokyai.dnd14th1backend.users.domain.UserGameProfile;
import com.rokyai.dnd14th1backend.users.dto.OptimizeChatResponse;
import com.rokyai.dnd14th1backend.users.dto.UserGameProfileResponse;
import com.rokyai.dnd14th1backend.users.exception.UserGameErrorStatus;
import com.rokyai.dnd14th1backend.users.exception.UserGameException;
import com.rokyai.dnd14th1backend.users.infrastructure.UserGameProfileRepository;

/** 사용자 게임 서비스 (XP 적립, 티어 계산) */
@Service
public class UserGameService {

    private static final double XP_PER_TOKEN = 1.2;
    private static final int MAX_TIER = 50;

    private final ConversationRepository conversationRepository;
    private final ChatRepository chatRepository;
    private final UserGameProfileRepository userGameProfileRepository;
    private final BadgeEventService badgeEventService;

    public UserGameService(
            ConversationRepository conversationRepository,
            ChatRepository chatRepository,
            UserGameProfileRepository userGameProfileRepository,
            BadgeEventService badgeEventService) {
        this.conversationRepository = conversationRepository;
        this.chatRepository = chatRepository;
        this.userGameProfileRepository = userGameProfileRepository;
        this.badgeEventService = badgeEventService;
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
        List<EarnedBadgeInfo> earnedBadges =
                badgeEventService.checkBadgesOnOptimize(
                        userId, tokenSaving, profile.getTotalXp(), optimizeCount);

        int tier = calculateTier(profile.getTotalXp());
        double progress = calculateProgress(profile.getTotalXp(), tier);

        return new OptimizeChatResponse(
                xpEarned, profile.getTotalXp(), tier, progress, earnedBadges);
    }

    @Transactional(readOnly = true)
    public UserGameProfileResponse getProfile(UUID userId) {
        UserGameProfile profile =
                userGameProfileRepository
                        .findByUserId(userId)
                        .orElseThrow(
                                () -> new UserGameException(UserGameErrorStatus.PROFILE_NOT_FOUND));

        int tier = calculateTier(profile.getTotalXp());
        long currentTierXp = requiredXp(tier);
        long nextTierXp = tier < MAX_TIER ? requiredXp(tier + 1) : requiredXp(MAX_TIER);

        return new UserGameProfileResponse(profile.getTotalXp(), tier, currentTierXp, nextTierXp);
    }

    public int calculateTier(long totalXp) {
        for (int tier = MAX_TIER; tier >= 2; tier--) {
            if (totalXp >= requiredXp(tier)) {
                return tier;
            }
        }
        return 1;
    }

    private long requiredXp(int tier) {
        return (long) (2000 * Math.pow(tier - 1, 2.1));
    }

    private double calculateProgress(long totalXp, int tier) {
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
