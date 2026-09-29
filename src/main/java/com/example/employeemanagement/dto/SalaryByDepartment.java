package com.example.employeemanagement.dto;

/**
 * JPQL constructor expression ke liye projection — poori entity load kiye bina
 * sirf aggregate columns aate hai.
 */
public record SalaryByDepartment(String department, long employeeCount, Double averageSalary) {
}
