package com.rokyai.dnd14th1backend.common.response;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** ApiResponseWrapper 래핑을 건너뛰는 어노테이션. 파일 다운로드, SSE 등 원시 응답이 필요한 경우 사용. */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SkipApiResponseWrapper {}
