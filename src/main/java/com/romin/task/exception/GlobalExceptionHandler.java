package com.romin.task.exception;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.romin.infra.payload.ApiResponse;
import com.romin.infra.payload.ErrorResponse;

@RestControllerAdvice(basePackages = "com.romin.task")
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleTaskNotFound(TaskNotFoundException ex){
        String message = String.format(ex.getMessage()+"that");
        ErrorResponse error = ErrorResponse.builder()
                                           .detail(message)
                                           .build();
        return ResponseEntity
               .status(HttpStatus.NOT_FOUND)
               .body(
                    buildResponse(
                        error,
                        HttpStatus.NOT_FOUND.value()
                    )
               );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalArgument(IllegalArgumentException ex){
        String message = String.format(ex.getMessage()+"this");
        ErrorResponse error = ErrorResponse.builder()
                                           .detail(message)
                                           .build();
        return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                        buildResponse(
                            error,
                            HttpStatus.BAD_REQUEST.value()
                        )
                    );                                        
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalState(IllegalStateException ex){
        String message = String.format(ex.getMessage()+"then");
        ErrorResponse error = ErrorResponse.builder()
                                           .detail(message)
                                           .build();
        return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                        buildResponse(
                            error,
                            HttpStatus.CONFLICT.value()
                        )
                    );                                        
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidationException(MethodArgumentNotValidException ex){
        Map<String, List<String>> errors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> {
                    String field = error.getField();
                    String message = error.getDefaultMessage();
                    errors.computeIfAbsent(
                                        field,
                                        k -> new ArrayList<>()
                                ).add(message);
                });
        ErrorResponse error = ErrorResponse.builder()
                                           .detail("Validation failed")
                                           .errors(errors)
                                           .build();                                                               
        return ResponseEntity
                    .badRequest()
                    .body(
                        buildResponse(
                            error,
                            HttpStatus.BAD_REQUEST.value()
                        )
                    );        
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleDataBaseVoilationException(DataIntegrityViolationException ex){
        ErrorResponse error = ErrorResponse.builder()
                                           .detail("Data conflict: A resource with these unique identifiers already exists, or required fields are missing.")
                                           .build();
        return ResponseEntity
                     .status(HttpStatus.CONFLICT)
                     .body(
                        buildResponse(
                            error,
                            HttpStatus.CONFLICT.value()
                        )
                     );
    }

    private ApiResponse<ErrorResponse> buildResponse(ErrorResponse error, int status){
        return ApiResponse.<ErrorResponse>builder()
                          .data(error)
                          .message("Error occurred")
                          .timeStamp(LocalDateTime.now())
                          .status(status)
                          .build();
    }
}
