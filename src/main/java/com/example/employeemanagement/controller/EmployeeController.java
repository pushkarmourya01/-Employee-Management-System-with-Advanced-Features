package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.EmployeeRequest;
import com.example.employeemanagement.dto.EmployeeResponse;
import com.example.employeemanagement.dto.EmployeeSearchCriteria;
import com.example.employeemanagement.dto.PageResponse;
import com.example.employeemanagement.dto.SalaryByDepartment;
import com.example.employeemanagement.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    //best practise hai bhai autowired se toh
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse created = employeeService.createEmployee(request);
        return ResponseEntity
                .created(URI.create("/api/employees/" + created.id()))
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    /**
     * ?page=0&size=10&sort=salary,desc&keyword=push&position=Developer&minSalary=1000
     * Pageable Spring khud request params se bana deta hai.
     */
    @GetMapping
    public ResponseEntity<PageResponse<EmployeeResponse>> searchEmployees(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        EmployeeSearchCriteria criteria =
                new EmployeeSearchCriteria(keyword, position, departmentId, minSalary, maxSalary);
        return ResponseEntity.ok(employeeService.searchEmployees(criteria, pageable));
    }

    @GetMapping("/by-department/{name}")
    public ResponseEntity<List<EmployeeResponse>> getByDepartmentName(@PathVariable String name) {
        return ResponseEntity.ok(employeeService.getByDepartmentName(name));
    }

    @GetMapping("/stats/salary-by-department")
    public ResponseEntity<List<SalaryByDepartment>> salaryStats() {
        return ResponseEntity.ok(employeeService.salaryStats());
    }

    @GetMapping("/stats/count-above")
    public ResponseEntity<Long> countEarningMoreThan(@RequestParam double salary) {
        return ResponseEntity.ok(employeeService.countEarningMoreThan(salary));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long id,
                                                           @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
