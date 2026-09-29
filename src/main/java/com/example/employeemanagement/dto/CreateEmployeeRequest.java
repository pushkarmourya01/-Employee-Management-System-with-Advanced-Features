package com.example.employeemanagement.dto;

import jakarta.validation.constraints.*;

/**
 * Client se aane wala JSON is class mein bind hota hai — Entity nahi.
 * Validation yahan isliye: galat data database tak pahunche hi nahi.
 */
public class CreateEmployeeRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be 2 to 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be 2 to 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must be at most 100 characters")
    private String email;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    @Pattern(regexp = "^$|^[0-9+\\-\\s]{7,20}$", message = "Phone can contain digits, +, - and spaces")
    private String phone;

    @NotBlank(message = "Position is required")
    @Size(max = 50, message = "Position must be at most 50 characters")
    private String position;

    @NotNull(message = "Salary is required")
    @Positive(message = "Salary must be greater than 0")
    private Double salary;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }
}
