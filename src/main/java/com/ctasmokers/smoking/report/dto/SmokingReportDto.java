package com.ctasmokers.smoking.report.dto;

import com.ctasmokers.smoking.report.model.SmokingReport;
import com.ctasmokers.smoking.common.model.TrainLine;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Schema(description = "Response body for a smoking report")
@NullMarked
public record SmokingReportDto(
    @Schema(
        description = "Date of the report",
        example = "2026-02-21",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    LocalDate date,

    @Schema(
        description = "Unique identifier for the report",
        example = "1771710202399_7a8dcb0d-aea9-4c37-89d6-fc79fe3ba77f",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String reportId,

    @Schema(
        description = "Timestamp when the report was submitted",
        example = "2026-02-21T21:00:22.399660Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Instant reportedAt,

    @Schema(
        description = "Timestamp when the report expires",
        example = "2026-02-21T21:30:22.399660Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Instant expiresAt,

    @Schema(
        description = "Train line where the smoking was reported",
        example = "RED",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    TrainLine line,

    @Schema(
        description = "Destination station ID",
        example = "40900",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String destinationId,

    @Schema(
        description = "Next station ID",
        example = "41220",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String nextStationId,

    @Schema(
        description = "Car number where the smoking was reported",
        example = "2435",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String carNumber,

    @Schema(description = "Run number of the train", example = "902", nullable = true)
    @Nullable
    String runNumber
) {
    public SmokingReportDto {
        Objects.requireNonNull(date);
        Objects.requireNonNull(reportId);
        Objects.requireNonNull(reportedAt);
        Objects.requireNonNull(expiresAt);
        Objects.requireNonNull(line);
        Objects.requireNonNull(destinationId);
        Objects.requireNonNull(nextStationId);
        Objects.requireNonNull(carNumber);
    }

    public static SmokingReportDto from(SmokingReport report) {
        Objects.requireNonNull(report);

        return new SmokingReportDto(
            report.date(),
            report.reportId(),
            report.reportedAt(),
            Instant.ofEpochSecond(report.expiresAt()),
            report.line(),
            report.destinationId(),
            report.nextStationId(),
            report.carNumber(),
            report.runNumber()
        );
    }
}
