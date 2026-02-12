package com.rokyai.dnd14th1backend.users.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.rokyai.dnd14th1backend.common.response.StatusInterface;

/** 사용자 게임 도메인 상태 코드 (3000-3999) */
@Getter
@RequiredArgsConstructor
public enum UserGameErrorStatus implements StatusInterface {
    ALREADY_OPTIMIZED(400, 3400, "이미 최적화된 Chat입니다."),
    INVALID_TOKEN_SAVING(400, 3401, "유효하지 않은 토큰 절약량입니다."),
    PROFILE_NOT_FOUND(404, 3404, "게임 프로필을 찾을 수 없습니다.");

    private final int httpStatusCode;
    private final int customStatusCode;
    private final String description;
}
