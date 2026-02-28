package com.ctasmokers.smoking.dto;

import com.ctasmokers.smoking.model.SmokingReport;
import com.ctasmokers.smoking.model.TrainLine;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@NullMarked
@Schema(description = "Response body for a smoking report")
public record SmokingReportResponse(
    @Schema(description = "Date of the report", example = "2026-02-21")
    LocalDate date,

    @Schema(
        description = "Unique identifier for the report",
        example = "1771710202399_7a8dcb0d-aea9-4c37-89d6-fc79fe3ba77f"
    )
    String reportId,

    @Schema(description = "Timestamp when the report was submitted", example = "2026-02-21T21:00:22.399660Z")
    Instant reportedAt,

    @Schema(description = "Timestamp when the report expires", example = "2026-02-21T21:30:22.399660Z")
    Instant expiresAt,

    @Schema(description = "Train line where the smoking was reported", example = "RED")
    TrainLine line,

    @Schema(description = "Destination station ID", example = "40900")
    String destinationId,

    @Schema(description = "Next station ID", example = "41220")
    String nextStationId,

    @Schema(description = "Car number where the smoking was reported", example = "2435")
    String carNumber,

    @Nullable
    @Schema(description = "Run number of the train", example = "902")
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
