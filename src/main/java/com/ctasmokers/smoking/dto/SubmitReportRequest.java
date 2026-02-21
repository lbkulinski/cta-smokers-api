package com.ctasmokers.smoking.dto;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@NullMarked
public record SubmitReportRequest(
    @NotBlank
    String line,

    @NotBlank
    String destination,

    @NotBlank
    String nextStop,

    @NotBlank
    String carNumber,

    @Nullable
    String runNumber
) {
    public SubmitReportRequest {
        Objects.requireNonNull(line);
        Objects.requireNonNull(destination);
        Objects.requireNonNull(nextStop);
        Objects.requireNonNull(carNumber);
    }
}
