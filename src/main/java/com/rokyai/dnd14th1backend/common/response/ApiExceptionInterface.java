package com.rokyai.dnd14th1backend.common.response;

/** API 예외 정보 인터페이스. 예외 응답의 data 필드에 담길 상세 정보를 정의. */
public interface ApiExceptionInterface {

    /**
     * 예외 상세 정보 반환
     *
     * @return 상세 메시지
     */
    String getDetail();
}
