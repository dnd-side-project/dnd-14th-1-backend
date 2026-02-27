package com.rokyai.dnd14th1backend.users.mapper;

import com.rokyai.dnd14th1backend.badge.entity.Badge;
import com.rokyai.dnd14th1backend.badge.entity.UserBadge;
import com.rokyai.dnd14th1backend.badge.util.BadgeImageUrlResolver;
import com.rokyai.dnd14th1backend.users.domain.User;
import com.rokyai.dnd14th1backend.users.dto.RepresentativeBadgeResponse;
import com.rokyai.dnd14th1backend.users.dto.UserMeResponse;

/** User Entity-DTO 변환 매퍼. */
public final class UserMapper {

    private UserMapper() {}

    /**
     * User → UserMeResponse 변환.
     *
     * @param user 사용자 엔티티
     * @return 내 정보 응답 DTO
     */
    public static UserMeResponse toMeResponse(User user) {
        RepresentativeBadgeResponse badgeResponse = null;

        UserBadge representativeUserBadge = user.getRepresentativeUserBadge();
        if (representativeUserBadge != null) {
            Badge badge = representativeUserBadge.getBadge();
            badgeResponse =
                    new RepresentativeBadgeResponse(
                            badge.getId(),
                            badge.getName(),
                            badge.getTier(),
                            BadgeImageUrlResolver.toPublicUrl(badge.getEnableImageUrl()),
                            BadgeImageUrlResolver.toPublicUrl(badge.getDisableImageUrl()));
        }

        return new UserMeResponse(
                user.getId(), user.getEmail(), user.getName(), user.getCreatedAt(), badgeResponse);
    }
}
