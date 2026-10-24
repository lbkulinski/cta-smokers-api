package com.ctasmokers.smoking.report.cursor;

import org.junit.jupiter.api.Test;

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

    @Test
    void constructor_negativeExpiresAt_throws() {
        assertThatThrownBy(() -> new ReportPageCursor("2435#RED", "REPORT", -1L))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
