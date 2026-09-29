# Employee Management System

Spring Boot project for learning advanced concepts.

## Getting Started

### Prerequisites
- Java 17+
- Maven
- MySQL

### Running the Application
```bash
mvn spring-boot:run
```

Or run `EmployeeManagementApplication.java` in IntelliJ.

## API

Base path: `/api/employees`

| Method | Path | Success | Errors |
|---|---|---|---|
| POST | `/api/employees` | 201 + `Location` | 400 validation, 409 duplicate email, 404 unknown `departmentId` |
| GET | `/api/employees` | 200 paginated | — |
| GET | `/api/employees/{id}` | 200 | 404 |
| GET | `/api/employees/by-department/{name}` | 200 | — |
| GET | `/api/employees/stats/salary-by-department` | 200 | — |
| GET | `/api/employees/stats/count-above?salary=` | 200 | — |
| PUT | `/api/employees/{id}` | 200 | 400, 404, 409 |
| DELETE | `/api/employees/{id}` | 204 | 404 |
| POST | `/api/departments` | 201 + `Location` | 400, 409 duplicate name |
| GET | `/api/departments` | 200 | — |
| GET | `/api/departments/{id}` | 200 | 404 |

List endpoint pagination, sorting aur optional filters support karta hai:

```
GET /api/employees?page=0&size=10&sort=salary,desc&keyword=push&position=Developer&departmentId=1&minSalary=30000&maxSalary=90000
```

Errors ek hi shape mein aate hai (`timestamp`, `status`, `error`, `message`, `path`, optional `fieldErrors`).

## Learning notes

- [Day 2 — DTO, Validation, Exception Handling, Lombok, Auditing](docs/DAY2-THEORY.md)
- [Day 3 — Pagination, Sorting, Specifications, Custom Queries, Relationships](docs/DAY3-THEORY.md)