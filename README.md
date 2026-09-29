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
| POST | `/api/employees` | 201 + `Location` | 400 validation, 409 duplicate email |
| GET | `/api/employees` | 200 | — |
| GET | `/api/employees/{id}` | 200 | 404 |
| PUT | `/api/employees/{id}` | 200 | 400, 404, 409 |
| DELETE | `/api/employees/{id}` | 204 | 404 |

Errors ek hi shape mein aate hai (`timestamp`, `status`, `error`, `message`, `path`, optional `fieldErrors`).

## Learning notes

- [Day 2 — DTO, Validation, Exception Handling, Lombok, Auditing](docs/DAY2-THEORY.md)