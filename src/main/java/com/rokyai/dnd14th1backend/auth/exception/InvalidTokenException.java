package com.rokyai.dnd14th1backend.auth.exception;

import com.rokyai.dnd14th1backend.common.exception.ApiException;
import com.rokyai.dnd14th1backend.common.response.StatusInterface;

/** JWT 토큰이 유효하지 않을 때 발생하는 예외. */
public class InvalidTokenException extends ApiException {

    public InvalidTokenException(StatusInterface status) {
        super(status);
    }

    public InvalidTokenException(StatusInterface status, String message) {
        super(status, message);
    }

    public InvalidTokenException(StatusInterface status, String message, Throwable cause) {
        super(status, message, cause);
    }
}
