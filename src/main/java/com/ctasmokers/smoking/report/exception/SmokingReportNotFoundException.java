package com.ctasmokers.smoking.report.exception;

import java.time.LocalDate;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SmokingReportNotFoundException extends RuntimeException {
    public SmokingReportNotFoundException(LocalDate date, String reportId) {
        super("Smoking report not found for date %s and report ID %s".formatted(date, reportId));
    }
}
