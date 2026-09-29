package com.example.employeemanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false, length = 60)
    private String name;

    /**
     * mappedBy = owning side Employee.department hai, yani foreign key employees table mein hai.
     * LAZY default hai collections ke liye; cascade jaan boojh ke nahi diya taki department delete
     * karne se employees delete na ho jaaye.
     */
    @OneToMany(mappedBy = "department")
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();
}
