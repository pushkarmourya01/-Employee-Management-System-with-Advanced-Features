package com.example.employeemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(

        @NotBlank(message = "Department name is required")
        @Size(max = 60, message = "Department name must be at most 60 characters")
        String name
) {
}
