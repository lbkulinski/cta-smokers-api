package com.ctasmokers.smoking.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Schema(description = "Request body for submitting a new smoking report")
@NullMarked
public record SubmitReportDto(
    @NotNull
    @Pattern(regexp = LINE_REGEX, message = LINE_MESSAGE)
    @Schema(description = "CTA train line", example = "RED")
    String line,

    @NotNull
    @Pattern(regexp = ID_REGEX, message = ID_MESSAGE)
    @Schema(description = "Destination station ID (1-10 digit numeric string)", example = "40900")
    String destinationId,

    @NotNull
    @Pattern(regexp = ID_REGEX, message = ID_MESSAGE)
    @Schema(description = "Next station ID (1-10 digit numeric string)", example = "41220")
    String nextStationId,

    @NotNull
    @Pattern(regexp = CAR_NUMBER_REGEX, message = CAR_NUMBER_MESSAGE)
    @Schema(description = "Car number where the smoking was reported (exactly 4 digits)", example = "2435")
    String carNumber,

    @Pattern(regexp = RUN_NUMBER_REGEX, message = RUN_NUMBER_MESSAGE)
    @Schema(description = "Run number (optional, exactly 3 digits)", example = "902", nullable = true)
    @Nullable
    String runNumber
) {
    private static final String LINE_REGEX = "^(RED|BLUE|GREEN|BROWN|PURPLE|PINK|ORANGE|YELLOW)$";
    private static final String LINE_MESSAGE = """
    Must be one of: RED, BLUE, GREEN, BROWN, PURPLE, PINK, ORANGE, YELLOW""";

    private static final String ID_REGEX = "^[0-9]{1,10}$";
    private static final String ID_MESSAGE = "Must be a numeric string with 1 to 10 digits";

    private static final String CAR_NUMBER_REGEX = "^[0-9]{4}$";
    private static final String CAR_NUMBER_MESSAGE = "Must be a numeric string with exactly 4 digits";

    private static final String RUN_NUMBER_REGEX = "^[0-9]{3}$";
    private static final String RUN_NUMBER_MESSAGE = "Must be a numeric string with exactly 3 digits";
}
