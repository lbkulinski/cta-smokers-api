package com.ctasmokers.smoking.report.cursor;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public record ReportPageCursor(
    String carNumberLine,
    String gsiPk,
    long expiresAt
) {
    public ReportPageCursor {
        Objects.requireNonNull(carNumberLine);
        Objects.requireNonNull(gsiPk);

        if (expiresAt < 0L) {
            throw new IllegalArgumentException("expiresAt must be non-negative");
        }
    }
}
