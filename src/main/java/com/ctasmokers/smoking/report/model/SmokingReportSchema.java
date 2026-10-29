package com.ctasmokers.smoking.report.model;

import com.cta4j.train.common.model.TrainLine;
import org.jspecify.annotations.NullMarked;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@NullMarked
public final class SmokingReportSchema {
    public static final String CAR_NUMBER_LINE_ATTRIBUTE = "carNumberLine";
    public static final String GSI_PK_ATTRIBUTE = "gsiPk";
    public static final String EXPIRES_AT_ATTRIBUTE = "expiresAt";

    public static final String GSI_PK_EXPIRES_AT_INDEX = "gsiPk-expiresAt-index";

    public static final String GSI_PK_VALUE = "REPORT";

    private static final String DELIMITER = "#";

    private static final String CAR_NUMBER_STRING = "[0-9]{4}";
    private static final String LINE_STRING = Arrays.stream(TrainLine.values())
                                                    .map(Enum::name)
                                                    .collect(Collectors.joining("|"));

    public static final String CAR_NUMBER_REGEX = "^" + CAR_NUMBER_STRING + "$";
    private static final String CAR_NUMBER_LINE_REGEX = "^%s%s(%s)$".formatted(
        CAR_NUMBER_STRING,
        DELIMITER,
        LINE_STRING);

    private static final Pattern CAR_NUMBER_PATTERN = Pattern.compile(CAR_NUMBER_REGEX);
    private static final Pattern CAR_NUMBER_LINE_PATTERN = Pattern.compile(CAR_NUMBER_LINE_REGEX);

    private SmokingReportSchema() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String carNumberLine(String carNumber, TrainLine trainLine) {
        Objects.requireNonNull(carNumber);
        Objects.requireNonNull(trainLine);

        if (!CAR_NUMBER_PATTERN.matcher(carNumber).matches()) {
            throw new IllegalArgumentException("carNumber must be a numeric string with exactly 4 digits");
        }

        return "%s%s%s".formatted(carNumber, DELIMITER, trainLine.name());
    }

    public static boolean isValidCarNumberLine(String carNumberLine) {
        Objects.requireNonNull(carNumberLine);

        return CAR_NUMBER_LINE_PATTERN.matcher(carNumberLine).matches();
    }
}
