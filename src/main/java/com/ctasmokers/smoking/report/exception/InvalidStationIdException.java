package com.ctasmokers.smoking.report.exception;

public final class InvalidStationIdException extends RuntimeException {
    public InvalidStationIdException(String field, String value) {
        super("%s is not a valid %s".formatted(value, field));
    }
}
