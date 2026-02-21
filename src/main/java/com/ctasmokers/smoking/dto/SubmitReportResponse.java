package com.ctasmokers.smoking.dto;

import com.ctasmokers.smoking.model.SmokingReport;
import com.ctasmokers.smoking.model.TrainLine;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@NullMarked
public record SubmitReportResponse(
    LocalDate date,

    String reportId,

    Instant reportedAt,

    Instant expiresAt,

    TrainLine line,

    String destination,

    String nextStop,

    String carNumber,

    @Nullable
    String runNumber
) {
    public SubmitReportResponse {
        Objects.requireNonNull(date);
        Objects.requireNonNull(reportId);
        Objects.requireNonNull(reportedAt);
        Objects.requireNonNull(expiresAt);
        Objects.requireNonNull(line);
        Objects.requireNonNull(destination);
        Objects.requireNonNull(nextStop);
        Objects.requireNonNull(carNumber);
    }

    public static SubmitReportResponse from(SmokingReport report) {
        Objects.requireNonNull(report);

        return new SubmitReportResponse(
            report.getDate(),
            report.getReportId(),
            report.getReportedAt(),
            Instant.ofEpochSecond(report.getExpiresAt()),
            report.getLine(),
            report.getDestination(),
            report.getNextStop(),
            report.getCarNumber(),
            report.getRunNumber()
        );
    }
}
