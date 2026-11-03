package com.ctasmokers.smoking.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@Schema(description = "Response body for a page of active smoking reports")
@NullMarked
public record SmokingReportPageDto(
    @Schema(
        description = "Active smoking reports on this page, newest first",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    List<SmokingReportDto> reports,

    @Schema(
        description = "Opaque cursor for the next page, or null on the last page. May be set before a final empty page",
        example = "c2FtcGxlLWN1cnNvcg",
        nullable = true
    )
    @Nullable
    String nextCursor
) {
    public SmokingReportPageDto {
        Objects.requireNonNull(reports);

        reports = List.copyOf(reports);
    }
}
