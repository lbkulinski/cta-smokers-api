package com.ctasmokers.common.exception;

import com.ctasmokers.smoking.aggregate.exception.SmokingReportAggregateNotFoundException;
import com.ctasmokers.smoking.report.exception.InvalidStationIdException;
import com.ctasmokers.smoking.report.exception.SmokingReportAlreadyExistsException;
import com.ctasmokers.smoking.report.exception.SmokingReportNotFoundException;
import com.rollbar.notifier.Rollbar;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@RestControllerAdvice
@Slf4j
@NullMarked
public final class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String BAD_REQUEST_DETAIL = "The request was invalid.";
    private static final String BAD_REQUEST_TITLE = "Bad Request";

    private static final String NOT_FOUND_DETAIL = "The requested resource was not found.";
    private static final String NOT_FOUND_TITLE = "Not Found";

    private static final String CONFLICT_DETAIL = "The requested resource already exists.";
    private static final String CONFLICT_TITLE = "Conflict";

    private static final String INTERNAL_ERROR_DETAIL = "An unexpected error occurred.";
    private static final String INTERNAL_ERROR_TITLE = "Internal Server Error";

    private final Rollbar rollbar;

    @Autowired
    public GlobalExceptionHandler(Rollbar rollbar) {
        this.rollbar = rollbar;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(HttpServletRequest request) {
        ProblemDetail problem = buildProblemDetail(
            HttpStatus.BAD_REQUEST,
            BAD_REQUEST_DETAIL,
            BAD_REQUEST_TITLE,
            request);

        return ResponseEntity.of(problem)
                             .build();
    }

    @ExceptionHandler(InvalidStationIdException.class)
    public ResponseEntity<ProblemDetail> handleInvalidStationIdException(
        InvalidStationIdException exception,
        HttpServletRequest request
    ) {
        String detail = exception.getMessage();

        ProblemDetail problem = buildProblemDetail(HttpStatus.BAD_REQUEST, detail, BAD_REQUEST_TITLE, request);

        return ResponseEntity.of(problem)
                             .build();
    }

    @ExceptionHandler(SmokingReportNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFoundException(HttpServletRequest request) {
        ProblemDetail problem = buildProblemDetail(HttpStatus.NOT_FOUND, NOT_FOUND_DETAIL, NOT_FOUND_TITLE, request);

        return ResponseEntity.of(problem)
                             .build();
    }

    @ExceptionHandler(SmokingReportAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleAlreadyExistsException(
        SmokingReportAlreadyExistsException exception,
        HttpServletRequest request
    ) {
        String message = exception.getMessage();

        log.warn(message);
        this.rollbar.warning(message);

        ProblemDetail problem = buildProblemDetail(HttpStatus.CONFLICT, CONFLICT_DETAIL, CONFLICT_TITLE, request);

        return ResponseEntity.of(problem)
                             .build();
    }

    @ExceptionHandler(SmokingReportAggregateNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleAggregateNotFoundException(HttpServletRequest request) {
        ProblemDetail problem = buildProblemDetail(HttpStatus.NOT_FOUND, NOT_FOUND_DETAIL, NOT_FOUND_TITLE, request);

        return ResponseEntity.of(problem)
                             .build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException(
        Exception exception,
        HttpServletRequest request
    ) {
        var message = "Unexpected error for %s".formatted(request.getRequestURI());

        log.error(message, exception);
        this.rollbar.error(exception, message);

        ProblemDetail problem = buildProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            INTERNAL_ERROR_DETAIL,
            INTERNAL_ERROR_TITLE,
            request);

        return ResponseEntity.of(problem)
                             .build();
    }

    private static ProblemDetail buildProblemDetail(
        HttpStatus status,
        String detail,
        String title,
        HttpServletRequest request
    ) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);

        var instance = URI.create(request.getRequestURI());

        problem.setTitle(title);
        problem.setInstance(instance);

        return problem;
    }
}
