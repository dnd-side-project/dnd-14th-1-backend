package com.rokyai.dnd14th1backend.auth.exception;

import com.rokyai.dnd14th1backend.common.exception.ApiException;
import com.rokyai.dnd14th1backend.common.response.StatusInterface;

/** Refresh Token이 유효하지 않을 때 발생하는 예외. */
public class InvalidRefreshTokenException extends ApiException {

    public InvalidRefreshTokenException() {
        super(AuthStatus.INVALID_REFRESH_TOKEN);
    }

    public InvalidRefreshTokenException(String message) {
        super(AuthStatus.INVALID_REFRESH_TOKEN, message);
    }

    public InvalidRefreshTokenException(String message, Throwable cause) {
        super(AuthStatus.INVALID_REFRESH_TOKEN, message, cause);
    }

    public InvalidRefreshTokenException(StatusInterface status) {
        super(status);
    }

    public InvalidRefreshTokenException(StatusInterface status, String message) {
        super(status, message);
    }

    public InvalidRefreshTokenException(StatusInterface status, String message, Throwable cause) {
        super(status, message, cause);
    }
}
