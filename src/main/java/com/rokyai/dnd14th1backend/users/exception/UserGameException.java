package com.rokyai.dnd14th1backend.users.exception;

import com.rokyai.dnd14th1backend.common.exception.ApiException;
import com.rokyai.dnd14th1backend.common.response.StatusInterface;

/** 사용자 게임 관련 예외 */
public class UserGameException extends ApiException {

    public UserGameException(StatusInterface status) {
        super(status);
    }

    public UserGameException(StatusInterface status, String message) {
        super(status, message);
    }

    public UserGameException(StatusInterface status, String message, Throwable cause) {
        super(status, message, cause);
    }
}
