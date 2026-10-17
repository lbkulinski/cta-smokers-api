package com.ctasmokers.smoking.report.dto;

import com.cta4j.train.common.model.TrainLine;
import com.ctasmokers.smoking.report.model.SmokingReport;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;

@Schema(description = "Response body for a smoking report")
@NullMarked
public record SmokingReportDto(
    @Schema(
        description = "Car number where the smoking was reported",
        example = "2435",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String carNumber,

    @Schema(
        description = "Train line where the smoking was reported",
        example = "RED",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    TrainLine line,

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
        description = "Run number of the train, or null if not provided",
        example = "902",
        nullable = true
    )
    @Nullable
    String runNumber
) {
    public SmokingReportDto {
        Objects.requireNonNull(carNumber);
        Objects.requireNonNull(line);
        Objects.requireNonNull(reportedAt);
        Objects.requireNonNull(expiresAt);
        Objects.requireNonNull(destinationId);
        Objects.requireNonNull(nextStationId);
    }

    public static SmokingReportDto from(SmokingReport report) {
        Objects.requireNonNull(report);

        return new SmokingReportDto(
            report.carNumber(),
            report.line(),
            report.reportedAt(),
            Instant.ofEpochSecond(report.expiresAt()),
            report.destinationId(),
            report.nextStationId(),
            report.runNumber());
    }
}
