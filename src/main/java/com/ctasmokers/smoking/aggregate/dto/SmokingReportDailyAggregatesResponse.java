package com.ctasmokers.smoking.aggregate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Objects;

@Schema(description = "Response body for daily smoking report aggregates within a month")
@NullMarked
public record SmokingReportDailyAggregatesResponse(
    @Schema(description = "Daily aggregates, one entry per day with recorded reports")
    List<SmokingReportDailyCount> days
) {
    public SmokingReportDailyAggregatesResponse {
        Objects.requireNonNull(days);

        days = List.copyOf(days);
    }
}
