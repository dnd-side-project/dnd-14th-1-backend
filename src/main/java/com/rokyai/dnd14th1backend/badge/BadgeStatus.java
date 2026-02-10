package com.rokyai.dnd14th1backend.badge;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.rokyai.dnd14th1backend.common.response.StatusInterface;

/** 배지 도메인 상태 코드. (4000~4999: Badge domain) */
@Getter
@RequiredArgsConstructor
public enum BadgeStatus implements StatusInterface {
    BADGE_NOT_FOUND(404, 4000, "배지를 찾을 수 없습니다");

    private final int httpStatusCode;
    private final int customStatusCode;
    private final String description;
}
