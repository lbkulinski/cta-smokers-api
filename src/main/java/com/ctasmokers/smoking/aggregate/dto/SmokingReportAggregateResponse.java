package com.ctasmokers.smoking.aggregate.dto;

import com.ctasmokers.smoking.aggregate.model.SmokingReportAggregate;

public record SmokingReportAggregateResponse(long reportCount) {
    public SmokingReportAggregateResponse {
        if (reportCount < 0) {
            throw new IllegalArgumentException("reportCount cannot be negative");
        }
    }

    public static SmokingReportAggregateResponse from(SmokingReportAggregate aggregate) {
        return new SmokingReportAggregateResponse(aggregate.reportCount());
    }
}
