package com.ctasmokers.smoking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.jspecify.annotations.Nullable;

@Schema(description = "Request body for submitting a new smoking report")
public record SubmitReportRequest(
    @NotNull
    @Pattern(regexp = LINE_REGEX, message = LINE_MESSAGE)
    @Schema(description = "CTA train line", example = "RED")
    String line,

    @NotNull
    @Pattern(regexp = ID_REGEX, message = ID_MESSAGE)
    @Schema(description = "Destination station ID", example = "40900")
    String destinationId,

    @NotNull
    @Pattern(regexp = ID_REGEX, message = ID_MESSAGE)
    @Schema(description = "Next station ID", example = "41220")
    String nextStationId,

    @NotNull
    @Pattern(regexp = ID_REGEX, message = ID_MESSAGE)
    @Schema(description = "Car number", example = "2435")
    String carNumber,

    @Nullable
    @Pattern(regexp = ID_REGEX, message = ID_MESSAGE)
    @Schema(description = "Run number (optional)", example = "902")
    String runNumber
) {
    private static final String LINE_REGEX = "^(RED|BLUE|GREEN|BROWN|PURPLE|PINK|ORANGE|YELLOW)$";
    private static final String LINE_MESSAGE = """
    Must be one of: RED, BLUE, GREEN, BROWN, PURPLE, PINK, ORANGE, YELLOW""";

    private static final String ID_REGEX = "^[0-9]{1,10}$";
    private static final String ID_MESSAGE = "Must be a number with 1 to 10 digits";
}
