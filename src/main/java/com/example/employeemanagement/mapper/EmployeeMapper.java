package com.example.employeemanagement.mapper;

import com.example.employeemanagement.dto.CreateEmployeeRequest;
import com.example.employeemanagement.dto.EmployeeResponse;
import com.example.employeemanagement.dto.UpdateEmployeeRequest;
import com.example.employeemanagement.entity.Employee;
import org.springframework.stereotype.Component;

/**
 * DTO <-> Entity conversion ek hi jagah.
 * Controller/Service ko JSON mapping ka kaam nahi karna chahiye scatter hoke.
 */
@Component
public class EmployeeMapper {

    public Employee toEntity(CreateEmployeeRequest request) {
        Employee employee = new Employee();
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setPosition(request.getPosition());
        employee.setSalary(request.getSalary());
        return employee;
    }

    public void updateEntity(Employee existing, UpdateEmployeeRequest request) {
        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setEmail(request.getEmail());
        existing.setPhone(request.getPhone());
        existing.setPosition(request.getPosition());
        existing.setSalary(request.getSalary());
    }

    public EmployeeResponse toResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setEmail(employee.getEmail());
        response.setPhone(employee.getPhone());
        response.setPosition(employee.getPosition());
        response.setSalary(employee.getSalary());
        response.setCreatedAt(employee.getCreatedAt());
        return response;
    }
}
