package com.ctasmokers.common.exception;

import com.ctasmokers.smoking.exception.SmokingReportNotFoundException;
import com.rollbar.notifier.Rollbar;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public final class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String NOT_FOUND_DETAIL = "The requested resource was not found.";
    private static final String NOT_FOUND_TITLE = "Not Found";

    private static final String TYPE_MISMATCH_DETAIL_TEMPLATE = "Invalid value '%s' for parameter '%s'";
    private static final String TYPE_MISMATCH_TITLE = "Bad Request";

    private static final String VALIDATION_ERROR_DETAIL = "One or more validation errors occurred.";
    private static final String VALIDATION_ERROR_TITLE = "Validation Error";
    private static final String VALIDATION_ERRORS_PROPERTY = "errors";

    private static final String INTERNAL_ERROR_DETAIL = "An unexpected error occurred.";
    private static final String INTERNAL_ERROR_TITLE = "Internal Server Error";

    private final Rollbar rollbar;

    @Autowired
    public GlobalExceptionHandler(Rollbar rollbar) {
        this.rollbar = rollbar;
    }

    @ExceptionHandler({
        NoResourceFoundException.class,
        NoHandlerFoundException.class,
        SmokingReportNotFoundException.class
    })
    public ResponseEntity<ProblemDetail> handleNotFoundException(HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, NOT_FOUND_DETAIL);

        URI instance = URI.create(request.getRequestURI());

        problem.setTitle(NOT_FOUND_TITLE);
        problem.setInstance(instance);

        return ResponseEntity.of(problem)
                             .build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatchException(
        MethodArgumentTypeMismatchException exception,
        HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            TYPE_MISMATCH_DETAIL_TEMPLATE.formatted(
                exception.getValue(),
                exception.getName()
            )
        );

        URI instance = URI.create(request.getRequestURI());

        problem.setTitle(TYPE_MISMATCH_TITLE);
        problem.setInstance(instance);

        return ResponseEntity.of(problem)
                             .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, VALIDATION_ERROR_DETAIL);

        URI instance = URI.create(request.getRequestURI());

        problem.setTitle(VALIDATION_ERROR_TITLE);
        problem.setInstance(instance);

        Map<String, List<String>> fieldErrors = exception.getBindingResult()
                                                         .getFieldErrors()
                                                         .stream()
                                                         .collect(
                                                             Collectors.groupingBy(
                                                                 FieldError::getField,
                                                                 Collectors.mapping(
                                                                     FieldError::getDefaultMessage,
                                                                     Collectors.toList()
                                                                 )
                                                             )
                                                         );

        problem.setProperty(VALIDATION_ERRORS_PROPERTY, fieldErrors);

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
