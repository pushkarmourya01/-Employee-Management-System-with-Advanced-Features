package com.example.employeemanagement.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Spring ka Page object directly return karna API contract ko Spring ke internal
 * JSON structure se baandh deta hai (aur version upgrade pe shape badal jata hai).
 * Isliye apna stable wrapper.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}
