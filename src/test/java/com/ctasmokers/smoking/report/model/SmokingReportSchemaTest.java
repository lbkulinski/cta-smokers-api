package com.ctasmokers.smoking.report.model;

import com.cta4j.train.common.model.TrainLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @ValueSource(strings = {"243", "24355", "abcd", "", " 2435", "2435 ", "2435\n"})
    void carNumberLine_invalidCarNumber_throws(String carNumber) {
        assertThatThrownBy(() -> SmokingReportSchema.carNumberLine(carNumber, TrainLine.RED))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("carNumber");
    }

    @Test
    void carNumberLine_nullArguments_throw() {
        assertThatThrownBy(() -> SmokingReportSchema.carNumberLine(null, TrainLine.RED))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SmokingReportSchema.carNumberLine("2435", null))
            .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(TrainLine.class)
    void isValidCarNumberLine_everyTrainLine_isValid(TrainLine line) {
        assertThat(SmokingReportSchema.isValidCarNumberLine("2435#" + line.name())).isTrue();
    }

    @ParameterizedTest
    @EnumSource(TrainLine.class)
    void isValidCarNumberLine_acceptsWhatCarNumberLineBuilds(TrainLine line) {
        String carNumberLine = SmokingReportSchema.carNumberLine("2435", line);

        assertThat(SmokingReportSchema.isValidCarNumberLine(carNumberLine)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "2435",
        "2435#",
        "#RED",
        "2435RED",
        "243#RED",
        "24355#RED",
        "abcd#RED",
        "2435#red",
        "2435#SILVER",
        "2435#RED#BLUE",
        " 2435#RED",
        "2435#RED ",
        "2435#RED\n"
    })
    void isValidCarNumberLine_malformed_isInvalid(String carNumberLine) {
        assertThat(SmokingReportSchema.isValidCarNumberLine(carNumberLine)).isFalse();
    }

    @Test
    void isValidCarNumberLine_veryLongValue_isInvalid() {
        assertThat(SmokingReportSchema.isValidCarNumberLine("2435#" + "R".repeat(3000))).isFalse();
    }

    @Test
    void isValidCarNumberLine_null_throws() {
        assertThatThrownBy(() -> SmokingReportSchema.isValidCarNumberLine(null))
            .isInstanceOf(NullPointerException.class);
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
