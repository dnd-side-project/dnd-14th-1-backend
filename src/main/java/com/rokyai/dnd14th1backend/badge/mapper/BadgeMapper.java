package com.rokyai.dnd14th1backend.badge.mapper;

import java.util.UUID;

import com.rokyai.dnd14th1backend.badge.dto.BadgeResponse;
import com.rokyai.dnd14th1backend.badge.dto.EarnedBadgeInfo;
import com.rokyai.dnd14th1backend.badge.dto.UserBadgeResponse;
import com.rokyai.dnd14th1backend.badge.entity.Badge;
import com.rokyai.dnd14th1backend.badge.entity.UserBadge;
import com.rokyai.dnd14th1backend.badge.util.BadgeImageUrlResolver;

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
                BadgeImageUrlResolver.toPublicUrl(badge.getEnableImageUrl()),
                BadgeImageUrlResolver.toPublicUrl(badge.getDisableImageUrl()));
    }

    /**
     * UserBadge → UserBadgeResponse (유저 보유 배지 목록 조회용).
     *
     * @param userBadge 유저-배지 연관 엔티티
     * @param representativeUserBadgeId 사용자의 대표 유저-배지 ID (없으면 null)
     */
    public static UserBadgeResponse toUserBadgeResponse(
            UserBadge userBadge, UUID representativeUserBadgeId) {
        Badge badge = userBadge.getBadge();
        return new UserBadgeResponse(
                badge.getId(),
                badge.getName(),
                badge.getDescription(),
                badge.getTier(),
                badge.getTriggerType(),
                badge.getTriggerCondition(),
                BadgeImageUrlResolver.toPublicUrl(badge.getEnableImageUrl()),
                BadgeImageUrlResolver.toPublicUrl(badge.getDisableImageUrl()),
                userBadge.getEarnedAt(),
                userBadge.getId().equals(representativeUserBadgeId));
    }

    /** UserBadge → EarnedBadgeInfo (API 응답 임베딩용). */
    public static EarnedBadgeInfo toEarnedBadgeInfo(UserBadge userBadge) {
        Badge badge = userBadge.getBadge();
        return new EarnedBadgeInfo(
                badge.getId(),
                badge.getName(),
                badge.getTier(),
                BadgeImageUrlResolver.toPublicUrl(badge.getEnableImageUrl()),
                BadgeImageUrlResolver.toPublicUrl(badge.getDisableImageUrl()),
                userBadge.getEarnedAt());
    }
}
