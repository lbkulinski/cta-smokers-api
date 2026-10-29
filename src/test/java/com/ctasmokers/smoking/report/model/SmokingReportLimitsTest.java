package com.ctasmokers.smoking.report.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmokingReportLimitsTest {
    @Test
    void defaultLimit_isWithinMaxLimit() {
        assertThat(SmokingReportLimits.DEFAULT_LIMIT).isEqualTo(25);
        assertThat(SmokingReportLimits.MAX_LIMIT).isEqualTo(100);
        assertThat(SmokingReportLimits.DEFAULT_LIMIT).isBetween(1, SmokingReportLimits.MAX_LIMIT);
    }

    @Test
    void constructor_isNotInstantiable() throws NoSuchMethodException {
        Constructor<SmokingReportLimits> constructor = SmokingReportLimits.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThatThrownBy(constructor::newInstance)
            .isInstanceOf(InvocationTargetException.class)
            .hasCauseInstanceOf(UnsupportedOperationException.class);
    }
}
