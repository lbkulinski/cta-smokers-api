package com.ctasmokers.smoking.report.model;

import com.cta4j.train.common.model.TrainLine;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmokingReportSchemaTest {
    @Test
    void carNumberLine_joinsCarNumberAndLineWithHash() {
        assertThat(SmokingReportSchema.carNumberLine("2435", TrainLine.RED)).isEqualTo("2435#RED");
        assertThat(SmokingReportSchema.carNumberLine("5001", TrainLine.BLUE)).isEqualTo("5001#BLUE");
    }

    @Test
    void gsiPk_isConstant() {
        assertThat(SmokingReportSchema.GSI_PK_VALUE).isEqualTo("REPORT");
    }

    @Test
    void constructor_isNotInstantiable() throws NoSuchMethodException {
        Constructor<SmokingReportSchema> constructor = SmokingReportSchema.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThatThrownBy(constructor::newInstance)
            .isInstanceOf(InvocationTargetException.class)
            .hasCauseInstanceOf(UnsupportedOperationException.class);
    }
}
