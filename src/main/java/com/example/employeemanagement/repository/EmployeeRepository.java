package com.example.employeemanagement.repository;

import com.example.employeemanagement.dto.SalaryByDepartment;
import com.example.employeemanagement.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>,
        JpaSpecificationExecutor<Employee> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    // Derived query: method ka naam hi query hai.
    List<Employee> findByDepartment_NameIgnoreCaseOrderBySalaryDesc(String departmentName);

    // Fetch join: employee + department ek hi SELECT mein -> N+1 problem khatam.
    @Query("""
            select e from Employee e left join fetch e.department
            where e.id = :id
            """)
    Optional<Employee> findByIdWithDepartment(@Param("id") Long id);

    // Constructor expression: poori entity ki jagah sirf aggregate columns.
    @Query("""
            select new com.example.employeemanagement.dto.SalaryByDepartment(
                coalesce(d.name, 'Unassigned'), count(e), avg(e.salary))
            from Employee e left join e.department d
            group by d.name
            order by avg(e.salary) desc
            """)
    List<SalaryByDepartment> salaryStatsByDepartment();

    // Native query: DB-specific SQL chahiye ho tab. nativeQuery = true.
    @Query(value = "select count(*) from employees where salary > :salary", nativeQuery = true)
    long countEarningMoreThan(@Param("salary") double salary);
}
