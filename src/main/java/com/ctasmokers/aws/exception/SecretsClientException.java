package com.ctasmokers.aws.exception;

public final class SecretsClientException extends RuntimeException {
    public SecretsClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
