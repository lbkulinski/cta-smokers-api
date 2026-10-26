package com.ctasmokers.smoking.report.model;

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SmokingReportLimits {
    public static final int DEFAULT_LIMIT = 25;
    public static final int MAX_LIMIT = 100;

    private SmokingReportLimits() {
        throw new UnsupportedOperationException("Utility class");
    }
}
