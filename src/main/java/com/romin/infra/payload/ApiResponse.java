package com.romin.infra.payload;

import java.time.LocalDateTime;


import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ApiResponse<T> {
    private int status;
    private LocalDateTime timeStamp;
    private String message;
    private T data;
}
