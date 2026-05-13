package com.ctasmokers.smoking.aggregate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NullMarked;

import java.time.LocalDate;
import java.util.Objects;

@Schema(description = "Daily smoking report count for a specific date")
@NullMarked
public record SmokingReportDailyCount(
    @Schema(description = "The date of the count", example = "2026-02-21")
    LocalDate date,

    @Schema(description = "Total number of smoking reports", example = "42")
    long reportCount
) {
    public SmokingReportDailyCount {
        Objects.requireNonNull(date);

        if (reportCount < 0) {
            throw new IllegalArgumentException("reportCount cannot be negative");
        }
    }
}
