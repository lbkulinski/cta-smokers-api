package com.ctasmokers.smoking.dto;

import jakarta.validation.constraints.Pattern;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record SubmitReportRequest(
    @Pattern(regexp = LINE_REGEX)
    String line,

    @Pattern(regexp = ID_REGEX)
    String destinationId,

    @Pattern(regexp = ID_REGEX)
    String nextStationId,

    @Pattern(regexp = ID_REGEX)
    String carNumber,

    @Nullable
    @Pattern(regexp = ID_REGEX)
    String runNumber
) {
    private static final String LINE_REGEX = "RED|BLUE|GREEN|BROWN|PURPLE|PINK|ORANGE|YELLOW";
    private static final String ID_REGEX = "[0-9]{1,10}";
}
