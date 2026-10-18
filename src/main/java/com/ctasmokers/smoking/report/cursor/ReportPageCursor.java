package com.ctasmokers.smoking.report.cursor;

import org.jspecify.annotations.NullMarked;

import java.time.LocalDate;
import java.util.Objects;

@NullMarked
public record ReportPageCursor(
    String carNumberLine,
    LocalDate reportDate,
    long expiresAt
) {
    public ReportPageCursor {
        Objects.requireNonNull(carNumberLine);
        Objects.requireNonNull(reportDate);

        if (expiresAt < 0L) {
            throw new IllegalArgumentException("expiresAt must be non-negative");
        }
    }
}
