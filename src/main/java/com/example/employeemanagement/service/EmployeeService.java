package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.EmployeeRequest;
import com.example.employeemanagement.dto.EmployeeResponse;
import com.example.employeemanagement.dto.EmployeeSearchCriteria;
import com.example.employeemanagement.dto.PageResponse;
import com.example.employeemanagement.dto.SalaryByDepartment;
import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.exception.DuplicateResourceException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.mapper.EmployeeMapper;
import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.specification.EmployeeSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.employeeMapper = employeeMapper;
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use: " + request.email());
        }
        Employee saved = employeeRepository.save(employeeMapper.toEntity(request, resolveDepartment(request)));
        return employeeMapper.toResponse(saved);
    }

    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> ResourceNotFoundException.employee(id));
        return employeeMapper.toResponse(employee);
    }

    public PageResponse<EmployeeResponse> searchEmployees(EmployeeSearchCriteria criteria, Pageable pageable) {
        Page<Employee> page = employeeRepository.findAll(EmployeeSpecifications.matches(criteria), pageable);
        return PageResponse.from(page.map(employeeMapper::toResponse));
    }

    public List<SalaryByDepartment> salaryStats() {
        return employeeRepository.salaryStatsByDepartment();
    }

    public List<EmployeeResponse> getByDepartmentName(String departmentName) {
        return employeeRepository.findByDepartment_NameIgnoreCaseOrderBySalaryDesc(departmentName).stream()
                .map(employeeMapper::toResponse)
                .toList();
    }

    public long countEarningMoreThan(double salary) {
        return employeeRepository.countEarningMoreThan(salary);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.employee(id));
        if (employeeRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new DuplicateResourceException("Email already in use: " + request.email());
        }
        employeeMapper.updateEntity(employee, request, resolveDepartment(request));
        return employeeMapper.toResponse(employeeRepository.saveAndFlush(employee));
    }

    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw ResourceNotFoundException.employee(id);
        }
        employeeRepository.deleteById(id);
    }

    private Department resolveDepartment(EmployeeRequest request) {
        if (request.departmentId() == null) {
            return null;
        }
        return departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + request.departmentId()));
    }
}
