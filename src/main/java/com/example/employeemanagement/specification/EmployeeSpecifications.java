package com.example.employeemanagement.specification;

import com.example.employeemanagement.dto.EmployeeSearchCriteria;
import com.example.employeemanagement.entity.Employee;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic WHERE clause. 5 optional filters ke liye 32 alag repository methods likhne ki jagah
 * Criteria API se runtime pe query build hoti hai.
 */
public final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    public static Specification<Employee> matches(EmployeeSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(criteria.keyword())) {
                String pattern = "%" + criteria.keyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("lastName")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern)));
            }
            if (hasText(criteria.position())) {
                predicates.add(cb.equal(cb.lower(root.get("position")), criteria.position().toLowerCase()));
            }
            if (criteria.departmentId() != null) {
                predicates.add(cb.equal(root.get("department").get("id"), criteria.departmentId()));
            }
            if (criteria.minSalary() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), criteria.minSalary()));
            }
            if (criteria.maxSalary() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salary"), criteria.maxSalary()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
