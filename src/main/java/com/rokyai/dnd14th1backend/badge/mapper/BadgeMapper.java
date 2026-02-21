package com.rokyai.dnd14th1backend.badge.mapper;

import com.rokyai.dnd14th1backend.badge.dto.BadgeResponse;
import com.rokyai.dnd14th1backend.badge.dto.EarnedBadgeInfo;
import com.rokyai.dnd14th1backend.badge.dto.UserBadgeResponse;
import com.rokyai.dnd14th1backend.badge.entity.Badge;
import com.rokyai.dnd14th1backend.badge.entity.UserBadge;

/** Badge Entity-DTO 변환 매퍼. */
public final class BadgeMapper {

    private BadgeMapper() {}

    /** Badge → BadgeResponse (전체 배지 목록 조회용). */
    public static BadgeResponse toResponse(Badge badge) {
        return new BadgeResponse(
                badge.getId(),
                badge.getName(),
                badge.getDescription(),
                badge.getTier(),
                badge.getTriggerType(),
                badge.getTriggerCondition(),
                badge.getEnableImageUrl(),
                badge.getDisableImageUrl());
    }

    /** UserBadge → UserBadgeResponse (유저 보유 배지 목록 조회용). */
    public static UserBadgeResponse toUserBadgeResponse(UserBadge userBadge) {
        Badge badge = userBadge.getBadge();
        return new UserBadgeResponse(
                badge.getId(),
                badge.getName(),
                badge.getDescription(),
                badge.getTier(),
                badge.getTriggerType(),
                badge.getTriggerCondition(),
                badge.getEnableImageUrl(),
                badge.getDisableImageUrl(),
                userBadge.getEarnedAt());
    }

    /** UserBadge → EarnedBadgeInfo (API 응답 임베딩용). */
    public static EarnedBadgeInfo toEarnedBadgeInfo(UserBadge userBadge) {
        Badge badge = userBadge.getBadge();
        return new EarnedBadgeInfo(
                badge.getId(),
                badge.getName(),
                badge.getTier(),
                badge.getEnableImageUrl(),
                badge.getDisableImageUrl(),
                userBadge.getEarnedAt());
    }
}
