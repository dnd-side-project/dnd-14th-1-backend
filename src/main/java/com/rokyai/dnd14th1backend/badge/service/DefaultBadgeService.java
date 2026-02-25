package com.rokyai.dnd14th1backend.badge.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.rokyai.dnd14th1backend.badge.entity.Badge;
import com.rokyai.dnd14th1backend.badge.entity.UserBadge;
import com.rokyai.dnd14th1backend.badge.repository.BadgeRepository;
import com.rokyai.dnd14th1backend.badge.repository.UserBadgeRepository;
import com.rokyai.dnd14th1backend.users.domain.User;

/** 신규/기존 사용자 로그인 시 기본 배지를 보장합니다. */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultBadgeService {

    private static final UUID DEFAULT_BADGE_ID =
            UUID.fromString("01968e00-0000-7000-8000-000000000001");

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    @Transactional
    public void ensureDefaultBadge(User user) {
        UUID userId = user.getId();
        if (userId == null) {
            log.warn("기본 뱃지 지급 실패: userId가 없습니다.");
            return;
        }

        if (userBadgeRepository.existsByUserIdAndBadgeId(userId, DEFAULT_BADGE_ID)) {
            return;
        }

        Badge defaultBadge = badgeRepository.findById(DEFAULT_BADGE_ID).orElse(null);
        if (defaultBadge == null) {
            log.warn("기본 뱃지 지급 실패: badgeId={} 를 찾을 수 없습니다.", DEFAULT_BADGE_ID);
            return;
        }

        UserBadge userBadge =
                UserBadge.builder()
                        .user(user)
                        .badge(defaultBadge)
                        .earnedAt(LocalDateTime.now())
                        .build();

        try {
            userBadgeRepository.save(userBadge);

            if (user.getRepresentativeUserBadge() == null) {
                user.updateRepresentativeUserBadge(userBadge);
            }
        } catch (DataIntegrityViolationException exception) {
            // 동시 요청으로 중복 지급 시 unique 제약 조건에서 방지
            log.debug(
                    "기본 뱃지가 이미 지급되었습니다. userId={}, badgeId={}",
                    userId,
                    DEFAULT_BADGE_ID);
        }
    }
}
