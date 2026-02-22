package com.rokyai.dnd14th1backend.badge;

/** 배지 달성 조건 타입. */
public enum BadgeTriggerType {
    /** 누적 채팅 최적화 횟수 기준 */
    OPTIMIZE_COUNT,
    /** 단회 최적화에서의 토큰 절약량 기준 */
    SINGLE_TOKEN_SAVING,
    /** 누적 크롤링(URL 제출) 횟수 기준 */
    CRAWLING_COUNT,
    /** 누적 XP 기준 */
    CUMULATIVE_XP
}
