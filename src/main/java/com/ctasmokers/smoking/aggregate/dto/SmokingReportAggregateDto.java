package com.ctasmokers.smoking.aggregate.dto;

import com.ctasmokers.smoking.aggregate.model.SmokingReportAggregate;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@Schema(description = "Response body for a smoking report aggregate")
@NullMarked
public record SmokingReportAggregateDto(
    @Schema(description = "Total number of smoking reports", example = "42")
    long reportCount
) {
    public SmokingReportAggregateDto {
        if (reportCount < 0) {
            throw new IllegalArgumentException("reportCount cannot be negative");
        }
    }

    public static SmokingReportAggregateDto from(SmokingReportAggregate aggregate) {
        Objects.requireNonNull(aggregate);

        return new SmokingReportAggregateDto(aggregate.reportCount());
    }
}
