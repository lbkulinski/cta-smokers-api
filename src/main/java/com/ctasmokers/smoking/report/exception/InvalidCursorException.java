package com.ctasmokers.smoking.report.exception;

public final class InvalidCursorException extends RuntimeException {
    public InvalidCursorException(String cursor) {
        super("%s is not a valid cursor".formatted(cursor));
    }
}
