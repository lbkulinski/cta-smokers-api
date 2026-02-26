package com.ctasmokers.smoking.dto;

import com.ctasmokers.smoking.model.SmokingReport;
import com.ctasmokers.smoking.model.TrainLine;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@NullMarked
public record SmokingReportResponse(
    LocalDate date,

    String reportId,

    Instant reportedAt,

    Instant expiresAt,

    TrainLine line,

    String destinationId,

    String nextStationId,

    String carNumber,

    @Nullable
    String runNumber
) {
    public SmokingReportResponse {
        Objects.requireNonNull(date);
        Objects.requireNonNull(reportId);
        Objects.requireNonNull(reportedAt);
        Objects.requireNonNull(expiresAt);
        Objects.requireNonNull(line);
        Objects.requireNonNull(destinationId);
        Objects.requireNonNull(nextStationId);
        Objects.requireNonNull(carNumber);
    }

    public static SmokingReportResponse from(SmokingReport report) {
        Objects.requireNonNull(report);

        return new SmokingReportResponse(
            report.getDate(),
            report.getReportId(),
            report.getReportedAt(),
            Instant.ofEpochSecond(report.getExpiresAt()),
            report.getLine(),
            report.getDestinationId(),
            report.getNextStationId(),
            report.getCarNumber(),
            report.getRunNumber()
        );
    }
}
