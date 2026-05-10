package com.ctasmokers.smoking.aggregate.dto;

import com.ctasmokers.smoking.aggregate.model.SmokingReportAggregate;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NullMarked;

@Schema(description = "Response body for a smoking report aggregate")
@NullMarked
public record SmokingReportAggregateResponse(
    @Schema(description = "Total number of smoking reports", example = "42")
    long reportCount
) {
    public SmokingReportAggregateResponse {
        if (reportCount < 0) {
            throw new IllegalArgumentException("reportCount cannot be negative");
        }
    }

    public static SmokingReportAggregateResponse from(SmokingReportAggregate aggregate) {
        return new SmokingReportAggregateResponse(aggregate.reportCount());
    }
}
