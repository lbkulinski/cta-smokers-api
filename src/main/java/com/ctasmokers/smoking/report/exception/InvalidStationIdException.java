package com.ctasmokers.smoking.report.exception;

public final class InvalidStationIdException extends RuntimeException {
    public InvalidStationIdException(String field) {
        super("The specified %s is invalid.".formatted(field));
    }
}
