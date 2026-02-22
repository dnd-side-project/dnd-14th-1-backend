package com.rokyai.dnd14th1backend.promptanalysis.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.rokyai.dnd14th1backend.common.response.StatusInterface;

/** 프롬프트 분석 도메인 상태 코드 (4000-4999) */
@Getter
@RequiredArgsConstructor
public enum PromptAnalysisErrorStatus implements StatusInterface {
    INVALID_ANALYSIS_RESULT(400, 4400, "유효하지 않은 분석 결과입니다.");

    private final int httpStatusCode;
    private final int customStatusCode;
    private final String description;
}
