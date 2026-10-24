package com.ctasmokers.smoking.report.model;

import com.cta4j.train.common.model.TrainLine;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmokingReportKeysTest {
    @Test
    void carNumberLine_joinsCarNumberAndLineWithHash() {
        assertThat(SmokingReportKeys.carNumberLine("2435", TrainLine.RED)).isEqualTo("2435#RED");
        assertThat(SmokingReportKeys.carNumberLine("5001", TrainLine.BLUE)).isEqualTo("5001#BLUE");
    }

    @Test
    void gsiPk_isConstant() {
        assertThat(SmokingReportKeys.GSI_PK).isEqualTo("REPORT");
    }

    @Test
    void constructor_isNotInstantiable() throws NoSuchMethodException {
        Constructor<SmokingReportKeys> constructor = SmokingReportKeys.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThatThrownBy(constructor::newInstance)
            .isInstanceOf(InvocationTargetException.class)
            .hasCauseInstanceOf(UnsupportedOperationException.class);
    }
}
