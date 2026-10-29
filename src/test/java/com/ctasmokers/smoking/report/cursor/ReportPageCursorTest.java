package com.ctasmokers.smoking.report.cursor;

import com.cta4j.train.common.model.TrainLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportPageCursorTest {
    @Test
    void constructor_validValues_exposesComponents() {
        var cursor = new ReportPageCursor("2435#RED", "REPORT", 0L);

        assertThat(cursor.carNumberLine()).isEqualTo("2435#RED");
        assertThat(cursor.gsiPk()).isEqualTo("REPORT");
        assertThat(cursor.expiresAt()).isZero();
    }

    @Test
    void constructor_nullCarNumberLine_throws() {
        assertThatThrownBy(() -> new ReportPageCursor(null, "REPORT", 1L))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_nullGsiPk_throws() {
        assertThatThrownBy(() -> new ReportPageCursor("2435#RED", null, 1L))
            .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(TrainLine.class)
    void constructor_everyTrainLine_isAccepted(TrainLine line) {
        var cursor = new ReportPageCursor("2435#" + line.name(), "REPORT", 1L);

        assertThat(cursor.carNumberLine()).isEqualTo("2435#" + line.name());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "garbage", "2435", "243#RED", "2435#red", "2435#SILVER", "2435#RED\n"})
    void constructor_malformedCarNumberLine_throws(String carNumberLine) {
        assertThatThrownBy(() -> new ReportPageCursor(carNumberLine, "REPORT", 1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("carNumberLine");
    }

    @Test
    void constructor_veryLongCarNumberLine_throws() {
        String carNumberLine = "2435#" + "R".repeat(3000);

        assertThatThrownBy(() -> new ReportPageCursor(carNumberLine, "REPORT", 1L))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_negativeExpiresAt_throws() {
        assertThatThrownBy(() -> new ReportPageCursor("2435#RED", "REPORT", -1L))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toKey_buildsDynamoKeyFromCursor() {
        var cursor = new ReportPageCursor("2435#RED", "REPORT", 1771712002L);

        assertThat(cursor.toKey()).isEqualTo(Map.of(
            "carNumberLine", AttributeValue.fromS("2435#RED"),
            "gsiPk", AttributeValue.fromS("REPORT"),
            "expiresAt", AttributeValue.fromN("1771712002")));
    }
}
