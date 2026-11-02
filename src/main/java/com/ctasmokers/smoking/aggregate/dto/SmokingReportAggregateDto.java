package com.ctasmokers.smoking.aggregate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NullMarked;

@Schema(description = "Response body for a smoking report aggregate")
@NullMarked
public record SmokingReportAggregateDto(
    @Schema(
        description = "Total number of smoking reports for the requested period, or 0 if none were recorded",
        example = "42",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    long reportCount
) {
    public SmokingReportAggregateDto {
        if (reportCount < 0) {
            throw new IllegalArgumentException("reportCount cannot be negative");
        }
    }
}
