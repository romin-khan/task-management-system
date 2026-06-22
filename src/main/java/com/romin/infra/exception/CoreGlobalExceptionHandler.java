package com.romin.infra.exception;

import java.time.Instant;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import org.springframework.lang.Nullable;

@RestControllerAdvice
public class CoreGlobalExceptionHandeller extends ResponseEntityExceptionHandler {

   @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            @NonNull Exception ex, 
            @Nullable Object body, 
            @NonNull HttpHeaders headers, 
            @NonNull HttpStatusCode statusCode, 
            @NonNull WebRequest request) {

        String message;
        String title;
        String errorCode;

        if(statusCode.equals(HttpStatus.BAD_REQUEST)){
            title = "Invalid Submission";
            message = "We couldn't process your information because some data was formatted incorrectly. Please check your inputs.";
            errorCode = "ERR_API_BAD_REQUEST";
        }
        else if(statusCode.equals(HttpStatus.NOT_FOUND)){
            title = "Route Not Found";
            message = "The page or feature you are trying to reach doesn't seem to exist. Please verify the URL.";
            errorCode = "ERR_API_ROUTE_NOT_FOUND";
        }
        else if(statusCode.equals(HttpStatus.METHOD_NOT_ALLOWED)){
            title = "Action Not Allowed";
            message = "The action you are trying to perform is not supported by this screen. Please refresh and try again.";
            errorCode = "ERR_API_ROUTE_NOT_FOUND";
        }
        else if(statusCode.equals(HttpStatus.UNSUPPORTED_MEDIA_TYPE)){
            title = "Unsupported Media Type";
            message = "we only support data in form of JSON";
            errorCode = "ERR_API_UNSUPPORTED_MEDIA";
        }else{
            HttpStatus status = HttpStatus.resolve(statusCode.value());
            title = (status != null) ? status.getReasonPhrase() : "Routing Error";
            message = "Somthing went wrong on our end while processing your request. please try again shortly.";
            errorCode = "ERR_API_WEB_FRAMEWORK_FAILURE";
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(statusCode, message);
        problemDetail.setTitle(title);

        problemDetail.setProperty("errorCode", errorCode);
        problemDetail.setProperty("timestamp", Instant.now());
        
        if(statusCode.is4xxClientError()){
            problemDetail.setProperty("technicaldetails", ex.getMessage());
        }

        return ResponseEntity
                .status(statusCode)
                .headers(headers)
                .body(problemDetail);
    }
}
