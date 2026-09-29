package com.example.employeemanagement.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException employee(Long id) {
        return new ResourceNotFoundException("Employee not found with id: " + id);
    }
}
