package com.romin.infra.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public record PaginatedResponse<T>(
    List<T> data,
    int currentPage,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean isLast
){
    public static <X> PaginatedResponse<X> from(Page<X> page){
        return new PaginatedResponse<>(
            page.getContent(),
            page.getNumber()+1,
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );
    }
}

