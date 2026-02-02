package com.rokyai.dnd14th1backend.common.util;

import java.util.EnumSet;
import java.util.UUID;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;

import com.github.f4b6a3.uuid.UuidCreator;

/**
 * Hibernate 6.2+ 방식의 UUID v7 생성기.
 * 시간 기반 정렬 가능한 UUID를 생성합니다.
 */
public class UuidV7Generator implements BeforeExecutionGenerator {

    @Override
    public UUID generate(
            SharedSessionContractImplementor session,
            Object owner,
            Object currentValue,
            EventType eventType) {
        return UuidCreator.getTimeOrderedEpoch();
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }
}
