package com.romin.infra.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.romin.infra.payload.ApiResponse;
import com.romin.infra.payload.ErrorResponse;

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
        String errorType;

        if(statusCode.value() == 400){
            errorType = "Invalid Submission";
            message = "We couldn't process your information because some data was formatted incorrectly. Please check your inputs.";
        }
        else if(statusCode.value() == 404){
            errorType = "Route Not Found";
            message = "The page or feature you are trying to reach doesn't seem to exist. Please verify the URL.";
        }
        else if(statusCode.value() == 405){
            errorType = "Action Not Allowed";
            message = "The action you are trying to perform is not supported by this screen. Please refresh and try again.";
        }
        else if(statusCode.value() == 415){
            errorType = "Unsupported Media Type";
            message = "we only support data in form of JSON";
        }else{
            HttpStatus status = HttpStatus.resolve(statusCode.value());
            errorType = (status != null) ? status.getReasonPhrase() : "Routing Error";
            message = "Somthing went wrong on our end while processing your request. please try again shortly.";
        }

        ErrorResponse error = ErrorResponse.builder()
                .detail(message)
                .build();

        return ResponseEntity
                .status(statusCode)
                .headers(headers)
                .body(
                    buildResponse(
                        error,
                        statusCode.value(),
                        errorType
                    )
                );
    }

    private ApiResponse<ErrorResponse> buildResponse(ErrorResponse error, int status, String customMessage){
        return ApiResponse.<ErrorResponse>builder()
                          .data(error)
                          .message(customMessage)
                          .timeStamp(LocalDateTime.now())
                          .status(status)
                          .build();
    }
}
