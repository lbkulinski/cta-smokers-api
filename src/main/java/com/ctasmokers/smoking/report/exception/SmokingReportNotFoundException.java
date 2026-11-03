package com.ctasmokers.smoking.report.exception;

import com.cta4j.train.common.model.TrainLine;

public final class SmokingReportNotFoundException extends RuntimeException {
    public SmokingReportNotFoundException(String carNumber, TrainLine line) {
        super("Smoking report not found for car number %s and line %s".formatted(carNumber, line.name()));
    }
}
