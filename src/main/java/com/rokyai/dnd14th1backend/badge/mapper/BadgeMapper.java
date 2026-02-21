package com.rokyai.dnd14th1backend.badge.mapper;

import com.rokyai.dnd14th1backend.badge.dto.BadgeResponse;
import com.rokyai.dnd14th1backend.badge.entity.Badge;

/** Badge Entity-DTO 변환 매퍼. */
public final class BadgeMapper {

    private BadgeMapper() {}

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
}
