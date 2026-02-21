package com.ctasmokers.smoking.dto;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@NullMarked
public record SmokingReportsResponse(
    List<SmokingReportResponse> reports,

    @Nullable
    String nextCursor
) {
    public SmokingReportsResponse {
        Objects.requireNonNull(reports);

        reports.forEach(Objects::requireNonNull);

        reports = List.copyOf(reports);
    }
}
