package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.DepartmentRequest;
import com.example.employeemanagement.dto.DepartmentResponse;
import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.exception.DuplicateResourceException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Department already exists: " + request.name());
        }
        Department saved = departmentRepository.save(Department.builder().name(request.name()).build());
        return new DepartmentResponse(saved.getId(), saved.getName(), 0);
    }

    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll().stream()
                .map(d -> new DepartmentResponse(d.getId(), d.getName(), d.getEmployees().size()))
                .toList();
    }

    public DepartmentResponse getById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return new DepartmentResponse(department.getId(), department.getName(), department.getEmployees().size());
    }
}
