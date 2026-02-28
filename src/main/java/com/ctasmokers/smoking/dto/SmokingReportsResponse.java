package com.ctasmokers.smoking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@NullMarked
@Schema(description = "Response body for a list of smoking reports")
public record SmokingReportsResponse(
    @Schema(description = "List of smoking reports")
    List<SmokingReportResponse> reports,

    @Nullable
    @Schema(
        description = "Cursor for fetching the next page of results, or null if there are no more results",
        example = "1771710202399_7a8dcb0d-aea9-4c37-89d6-fc79fe3ba77f"
    )
    String nextCursor
) {
    public SmokingReportsResponse {
        Objects.requireNonNull(reports);

        reports.forEach(Objects::requireNonNull);

        reports = List.copyOf(reports);
    }
}
