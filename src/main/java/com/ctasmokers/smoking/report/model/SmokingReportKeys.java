package com.ctasmokers.smoking.report.model;

import com.cta4j.train.common.model.TrainLine;

public final class SmokingReportKeys {
    public static final String GSI_PK = "REPORT";

    private static final String DELIMITER = "#";

    private SmokingReportKeys() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String carNumberLine(String carNumber, TrainLine trainLine) {
        return "%s%s%s".formatted(carNumber, DELIMITER, trainLine.name());
    }
}
