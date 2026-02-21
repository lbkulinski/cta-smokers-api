package com.ctasmokers.smoking.dto;

import com.ctasmokers.smoking.model.SmokingReport;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public record FindReportsResponse(
    List<SmokingReport> reports,

    @Nullable
    String lastReportId
) {
}
