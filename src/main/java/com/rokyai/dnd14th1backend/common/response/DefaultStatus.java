package com.rokyai.dnd14th1backend.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 기본 상태 코드 정의. 도메인별 상태 코드는 별도 Enum으로 StatusInterface를 구현하여 사용. */
@Getter
@RequiredArgsConstructor
public enum DefaultStatus implements StatusInterface {
    // 성공
    OK(200, 200, "성공"),

    // 클라이언트 에러
    BAD_REQUEST(400, 400, "잘못된 요청입니다"),
    UNAUTHORIZED(401, 401, "인증이 필요합니다"),
    FORBIDDEN(403, 403, "접근 권한이 없습니다"),
    NOT_FOUND(404, 404, "리소스를 찾을 수 없습니다"),
    CONFLICT(409, 409, "리소스 충돌이 발생했습니다"),

    // 서버 에러
    INTERNAL_SERVER_ERROR(500, 500, "서버 내부 오류가 발생했습니다"),
    UNKNOWN_ERROR(500, 500, "알 수 없는 오류가 발생했습니다");

    private final int httpStatusCode;
    private final int customStatusCode;
    private final String description;
}
