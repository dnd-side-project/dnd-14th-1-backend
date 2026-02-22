package com.rokyai.dnd14th1backend.badge.exception;

import com.rokyai.dnd14th1backend.common.exception.ApiException;
import com.rokyai.dnd14th1backend.common.response.StatusInterface;

/** 배지 관련 예외의 기본 클래스. */
public class BadgeException extends ApiException {

    public BadgeException(StatusInterface status) {
        super(status);
    }

    public BadgeException(StatusInterface status, String message) {
        super(status, message);
    }

    public BadgeException(StatusInterface status, String message, Throwable cause) {
        super(status, message, cause);
    }
}
