package com.rokyai.dnd14th1backend.badge.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.rokyai.dnd14th1backend.badge.dto.BadgeResponse;
import com.rokyai.dnd14th1backend.badge.dto.UserBadgeResponse;
import com.rokyai.dnd14th1backend.badge.mapper.BadgeMapper;
import com.rokyai.dnd14th1backend.badge.repository.BadgeRepository;
import com.rokyai.dnd14th1backend.badge.repository.UserBadgeRepository;

/** 배지 비즈니스 로직. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    /** 전체 배지 목록을 조회합니다. */
    public List<BadgeResponse> getAllBadges() {
        return badgeRepository.findAll().stream().map(BadgeMapper::toResponse).toList();
    }

    /** 유저가 보유한 배지 목록을 조회합니다. */
    public List<UserBadgeResponse> getUserBadges(UUID userId) {
        return userBadgeRepository.findByUserId(userId).stream()
                .map(BadgeMapper::toUserBadgeResponse)
                .toList();
    }
}
