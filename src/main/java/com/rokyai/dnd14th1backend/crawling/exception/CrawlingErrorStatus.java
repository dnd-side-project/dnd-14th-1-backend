package com.rokyai.dnd14th1backend.crawling.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.rokyai.dnd14th1backend.common.response.StatusInterface;

/** 크롤링 도메인 상태 코드 (2000-2999) */
@Getter
@RequiredArgsConstructor
public enum CrawlingErrorStatus implements StatusInterface {
    // 잘못된 요청 (2400)
    INVALID_URL(400, 2400, "유효하지 않은 URL입니다."),
    UNSUPPORTED_PLATFORM(400, 2401, "지원하지 않는 플랫폼입니다."),

    // 리소스 없음 (2404)
    TASK_NOT_FOUND(404, 2404, "크롤링 작업을 찾을 수 없습니다."),
    CONVERSATION_NOT_FOUND(404, 2405, "Conversation을 찾을 수 없습니다."),
    CHAT_NOT_FOUND(404, 2406, "Chat을 찾을 수 없습니다."),

    // 서버 오류 (2500)
    CRAWLING_FAILED(500, 2500, "크롤링에 실패했습니다."),
    CRAWLING_TIMEOUT(500, 2501, "허용된 시간을 초과했습니다.");

    private final int httpStatusCode;
    private final int customStatusCode;
    private final String description;
}
