package com.rokyai.dnd14th1backend.config;

/** API 관련 상수를 정의하는 클래스. 버전 관리 헤더명과 기본 버전 등 공통 상수를 관리한다. */
public final class ApiConstants {

    private ApiConstants() {}

    /** API 버전 지정에 사용되는 HTTP 헤더 이름 */
    public static final String API_VERSION_HEADER = "X-API-Version";

    /** 헤더가 없을 때 적용되는 기본 API 버전 */
    public static final String DEFAULT_API_VERSION = "0.0.1";
}
