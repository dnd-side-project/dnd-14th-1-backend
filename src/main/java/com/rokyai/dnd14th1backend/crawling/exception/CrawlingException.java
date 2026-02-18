package com.rokyai.dnd14th1backend.crawling.exception;

import com.rokyai.dnd14th1backend.common.exception.ApiException;
import com.rokyai.dnd14th1backend.common.response.StatusInterface;

/** 크롤링 관련 예외의 기본 클래스 */
public class CrawlingException extends ApiException {

    public CrawlingException(StatusInterface status) {
        super(status);
    }

    public CrawlingException(StatusInterface status, String message) {
        super(status, message);
    }

    public CrawlingException(StatusInterface status, String message, Throwable cause) {
        super(status, message, cause);
    }
}
