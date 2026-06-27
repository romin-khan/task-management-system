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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice(basePackages = "com.romin.task")
@Order(Ordered.HIGHEST_PRECEDENCE) 
public class GlobalExceptionHandler {

    private static final URI TYPE_TASK_NOT_FOUND = URI.create("urn:problem-type:task-not-found");
    private static final URI TYPE_INVALID_ARGUMENT = URI.create("urn:problem-type:invalid-argument");
    private static final URI TYPE_INVALID_STATE = URI.create("urn:problem-type:invalid-state-transition");
    private static final URI TYPE_VALIDATION_FAILED = URI.create("urn:problem-type:validation-failed");
    private static final URI TYPE_DATA_CONFLICT = URI.create("urn:problem-type:data-conflict");
    private static final URI TYPE_CONCURRENCY_CONFLICT = URI.create("urn:problem-type:concurrency-conflict");

    @SuppressWarnings("null")
    @ExceptionHandler(TaskNotFoundException.class)
    public ProblemDetail handleTaskNotFound(TaskNotFoundException ex) {
        log.warn("[EXCEPTION] Task lookup failed at API boundary. Message: {}", ex.getMessage());
        return buildProblem(
                HttpStatus.NOT_FOUND,
                "Task Not Found",
                String.format("%s ", ex.getMessage()),
                "ERR_TASK_NOT_FOUND",
                TYPE_TASK_NOT_FOUND
        );
    }

    @SuppressWarnings("null")
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("[EXCEPTION] Invalid argument intercepted at API boundary. Message: {}", ex.getMessage());
        return buildProblem(
                HttpStatus.BAD_REQUEST,
                "Invalid Argument Provided",
                String.format("%s ", ex.getMessage()),
                "ERR_INVALID_ARGUMENT",
                TYPE_INVALID_ARGUMENT
        );
    }

    @SuppressWarnings("null")
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        log.warn("[EXCEPTION] Business state invariant breach intercepted. Message: {}", ex.getMessage());
        return buildProblem(
                HttpStatus.CONFLICT,
                "Illegal State Conflict",
                String.format("%s ", ex.getMessage()),
                "ERR_ILLEGAL_STATE",
                TYPE_INVALID_STATE
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("[EXCEPTION] Payload binding constraint check failed. Total Field Faults: {}", 
                 ex.getBindingResult().getFieldErrorCount());

        @SuppressWarnings("null")
        ProblemDetail problemDetail = buildProblem(
                HttpStatus.BAD_REQUEST,
                "Constraint Violation",
                "Validation checks failed on incoming request fields.",
                "ERR_VALIDATION_FAILED",
                TYPE_VALIDATION_FAILED
        );

        @SuppressWarnings("null")
        Map<String, List<String>> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                         FieldError::getField,
                         Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));

        log.debug("[EXCEPTION] Detailed constraint violations mapped for payload fields: {}", validationErrors.keySet());
        problemDetail.setProperty("errors", validationErrors);
        return problemDetail;
    }

    @SuppressWarnings("null")
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDatabaseViolationException(DataIntegrityViolationException ex) {
        log.error("[DATABASE ERROR] Low-level data integrity constraint breached. Cause: {}", ex.getMostSpecificCause().getMessage());
        return buildProblem(
                HttpStatus.CONFLICT,
                "Database Integrity Violation",
                "Data conflict: A resource with these unique identifiers already exists, or required fields are missing.",
                "ERR_DATABASE_CONFLICT",
                TYPE_DATA_CONFLICT
        );
    }

    @SuppressWarnings("null")
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex) {
        log.warn("[EXCEPTION] Concurrency race condition intercepted. Resource entity update conflict. Type: {}", 
                 ex.getPersistentClassName());
                 
        return buildProblem(
                HttpStatus.CONFLICT, // 409 Conflict is the precise HTTP industry status code for locking failures
                "Data Concurrency Conflict",
                "The task resource you are trying to modify was updated by another session or user background execution block while you were viewing it. Please reload or re-fetch your client state payload and resubmit.",
                "ERR_CONCURRENCY_CONFLICT",
                TYPE_CONCURRENCY_CONFLICT
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
