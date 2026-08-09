package com.ctasmokers.smoking.aggregate.model;

import org.jspecify.annotations.NullMarked;

import java.time.LocalDate;
import java.util.Objects;

@NullMarked
public record SmokingReportDailyCount(
    LocalDate date,
    long reportCount
) {
    public SmokingReportDailyCount {
        Objects.requireNonNull(date);

        if (reportCount < 0) {
            throw new IllegalArgumentException("reportCount cannot be negative");
        }
    }
}
