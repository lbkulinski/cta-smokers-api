package com.ctasmokers.smoking.count.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NullMarked;

@Schema(description = "Response body for a smoking report count")
@NullMarked
public record SmokingReportCountDto(
    @Schema(
        description = "Total number of smoking reports for the requested period, or 0 if none were recorded",
        example = "42",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    long reportCount
) {
    public SmokingReportCountDto {
        if (reportCount < 0) {
            throw new IllegalArgumentException("reportCount cannot be negative");
        }
    }
}
