package com.rokyai.dnd14th1backend.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 기본 상태 코드 정의. 도메인별 상태 코드는 별도 Enum으로 StatusInterface를 구현하여 사용. */
@Getter
@RequiredArgsConstructor
public enum DefaultStatus implements StatusInterface {
    // 성공 (0)
    OK(200, 0, "성공"),

    // 클라이언트 에러 (1~9)
    BAD_REQUEST(400, 1, "잘못된 요청입니다"),
    UNAUTHORIZED(401, 2, "인증이 필요합니다"),
    FORBIDDEN(403, 3, "접근 권한이 없습니다"),
    NOT_FOUND(404, 4, "리소스를 찾을 수 없습니다"),
    CONFLICT(409, 5, "리소스 충돌이 발생했습니다"),

    // 서버 에러 (9000~9999)
    INTERNAL_SERVER_ERROR(500, 9000, "서버 내부 오류가 발생했습니다"),
    UNKNOWN_ERROR(500, 9001, "알 수 없는 오류가 발생했습니다");

    private final int httpStatusCode;
    private final int customStatusCode;
    private final String description;
}
