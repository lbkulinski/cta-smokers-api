package com.ctasmokers.smoking.report.exception;

import com.ctasmokers.smoking.common.model.TrainLine;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SmokingReportAlreadyExistsException extends RuntimeException {
    public SmokingReportAlreadyExistsException(String carNumber, TrainLine line) {
        super("Smoking report already exists for car number %s on line %s".formatted(carNumber, line));
    }
}
