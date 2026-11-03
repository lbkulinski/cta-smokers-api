package com.ctasmokers.smoking.report.exception;

import com.cta4j.train.common.model.TrainLine;

public final class SmokingReportAlreadyExistsException extends RuntimeException {
    public SmokingReportAlreadyExistsException(String carNumber, TrainLine line) {
        super("Smoking report already exists for car number %s on line %s".formatted(carNumber, line));
    }
}
