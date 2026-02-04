package com.rokyai.dnd14th1backend.auth.exception;

import com.rokyai.dnd14th1backend.common.exception.ApiException;
import com.rokyai.dnd14th1backend.common.response.StatusInterface;

/** ID Token이 유효하지 않을 때 발생하는 예외. */
public class InvalidIdTokenException extends ApiException {

    public InvalidIdTokenException() {
        super(AuthStatus.INVALID_ID_TOKEN);
    }

    public InvalidIdTokenException(String message) {
        super(AuthStatus.INVALID_ID_TOKEN, message);
    }

    public InvalidIdTokenException(String message, Throwable cause) {
        super(AuthStatus.INVALID_ID_TOKEN, message, cause);
    }

    public InvalidIdTokenException(StatusInterface status) {
        super(status);
    }

    public InvalidIdTokenException(StatusInterface status, String message) {
        super(status, message);
    }

    public InvalidIdTokenException(StatusInterface status, String message, Throwable cause) {
        super(status, message, cause);
    }
}
