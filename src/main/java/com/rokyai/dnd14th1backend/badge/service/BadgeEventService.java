package com.rokyai.dnd14th1backend.badge.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.rokyai.dnd14th1backend.badge.BadgeTriggerType;
import com.rokyai.dnd14th1backend.badge.dto.EarnedBadgeInfo;
import com.rokyai.dnd14th1backend.badge.entity.Badge;
import com.rokyai.dnd14th1backend.badge.entity.UserBadge;
import com.rokyai.dnd14th1backend.badge.mapper.BadgeMapper;
import com.rokyai.dnd14th1backend.badge.repository.BadgeRepository;
import com.rokyai.dnd14th1backend.badge.repository.UserBadgeRepository;
import com.rokyai.dnd14th1backend.users.domain.User;
import com.rokyai.dnd14th1backend.users.infrastructure.UserRepository;

/** 배지 이벤트 자동 체크 및 부여 서비스. */
@Service
@RequiredArgsConstructor
@Transactional
public class BadgeEventService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;

    /**
     * optimizeChat 이벤트 시 OPTIMIZE_COUNT / SINGLE_TOKEN_SAVING / CUMULATIVE_XP 배지 체크.
     *
     * @param userId 사용자 ID
     * @param tokenSaving 이번 최적화의 토큰 절약량
     * @param totalXp profile.addXp() 이후 갱신된 누적 XP
     * @param optimizeCount chat.applyOptimization() 이후 조회한 누적 최적화 횟수
     * @return 새로 획득한 배지 목록 (없으면 빈 List)
     */
    public List<EarnedBadgeInfo> checkBadgesOnOptimize(
            UUID userId, int tokenSaving, long totalXp, long optimizeCount) {
        Set<UUID> earnedBadgeIds = userBadgeRepository.findBadgeIdsByUserId(userId);
        List<Badge> allBadges = badgeRepository.findAll();
        User user = userRepository.getReferenceById(userId);
        List<EarnedBadgeInfo> newlyEarned = new ArrayList<>();

        for (Badge badge : allBadges) {
            if (earnedBadgeIds.contains(badge.getId())) {
                continue;
            }
            if (badge.getTriggerType() == BadgeTriggerType.CRAWLING_COUNT) {
                continue;
            }

            boolean conditionMet =
                    switch (badge.getTriggerType()) {
                        case OPTIMIZE_COUNT -> optimizeCount >= badge.getTriggerCondition();
                        case SINGLE_TOKEN_SAVING -> tokenSaving >= badge.getTriggerCondition();
                        case CUMULATIVE_XP -> totalXp >= badge.getTriggerCondition();
                        default -> false;
                    };

            if (conditionMet) {
                newlyEarned.add(grantBadge(user, badge));
            }
        }

        return newlyEarned;
    }

    /**
     * requestCrawling 이벤트 시 CRAWLING_COUNT 배지 체크.
     *
     * @param userId 사용자 ID
     * @param crawlingCount crawlingTaskRepository.save() 이후 조회한 누적 크롤링 횟수
     * @return 새로 획득한 배지 목록 (없으면 빈 List)
     */
    public List<EarnedBadgeInfo> checkBadgesOnCrawl(UUID userId, long crawlingCount) {
        Set<UUID> earnedBadgeIds = userBadgeRepository.findBadgeIdsByUserId(userId);
        User user = userRepository.getReferenceById(userId);
        List<EarnedBadgeInfo> newlyEarned = new ArrayList<>();

        badgeRepository.findAll().stream()
                .filter(badge -> badge.getTriggerType() == BadgeTriggerType.CRAWLING_COUNT)
                .filter(badge -> !earnedBadgeIds.contains(badge.getId()))
                .filter(badge -> crawlingCount >= badge.getTriggerCondition())
                .map(badge -> grantBadge(user, badge))
                .forEach(newlyEarned::add);

        return newlyEarned;
    }

    private EarnedBadgeInfo grantBadge(User user, Badge badge) {
        UserBadge userBadge =
                UserBadge.builder().user(user).badge(badge).earnedAt(LocalDateTime.now()).build();
        userBadgeRepository.save(userBadge);
        return BadgeMapper.toEarnedBadgeInfo(userBadge);
    }
}
