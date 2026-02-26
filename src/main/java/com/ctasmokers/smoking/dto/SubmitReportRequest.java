package com.ctasmokers.smoking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.jspecify.annotations.Nullable;

public record SubmitReportRequest(
    @NotNull
    @Pattern(regexp = LINE_REGEX, message = LINE_MESSAGE)
    String line,

    @NotNull
    @Pattern(regexp = ID_REGEX, message = ID_MESSAGE)
    String destinationId,

    @NotNull
    @Pattern(regexp = ID_REGEX, message = ID_MESSAGE)
    String nextStationId,

    @NotNull
    @Pattern(regexp = ID_REGEX, message = ID_MESSAGE)
    String carNumber,

    @Nullable
    @Pattern(regexp = ID_REGEX, message = ID_MESSAGE)
    String runNumber
) {
    private static final String LINE_REGEX = "^(RED|BLUE|GREEN|BROWN|PURPLE|PINK|ORANGE|YELLOW)$";
    private static final String LINE_MESSAGE = """
    Must be one of: RED, BLUE, GREEN, BROWN, PURPLE, PINK, ORANGE, YELLOW""";

    private static final String ID_REGEX = "^[0-9]{1,10}$";
    private static final String ID_MESSAGE = "Must be a number with 1 to 10 digits";
}
