package com.ctasmokers.smoking.report.model;

import com.cta4j.train.common.model.TrainLine;

public final class SmokingReportKeys {
    private static final String DELIMITER = "#";

    private SmokingReportKeys() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String carNumberLine(String carNumber, TrainLine trainLine) {
        return "%s%s%s".formatted(carNumber, DELIMITER, trainLine.name());
    }

    public record CarNumberLineKey(String carNumber, TrainLine trainLine) {
        public String toKey() {
            return carNumberLine(this.carNumber, this.trainLine);
        }
    }

    public static CarNumberLineKey parseCarNumberLine(String carNumberLine) {
        String[] parts = carNumberLine.split(DELIMITER);

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid carNumberLine format: %s".formatted(carNumberLine));
        }

        String carNumber = parts[0];
        var trainLine = TrainLine.valueOf(parts[1]);

        return new CarNumberLineKey(carNumber, trainLine);
    }
}
