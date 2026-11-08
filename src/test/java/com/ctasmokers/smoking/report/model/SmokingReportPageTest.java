package com.ctasmokers.smoking.report.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmokingReportPageTest {
    @Test
    void constructor_nullReports_throws() {
        assertThatThrownBy(() -> new SmokingReportPage(null, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_copiesReportsDefensively() {
        var reports = new ArrayList<SmokingReport>();

        var page = new SmokingReportPage(reports, "cursor");

        reports.add(null);

        assertThat(page.reports()).isEmpty();
        assertThat(page.nextCursor()).isEqualTo("cursor");
        assertThatThrownBy(() -> page.reports().add(null)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void constructor_allowsNullCursor() {
        assertThat(new SmokingReportPage(List.of(), null).nextCursor()).isNull();
    }
}
