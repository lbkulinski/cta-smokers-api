package com.ctasmokers.smoking.report.model;

import com.cta4j.train.common.model.TrainLine;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SmokingReportSchema {
    public static final String CAR_NUMBER_LINE_ATTRIBUTE = "carNumberLine";
    public static final String GSI_PK_ATTRIBUTE = "gsiPk";
    public static final String EXPIRES_AT_ATTRIBUTE = "expiresAt";

    public static final String GSI_PK_EXPIRES_AT_INDEX = "gsiPk-expiresAt-index";

    public static final String GSI_PK_VALUE = "REPORT";

    private static final String DELIMITER = "#";

    private SmokingReportSchema() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String carNumberLine(String carNumber, TrainLine trainLine) {
        return "%s%s%s".formatted(carNumber, DELIMITER, trainLine.name());
    }
}
