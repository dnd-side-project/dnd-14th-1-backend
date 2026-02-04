package com.rokyai.dnd14th1backend.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.rokyai.dnd14th1backend.common.response.StatusInterface;

/** 인증 도메인 상태 코드. (1000~1999: Auth domain) */
@Getter
@RequiredArgsConstructor
public enum AuthStatus implements StatusInterface {
    // 잘못된 요청 (1400)
    INVALID_ID_TOKEN(400, 1400, "유효하지 않은 ID Token입니다"),

    // 인증 실패 (1401~1404)
    INVALID_OAUTH_REQUEST(400, 1401, "유효하지 않은 OAuth 요청입니다"),
    INVALID_REFRESH_TOKEN(401, 1402, "유효하지 않은 Refresh Token입니다"),
    EXPIRED_TOKEN(401, 1403, "만료된 토큰입니다"),
    INVALID_ISSUER(401, 1404, "유효하지 않은 issuer입니다"),

    // 권한 없음 (1405)
    INVALID_AUDIENCE(403, 1405, "유효하지 않은 audience입니다");

    private final int httpStatusCode;
    private final int customStatusCode;
    private final String description;
}
