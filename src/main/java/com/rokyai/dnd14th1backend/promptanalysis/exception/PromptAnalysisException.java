package com.rokyai.dnd14th1backend.promptanalysis.exception;

import com.rokyai.dnd14th1backend.common.exception.ApiException;
import com.rokyai.dnd14th1backend.common.response.StatusInterface;

/** 프롬프트 분석 관련 예외 */
public class PromptAnalysisException extends ApiException {

    public PromptAnalysisException(StatusInterface status) {
        super(status);
    }

    public PromptAnalysisException(StatusInterface status, String message) {
        super(status, message);
    }

    public PromptAnalysisException(StatusInterface status, String message, Throwable cause) {
        super(status, message, cause);
    }
}
