package com.ctasmokers.smoking.report.dto;

import com.cta4j.train.common.model.TrainLine;
import com.ctasmokers.smoking.report.model.SmokingReport;
import com.ctasmokers.smoking.report.model.SmokingReportKeys;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmokingReportDtoTest {
    private static SmokingReport report(String runNumber) {
        return SmokingReport.builder()
                            .carNumberLine("2435#RED")
                            .carNumber("2435")
                            .line(TrainLine.RED)
                            .gsiPk(SmokingReportKeys.GSI_PK)
                            .reportedAt(Instant.parse("2026-05-10T12:00:00Z"))
                            .expiresAt(Instant.parse("2026-05-10T12:30:00Z").getEpochSecond())
                            .destinationId("40900")
                            .nextStationId("41220")
                            .runNumber(runNumber)
                            .build();
    }

    @Test
    void from_mapsAllFieldsAndConvertsExpiresAtToInstant() {
        var dto = SmokingReportDto.from(report("902"));

        assertThat(dto.carNumber()).isEqualTo("2435");
        assertThat(dto.line()).isEqualTo(TrainLine.RED);
        assertThat(dto.reportedAt()).isEqualTo(Instant.parse("2026-05-10T12:00:00Z"));
        assertThat(dto.expiresAt()).isEqualTo(Instant.parse("2026-05-10T12:30:00Z"));
        assertThat(dto.destinationId()).isEqualTo("40900");
        assertThat(dto.nextStationId()).isEqualTo("41220");
        assertThat(dto.runNumber()).isEqualTo("902");
    }

    @Test
    void from_withoutRunNumber_mapsNull() {
        assertThat(SmokingReportDto.from(report(null)).runNumber()).isNull();
    }

    @Test
    void from_nullReport_throws() {
        assertThatThrownBy(() -> SmokingReportDto.from(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_nullRequiredField_throws() {
        var now = Instant.now();

        assertThatThrownBy(() -> new SmokingReportDto(null, TrainLine.RED, now, now, "1", "2", null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new SmokingReportDto("2435", null, now, now, "1", "2", null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new SmokingReportDto("2435", TrainLine.RED, null, now, "1", "2", null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new SmokingReportDto("2435", TrainLine.RED, now, null, "1", "2", null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new SmokingReportDto("2435", TrainLine.RED, now, now, null, "2", null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new SmokingReportDto("2435", TrainLine.RED, now, now, "1", null, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void pageDto_nullReports_throws() {
        assertThatThrownBy(() -> new SmokingReportPageDto(null, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void pageDto_copiesReportsDefensively() {
        var reports = new ArrayList<SmokingReportDto>();

        var page = new SmokingReportPageDto(reports, "cursor");

        reports.add(SmokingReportDto.from(report(null)));

        assertThat(page.reports()).isEmpty();
        assertThat(page.nextCursor()).isEqualTo("cursor");
        assertThat(new SmokingReportPageDto(List.of(), null).nextCursor()).isNull();
    }
}
