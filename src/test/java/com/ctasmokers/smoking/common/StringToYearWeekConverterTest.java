package com.ctasmokers.smoking.common;

import com.ctasmokers.smoking.common.model.YearWeek;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StringToYearWeekConverterTest {
    private final StringToYearWeekConverter converter = new StringToYearWeekConverter();

    @Test
    void convert_validString_returnsYearWeek() {
        YearWeek result = converter.convert("2026-W13");

        assertThat(result.year()).isEqualTo(2026);
        assertThat(result.week()).isEqualTo(13);
    }

    @Test
    void convert_missingDelimiter_throws() {
        assertThatThrownBy(() -> converter.convert("2026W13"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void convert_nonNumericYear_throws() {
        assertThatThrownBy(() -> converter.convert("abc-W13"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void convert_nonNumericWeek_throws() {
        assertThatThrownBy(() -> converter.convert("2026-Wabc"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
