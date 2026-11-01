package com.ctasmokers.smoking.aggregate.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmokingReportAggregateDtoTest {
    @Test
    void constructor_negativeReportCount_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new SmokingReportAggregateDto(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("reportCount cannot be negative");
    }

    @Test
    void constructor_zeroReportCount_succeeds() {
        var dto = new SmokingReportAggregateDto(0);

        assertThat(dto.reportCount()).isZero();
    }
}