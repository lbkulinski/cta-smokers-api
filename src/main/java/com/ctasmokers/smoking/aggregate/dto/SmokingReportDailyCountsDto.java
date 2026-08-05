package com.ctasmokers.smoking.aggregate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Objects;

@Schema(description = "Response body for daily smoking report counts within a month")
@NullMarked
public record SmokingReportDailyCountsDto(
    @Schema(
        description = "Daily counts, one entry per day with at least one recorded report",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    List<SmokingReportDailyCountDto> days
) {
    public SmokingReportDailyCountsDto {
        Objects.requireNonNull(days);

        days = List.copyOf(days);
    }
}
