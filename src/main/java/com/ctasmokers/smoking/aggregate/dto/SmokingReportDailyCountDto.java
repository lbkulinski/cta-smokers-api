package com.ctasmokers.smoking.aggregate.dto;

import com.ctasmokers.smoking.aggregate.model.SmokingReportDailyCount;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NullMarked;

import java.time.LocalDate;
import java.util.Objects;

@Schema(description = "Daily smoking report count for a specific date")
@NullMarked
public record SmokingReportDailyCountDto(
    @Schema(
        description = "The date of the count",
        example = "2026-02-21",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    LocalDate date,

    @Schema(
        description = "Number of smoking reports on that date",
        example = "42",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    long reportCount
) {
    public SmokingReportDailyCountDto {
        Objects.requireNonNull(date);

        if (reportCount < 0) {
            throw new IllegalArgumentException("reportCount cannot be negative");
        }
    }

    public static SmokingReportDailyCountDto from(SmokingReportDailyCount dailyCount) {
        Objects.requireNonNull(dailyCount);

        return new SmokingReportDailyCountDto(dailyCount.date(), dailyCount.reportCount());
    }
}
