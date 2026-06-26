package com.romin.infra.exception;

import java.net.URI;
import java.time.Instant;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE+1)
public class CoreGlobalExceptionHandler{

    private static final URI TYPE_BAD_REQUEST = URI.create("urn:problem-type:bad-request");
    private static final URI TYPE_ROUTE_NOT_FOUND = URI.create("urn:problem-type:route-not-found");
    private static final URI TYPE_METHOD_NOT_ALLOWED = URI.create("urn:problem-type:method-not-allowed");
    private static final URI TYPE_UNSUPPORTED_MEDIA = URI.create("urn:problem-type:unsupported-media");
    private static final URI TYPE_INTERNAL_SERVER_ERROR = URI.create("urn:problem-type:internal-server-error");

    @SuppressWarnings("null")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMalformedJson(HttpMessageNotReadableException ex) {
        log.warn("[INFRA EXCEPTION] Incoming request dropped. Malformed JSON syntax structure intercepted.");
        return buildProblem(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON Request",
                "The server could not read your payload. Please ensure your JSON string syntax format is valid.",
                "ERR_API_MALFORMED_JSON",
                TYPE_BAD_REQUEST,
                ex
        );
    }

    @SuppressWarnings("null")
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ProblemDetail handleNotFound(Exception ex) {
        log.warn("[INFRA EXCEPTION] Request routed to a non-existent endpoint path or static resource mapping.");
        return buildProblem(
                HttpStatus.NOT_FOUND, 
                "Route Not Found", 
                "The page or feature endpoint you are trying to reach doesn't exist.", 
                "ERR_API_ROUTE_NOT_FOUND", 
                TYPE_ROUTE_NOT_FOUND, 
                ex
        );
    }

    @SuppressWarnings("null")
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        log.warn("[INFRA EXCEPTION] HTTP verb mismatch. Method '{}' not supported for this routing path.", ex.getMethod());
        return buildProblem(
                HttpStatus.METHOD_NOT_ALLOWED, 
                "Action Not Allowed", 
                "The HTTP action verb you performed is not supported by this API endpoint.", 
                "ERR_API_METHOD_NOT_ALLOWED", 
                TYPE_METHOD_NOT_ALLOWED, 
                ex
        );
    }

    @SuppressWarnings("null")
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleUnsupportedMedia(HttpMediaTypeNotSupportedException ex) {
        log.warn("[INFRA EXCEPTION] Content-Type mapping rejected. Client provided unsupported media type: '{}'", ex.getContentType());
        return buildProblem(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE, 
                "Unsupported Media Type", 
                "Content-Type mismatch. We only support data payload transfers in valid JSON format.", 
                "ERR_API_UNSUPPORTED_MEDIA", 
                TYPE_UNSUPPORTED_MEDIA, 
                ex
        );
    }

    // Handle Server Issues (500 Internal Server Boundary - Runtime/JVM Crashes)
    @SuppressWarnings("null")
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAllRemainingErrors(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String title = "Server Error";
        String message = "Something went wrong on our end while processing your request.";
        String errorCode = "ERR_API_WEB_FRAMEWORK_FAILURE";
        URI typeUri = TYPE_INTERNAL_SERVER_ERROR;

        if (ex instanceof org.springframework.web.server.ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            title = "Request Error";
            message = rse.getReason();
            errorCode = "ERR_API_BAD_REQUEST";
            typeUri = TYPE_BAD_REQUEST;
            log.warn("[INFRA EXCEPTION] ResponseStatusException intercepted. Status: {}, Reason: {}", status, message);
        } else {
            log.error("[CRITICAL FAILURE] Unhandled runtime exception or JVM crash intercepted at application perimeter.", ex);
        }

        return buildProblem(status, title, message, errorCode, typeUri, ex);
    }

    private ProblemDetail buildProblem(@NonNull HttpStatus status, String title, String detail, String errorCode, @NonNull URI typeUri, Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        
        problemDetail.setType(typeUri); 
        problemDetail.setTitle(title);
        problemDetail.setProperty("errorCode", errorCode);
        problemDetail.setProperty("timestamp", Instant.now());

        if (status.is4xxClientError()) {
            problemDetail.setProperty("technicalDetails", ex.getMessage());
        }
        return problemDetail;
    }
}
