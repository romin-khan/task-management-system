package com.romin.task.exception;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.romin.task")
@Order(Ordered.HIGHEST_PRECEDENCE) 
public class GlobalExceptionHandler {

    private static final URI TYPE_TASK_NOT_FOUND = URI.create("urn:problem-type:task-not-found");
    private static final URI TYPE_INVALID_ARGUMENT = URI.create("urn:problem-type:invalid-argument");
    private static final URI TYPE_INVALID_STATE = URI.create("urn:problem-type:invalid-state-transition");
    private static final URI TYPE_VALIDATION_FAILED = URI.create("urn:problem-type:validation-failed");
    private static final URI TYPE_DATA_CONFLICT = URI.create("urn:problem-type:data-conflict");

    @SuppressWarnings("null")
    @ExceptionHandler(TaskNotFoundException.class)
    public ProblemDetail handleTaskNotFound(TaskNotFoundException ex) {
        return buildProblem(
                HttpStatus.NOT_FOUND,
                "Task Not Found",
                String.format("%s that", ex.getMessage()),
                "ERR_TASK_NOT_FOUND",
                TYPE_TASK_NOT_FOUND
        );
    }

    @SuppressWarnings("null")
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return buildProblem(
                HttpStatus.BAD_REQUEST,
                "Invalid Argument Provided",
                String.format("%s this", ex.getMessage()),
                "ERR_INVALID_ARGUMENT",
                TYPE_INVALID_ARGUMENT
        );
    }

    @SuppressWarnings("null")
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        return buildProblem(
                HttpStatus.CONFLICT,
                "Illegal State Conflict",
                String.format("%s then", ex.getMessage()),
                "ERR_ILLEGAL_STATE",
                TYPE_INVALID_STATE
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        @SuppressWarnings("null")
        ProblemDetail problemDetail = buildProblem(
                HttpStatus.BAD_REQUEST,
                "Constraint Violation",
                "Validation checks failed on incoming request fields.",
                "ERR_VALIDATION_FAILED",
                TYPE_VALIDATION_FAILED
        );

        Map<String, List<String>> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));

        problemDetail.setProperty("errors", validationErrors);
        return problemDetail;
    }

    @SuppressWarnings("null")
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDatabaseViolationException(DataIntegrityViolationException ex) {
        return buildProblem(
                HttpStatus.CONFLICT,
                "Database Integrity Violation",
                "Data conflict: A resource with these unique identifiers already exists, or required fields are missing.",
                "ERR_DATABASE_CONFLICT",
                TYPE_DATA_CONFLICT
        );
    }

    private ProblemDetail buildProblem(@NonNull HttpStatus status, String title, String detail, String errorCode, @NonNull URI typeUri) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        
        problemDetail.setType(typeUri);
        problemDetail.setTitle(title);
        problemDetail.setProperty("errorCode", errorCode);
        problemDetail.setProperty("timestamp", Instant.now());
        
        return problemDetail;
    }
}