package com.romin.task.exception;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@SuppressWarnings("null")
@RestControllerAdvice(basePackages = "com.romin.task")
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleTaskNotFound(TaskNotFoundException ex) {
        String detailMessage = String.format("%s that", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, detailMessage);
        problemDetail.setTitle("Task Not Found");
        
        problemDetail.setType(URI.create("errors/task-not-found"));
        
        addCommonMetadata(problemDetail, "ERR_TASK_NOT_FOUND");
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        String detailMessage = String.format("%s this", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detailMessage);
        problemDetail.setTitle("Invalid Argument Provided");
        
        problemDetail.setType(URI.create("errors/invalid-argument"));
        
        addCommonMetadata(problemDetail, "ERR_INVALID_ARGUMENT");
        
        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex) {
        String detailMessage = String.format("%s then", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detailMessage);
        problemDetail.setTitle("Illegal State Conflict");
        
        problemDetail.setType(URI.create("errors/invalid-state-transition"));
        
        addCommonMetadata(problemDetail, "ERR_ILLEGAL_STATE");
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, 
                "Validation checks failed on incoming request fields."
        );
        problemDetail.setTitle("Constraint Violation");
        
        problemDetail.setType(URI.create("errors/validation-failed"));

        Map<String, List<String>> validationErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(
                Collectors.groupingBy(
                    error -> error.getField(),
                    Collectors.mapping(
                        FieldError::getDefaultMessage,
                        Collectors.toList()
                    )
                )
            )

        problemDetail.setProperty("errors", validationErrors);
        addCommonMetadata(problemDetail, "ERR_VALIDATION_FAILED");
        
        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDatabaseViolationException(DataIntegrityViolationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Data conflict: A resource with these unique identifiers already exists, or required fields are missing."
        );
        problemDetail.setTitle("Database Integrity Violation");
        
        problemDetail.setType(URI.create("errors/data-conflict"));
        
        addCommonMetadata(problemDetail, "ERR_DATABASE_CONFLICT");
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    private void addCommonMetadata(ProblemDetail problemDetail, String errorCode) {
        problemDetail.setProperty("errorCode", errorCode);
        problemDetail.setProperty("timestamp", Instant.now());
    }
}
