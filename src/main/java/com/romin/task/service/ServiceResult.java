package com.romin.task.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ServiceResult<T> {
    private String message;
    private T data;
}
