package com.example.employeemanagement.dto;

/**
 * Sare optional filters ek object mein. Null ka matlab "ye filter mat lagao".
 */
public record EmployeeSearchCriteria(
        String keyword,
        String position,
        Long departmentId,
        Double minSalary,
        Double maxSalary
) {
}
