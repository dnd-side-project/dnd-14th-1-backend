package com.rokyai.dnd14th1backend.auth.exception;

import com.rokyai.dnd14th1backend.common.exception.ApiException;
import com.rokyai.dnd14th1backend.common.response.StatusInterface;

/** 인증 관련 예외의 기본 클래스. */
public class AuthException extends ApiException {

    public AuthException(StatusInterface status) {
        super(status);
    }

    public AuthException(StatusInterface status, String message) {
        super(status, message);
    }

    public AuthException(StatusInterface status, String message, Throwable cause) {
        super(status, message, cause);
    }
}
