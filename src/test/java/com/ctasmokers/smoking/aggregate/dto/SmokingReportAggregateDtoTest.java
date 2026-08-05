package com.ctasmokers.smoking.aggregate.dto;

import com.ctasmokers.smoking.aggregate.model.SmokingReportAggregate;
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
    void from_mapsReportCountFromAggregate() {
        SmokingReportAggregate aggregate = SmokingReportAggregate.builder()
                                                                  .pk("LINE#RED")
                                                                  .sk("ALL_TIME")
                                                                  .reportCount(42)
                                                                  .build();

        SmokingReportAggregateDto dto = SmokingReportAggregateDto.from(aggregate);

        assertThat(dto.reportCount()).isEqualTo(42);
    }
}