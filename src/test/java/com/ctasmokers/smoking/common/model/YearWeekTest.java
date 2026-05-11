package com.ctasmokers.smoking.common.model;

import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YearWeekTest {
    @Test
    void constructor_validYearAndWeek_succeeds() {
        YearWeek yearWeek = new YearWeek(2026, 13);

        assertThat(yearWeek.year()).isEqualTo(2026);
        assertThat(yearWeek.week()).isEqualTo(13);
    }

    @Test
    void constructor_yearBelowMinValue_throws() {
        assertThatThrownBy(() -> new YearWeek(Year.MIN_VALUE - 1, 1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_yearAboveMaxValue_throws() {
        assertThatThrownBy(() -> new YearWeek(Year.MAX_VALUE + 1, 1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_weekBelowOne_throws() {
        assertThatThrownBy(() -> new YearWeek(2026, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_weekAboveMaxForYear_throws() {
        // 2025 has 52 ISO weeks
        assertThatThrownBy(() -> new YearWeek(2025, 53))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_week53ValidFor53WeekYear_succeeds() {
        // 2026 has 53 ISO weeks (starts on a Thursday)
        YearWeek yearWeek = new YearWeek(2026, 53);

        assertThat(yearWeek.week()).isEqualTo(53);
    }

    @Test
    void toString_formatsCorrectly() {
        assertThat(new YearWeek(2026, 13).toString()).isEqualTo("2026-W13");
    }

    @Test
    void toString_padsSingleDigitWeek() {
        assertThat(new YearWeek(2026, 1).toString()).isEqualTo("2026-W01");
    }
}
