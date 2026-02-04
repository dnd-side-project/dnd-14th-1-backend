package com.rokyai.dnd14th1backend.common.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.hibernate.annotations.IdGeneratorType;

/** UUID v7 생성을 위한 커스텀 어노테이션. Entity의 @Id 필드에 사용합니다. */
@IdGeneratorType(UuidV7Generator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UuidV7 {}
