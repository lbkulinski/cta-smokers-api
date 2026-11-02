package com.ctasmokers.smoking.count.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmokingReportCountDtoTest {
    @Test
    void constructor_negativeReportCount_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new SmokingReportCountDto(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("reportCount cannot be negative");
    }

    @Test
    void constructor_zeroReportCount_succeeds() {
        var dto = new SmokingReportCountDto(0);

        assertThat(dto.reportCount()).isZero();
    }
}