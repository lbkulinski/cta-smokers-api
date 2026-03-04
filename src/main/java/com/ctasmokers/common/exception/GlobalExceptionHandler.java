package com.ctasmokers.common.exception;

import com.ctasmokers.smoking.report.exception.SmokingReportNotFoundException;
import com.rollbar.notifier.Rollbar;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@RestControllerAdvice
public final class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String NOT_FOUND_DETAIL = "The requested resource was not found.";
    private static final String NOT_FOUND_TITLE = "Not Found";

    private static final String INTERNAL_ERROR_DETAIL = "An unexpected error occurred.";
    private static final String INTERNAL_ERROR_TITLE = "Internal Server Error";

    private final Rollbar rollbar;

    @Autowired
    public GlobalExceptionHandler(Rollbar rollbar) {
        this.rollbar = rollbar;
    }

    @ExceptionHandler(SmokingReportNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFoundException(HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, NOT_FOUND_DETAIL);

        URI instance = URI.create(request.getRequestURI());

        problem.setTitle(NOT_FOUND_TITLE);
        problem.setInstance(instance);

        return ResponseEntity.of(problem)
                             .build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException(
        Exception exception,
        HttpServletRequest request
    ) {
        String message = "Unexpected error for %s".formatted(request.getRequestURI());

        log.error(message, exception);
        this.rollbar.error(exception, message);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            INTERNAL_ERROR_DETAIL
        );

        URI instance = URI.create(request.getRequestURI());

        problem.setTitle(INTERNAL_ERROR_TITLE);
        problem.setInstance(instance);

        return ResponseEntity.of(problem)
                             .build();
    }
}
