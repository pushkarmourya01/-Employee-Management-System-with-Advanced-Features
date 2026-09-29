package com.example.employeemanagement.mapper;

import com.example.employeemanagement.dto.EmployeeRequest;
import com.example.employeemanagement.dto.EmployeeResponse;
import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.entity.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public Employee toEntity(EmployeeRequest request, Department department) {
        return Employee.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phone(request.phone())
                .position(request.position())
                .salary(request.salary())
                .department(department)
                .build();
    }

    public void updateEntity(Employee employee, EmployeeRequest request, Department department) {
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setPhone(request.phone());
        employee.setPosition(request.position());
        employee.setSalary(request.salary());
        employee.setDepartment(department);
    }

    public EmployeeResponse toResponse(Employee employee) {
        Department department = employee.getDepartment();
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getPosition(),
                employee.getSalary(),
                department == null ? null : department.getId(),
                department == null ? null : department.getName(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }
}
