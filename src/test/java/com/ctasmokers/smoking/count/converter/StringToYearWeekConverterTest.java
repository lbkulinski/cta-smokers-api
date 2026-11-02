package com.ctasmokers.smoking.count.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.threeten.extra.YearWeek;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StringToYearWeekConverterTest {
    private final StringToYearWeekConverter converter = new StringToYearWeekConverter();

    @Test
    void convert_validString_returnsYearWeek() {
        YearWeek result = converter.convert("2026-W13");

        assertThat(result.getYear()).isEqualTo(2026);
        assertThat(result.getWeek()).isEqualTo(13);
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

    @ParameterizedTest
    @ValueSource(strings = {"2026-W5", "+026-W13", "2026-W+5", "202-W13", "20266-W13", "2026-W13 ", " 2026-W13",
                            "2026-W13\n", ""})
    void convert_nonCanonicalFormat_throws(String source) {
        assertThatThrownBy(() -> converter.convert(source))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-W00", "2026-W54", "2025-W54"})
    void convert_weekOutOfRange_throws(String source) {
        assertThatThrownBy(() -> converter.convert(source))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void convert_week53InYearWithFiftyThreeWeeks_returnsYearWeek() {
        assertThat(converter.convert("2026-W53")).isEqualTo(YearWeek.of(2026, 53));
    }

    @Test
    void convert_lowercaseW_isAccepted() {
        assertThat(converter.convert("2026-w13")).isEqualTo(YearWeek.of(2026, 13));
    }

    @Test
    void convert_week53InYearWithFiftyTwoWeeks_rollsOverToNextYear() {
        assertThat(converter.convert("2025-W53")).isEqualTo(YearWeek.of(2026, 1));
    }

    @Test
    void convert_toStringOutput_roundTrips() {
        YearWeek yearWeek = YearWeek.of(2026, 1);

        assertThat(converter.convert(yearWeek.toString())).isEqualTo(yearWeek);
    }
}