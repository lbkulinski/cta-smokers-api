package com.ctasmokers.smoking.aggregate.dto;

import com.ctasmokers.smoking.aggregate.model.SmokingReportDailyCount;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmokingReportDailyCountDtoTest {
    private static final LocalDate DATE = LocalDate.of(2026, 5, 10);

    @Test
    void constructor_negativeReportCount_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new SmokingReportDailyCountDto(DATE, -1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("reportCount cannot be negative");
    }

    @Test
    void from_mapsDateAndReportCountFromDailyCount() {
        SmokingReportDailyCount dailyCount = new SmokingReportDailyCount(DATE, 7);

        SmokingReportDailyCountDto dto = SmokingReportDailyCountDto.from(dailyCount);

        assertThat(dto.date()).isEqualTo(DATE);
        assertThat(dto.reportCount()).isEqualTo(7);
    }
}