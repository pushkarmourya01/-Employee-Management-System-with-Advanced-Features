package com.example.employeemanagement.dto;

import java.time.LocalDateTime;

/**
 * API se bahar jaane wala output. Yahan wahi fields hai jo client ko dikhane hai.
 */
public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String position,
        Double salary,
        Long departmentId,
        String departmentName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
