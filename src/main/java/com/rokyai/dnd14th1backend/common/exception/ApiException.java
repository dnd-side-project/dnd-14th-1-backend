package com.rokyai.dnd14th1backend.common.exception;

import lombok.Getter;

import com.rokyai.dnd14th1backend.common.response.StatusInterface;

/** API 예외 기본 클래스. 비즈니스 로직에서 발생하는 모든 API 예외는 이 클래스를 상속. */
@Getter
public class ApiException extends RuntimeException {

    private final StatusInterface status;

    public ApiException(StatusInterface status) {
        super(status.getDescription());
        this.status = status;
    }

    public ApiException(StatusInterface status, String detail) {
        super(detail);
        this.status = status;
    }

    public ApiException(StatusInterface status, String detail, Throwable cause) {
        super(detail, cause);
        this.status = status;
    }

    public String getDetail() {
        return super.getMessage();
    }
}
