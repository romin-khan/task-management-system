package com.romin.infra.payload;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ErrorResponse {
    private String detail;
    private Map<String, List<String>> errors;
}
