package com.rokyai.dnd14th1backend.badge.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.rokyai.dnd14th1backend.badge.BadgeStatus;
import com.rokyai.dnd14th1backend.badge.dto.BadgeResponse;
import com.rokyai.dnd14th1backend.badge.dto.UserBadgeResponse;
import com.rokyai.dnd14th1backend.badge.entity.UserBadge;
import com.rokyai.dnd14th1backend.badge.exception.BadgeException;
import com.rokyai.dnd14th1backend.badge.mapper.BadgeMapper;
import com.rokyai.dnd14th1backend.badge.repository.BadgeRepository;
import com.rokyai.dnd14th1backend.badge.repository.UserBadgeRepository;
import com.rokyai.dnd14th1backend.users.domain.User;
import com.rokyai.dnd14th1backend.users.infrastructure.UserRepository;

/** 배지 비즈니스 로직. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;

    /** 전체 배지 목록을 조회합니다. */
    public List<BadgeResponse> getAllBadges() {
        return badgeRepository.findAll().stream().map(BadgeMapper::toResponse).toList();
    }

    /** 유저가 보유한 배지 목록을 조회합니다. */
    public List<UserBadgeResponse> getUserBadges(UUID userId) {
        User user = userRepository.getReferenceById(userId);
        UUID representativeUserBadgeId =
                user.getRepresentativeUserBadge() != null
                        ? user.getRepresentativeUserBadge().getId()
                        : null;

        return userBadgeRepository.findByUserId(userId).stream()
                .map(
                        userBadge ->
                                BadgeMapper.toUserBadgeResponse(
                                        userBadge, representativeUserBadgeId))
                .toList();
    }

    /**
     * 대표 배지를 설정합니다.
     *
     * @param userId 사용자 ID
     * @param badgeId 대표 배지로 설정할 배지 ID
     * @throws BadgeException 미획득 배지인 경우
     */
    @Transactional
    public void setRepresentativeBadge(UUID userId, UUID badgeId) {
        UserBadge userBadge =
                userBadgeRepository
                        .findByUserIdAndBadgeId(userId, badgeId)
                        .orElseThrow(() -> new BadgeException(BadgeStatus.BADGE_NOT_EARNED));

        User user = userRepository.getReferenceById(userId);
        user.updateRepresentativeUserBadge(userBadge);
    }
}
