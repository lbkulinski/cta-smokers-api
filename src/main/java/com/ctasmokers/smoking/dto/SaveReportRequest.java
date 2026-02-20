package com.ctasmokers.smoking.dto;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@NullMarked
public record SaveReportRequest(
    String line,

    String destination,

    String nextStop,

    String carNumber,

    @Nullable
    String runNumber
) {
    public SaveReportRequest {
        Objects.requireNonNull(line);
        Objects.requireNonNull(destination);
        Objects.requireNonNull(nextStop);
        Objects.requireNonNull(carNumber);
    }
}
