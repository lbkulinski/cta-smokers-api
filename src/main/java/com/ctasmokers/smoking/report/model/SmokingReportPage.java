package com.ctasmokers.smoking.report.model;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@NullMarked
public record SmokingReportPage(List<SmokingReport> reports, @Nullable String nextCursor) {
    public SmokingReportPage {
        Objects.requireNonNull(reports);

        reports = List.copyOf(reports);
    }
}
