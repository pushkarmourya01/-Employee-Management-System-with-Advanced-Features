package com.example.employeemanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Client se aane wala input. Entity ko directly @RequestBody mein lene se
 * client id/createdAt jaise fields bhi bhej sakta hai (mass assignment).
 */
public record EmployeeRequest(

        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must be at most 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must be at most 50 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = 100, message = "Email must be at most 100 characters")
        String email,

        @Pattern(regexp = "^$|^[0-9+\\-\\s]{7,20}$", message = "Phone must be 7-20 digits")
        String phone,

        @Size(max = 50, message = "Position must be at most 50 characters")
        String position,

        @Positive(message = "Salary must be greater than 0")
        Double salary
) {
}
