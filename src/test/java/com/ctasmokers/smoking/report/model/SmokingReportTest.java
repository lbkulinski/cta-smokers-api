package com.ctasmokers.smoking.report.model;

import com.cta4j.train.common.model.TrainLine;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmokingReportTest {
    private static SmokingReport.SmokingReportBuilder validBuilder() {
        return SmokingReport.builder()
                            .carNumberLine("2435#RED")
                            .carNumber("2435")
                            .line(TrainLine.RED)
                            .gsiPk(SmokingReportSchema.GSI_PK_VALUE)
                            .reportedAt(Instant.parse("2026-05-10T12:00:00Z"))
                            .expiresAt(1778416800L)
                            .destinationId("40900")
                            .nextStationId("41220");
    }

    @Test
    void builder_withoutRunNumber_allowsNullRunNumber() {
        assertThat(validBuilder().build().runNumber()).isNull();
    }

    @Test
    void builder_withRunNumber_keepsIt() {
        assertThat(validBuilder().runNumber("902").build().runNumber()).isEqualTo("902");
    }

    @Test
    void indexName_matchesTableDefinition() {
        assertThat(SmokingReportSchema.GSI_PK_EXPIRES_AT_INDEX).isEqualTo("gsiPk-expiresAt-index");
    }

    @Test
    void builder_missingRequiredFields_throws() {
        assertThatThrownBy(() -> validBuilder().carNumberLine(null).build()).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validBuilder().carNumber(null).build()).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validBuilder().line(null).build()).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validBuilder().gsiPk(null).build()).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validBuilder().reportedAt(null).build()).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validBuilder().destinationId(null).build()).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validBuilder().nextStationId(null).build()).isInstanceOf(NullPointerException.class);
    }
}
