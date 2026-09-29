# Day 2 — DTO, Validation, Exception Handling, Lombok & Auditing

Day 1 mein humne basic CRUD banaya (Entity → Repository → Service → Controller).
Day 2 mein usi API ko **production-grade** banate hai. Neeche har concept ki theory + is repo mein
exactly kahan use hua hai.

---

## 1. DTO Pattern (Data Transfer Object)

**Problem (Day 1 wala code):** Controller directly `Employee` entity le raha tha aur wahi return kar raha tha.

```java
public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee)
```

Isme 4 dikkat hai:

1. **Mass assignment** — client JSON mein `"id": 99` ya `"createdAt": ...` bhej de toh woh bhi bind ho jayega.
2. **Over-exposure** — kal ko entity mein `password`, `aadharNumber`, `internalNotes` add karoge toh woh
   automatically API response mein leak ho jayega.
3. **Tight coupling** — DB column rename kiya toh API contract toot gaya. DB aur API ka contract alag hona chahiye.
4. **Validation confusion** — create ke time email required hai, patch ke time nahi. Entity pe validation
   rakhoge toh dono cases handle nahi honge.

**Solution:** Do alag objects —

| Class | Kaam | File |
|---|---|---|
| `EmployeeRequest` | Client → Server (input) | `dto/EmployeeRequest.java` |
| `EmployeeResponse` | Server → Client (output) | `dto/EmployeeResponse.java` |
| `Employee` | Sirf DB ke liye (JPA entity) | `entity/Employee.java` |

Dono DTO **Java `record`** hai (Java 16+). Record = immutable data carrier: fields `final`, constructor,
`equals`, `hashCode`, `toString` sab automatic. Getter ka naam `getEmail()` nahi, sirf `email()` hota hai.

**Conversion kaun karega?** `mapper/EmployeeMapper.java` — ek `@Component` jo
`toEntity()`, `updateEntity()`, `toResponse()` deta hai. Mapping logic ek jagah rahe toh service saaf rehti hai.
(Bade projects mein log **MapStruct** use karte hai jo yeh mapper compile time pe khud generate kar deta hai —
Day 3/4 mein dekh sakte hai.)

**Golden rule:** Entity kabhi controller ke bahar mat jaane do.

---

## 2. Bean Validation (`jakarta.validation`)

`spring-boot-starter-validation` dependency pehle se pom mein thi, par use nahi ho rahi thi. Ab
`EmployeeRequest` ke har field pe constraint laga hai:

```java
@NotBlank(message = "First name is required")
@Size(max = 50, message = "First name must be at most 50 characters")
String firstName,

@NotBlank @Email String email,

@Positive(message = "Salary must be greater than 0")
Double salary
```

Common annotations:

| Annotation | Matlab |
|---|---|
| `@NotNull` | null nahi ho sakta (khali string `""` chalegi) |
| `@NotEmpty` | null nahi + length > 0 (`" "` chalegi) |
| `@NotBlank` | null nahi + trim ke baad bhi text ho (String ke liye best) |
| `@Size(min,max)` | String/Collection ki length |
| `@Email` | email format |
| `@Pattern(regexp)` | custom regex (yahan phone ke liye) |
| `@Positive` / `@Min` / `@Max` | numbers |

**Trigger kaise hota hai?** Controller mein `@Valid` lagana zaroori hai:

```java
public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request)
```

`@Valid` ke bina annotations bekaar hai — Spring validate hi nahi karega. Validation fail hone pe Spring
`MethodArgumentNotValidException` throw karta hai, jo hum globally handle karte hai (point 3).

**Validation ki 3 layers** (teeno chahiye, ek doosre ka replacement nahi):
1. DTO validation → 400 turant, DB tak request jaati hi nahi.
2. Business validation (service mein) → jaise "email already exists" → 409.
3. DB constraint (`unique = true`, `nullable = false`) → last line of defence (race condition ke liye).

---

## 3. Custom Exceptions + `@RestControllerAdvice`

**Problem:** Day 1 mein service `null` return karti thi aur controller `if (employee != null)` check karta tha.
Null return karna anti-pattern hai — har caller ko yaad rakhna padta hai check karna, aur error ka reason gum ho jata hai.

**Ab:**

```java
Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> ResourceNotFoundException.employee(id));
```

Teen nayi classes:

- `exception/ResourceNotFoundException` → 404
- `exception/DuplicateResourceException` → 409 (duplicate email)
- `exception/GlobalExceptionHandler` → sab kuch pakadne wala central handler

Dono exceptions **`RuntimeException`** (unchecked) extend karti hai — isliye `throws` likhne ki zarurat nahi,
aur Spring `@Transactional` bhi by default sirf unchecked exception pe rollback karta hai.

### `@RestControllerAdvice` kya hai?

Ye ek **cross-cutting** component hai: `@ControllerAdvice` + `@ResponseBody`. Ye poore app ke saare
controllers ko "advise" karta hai. Jab bhi kisi controller se exception nikalti hai, Spring yahan matching
`@ExceptionHandler` method dhundta hai (sabse specific match jeetata hai) aur uska return value HTTP response ban jata hai.

```java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(...) { ... }   // 404
```

Fayda: controller mein ek bhi `try/catch` nahi, aur poore API ka error format ek jaisa.

### Error response format

`dto/ErrorResponse.java` — `timestamp`, `status`, `error`, `message`, `path`, aur validation ke case mein
`fieldErrors` map. `@JsonInclude(NON_NULL)` se null fields JSON mein aate hi nahi.

Validation error ka output:

```json
{
  "timestamp": "2026-09-29T16:10:12.34",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/employees",
  "fieldErrors": {
    "email": "Email must be a valid email address",
    "firstName": "First name is required"
  }
}
```

Last handler `@ExceptionHandler(Exception.class)` hai — koi bhi unexpected error aaye toh client ko generic
"Something went wrong" (500) milta hai, aur asli stack trace **log** mein jata hai. Stack trace client ko
dikhana security risk hai (DB structure, class names leak hote hai).

### HTTP status codes jo ab sahi se use ho rahe hai

| Case | Status |
|---|---|
| Create success | 201 Created + `Location` header |
| Read / update success | 200 OK |
| Delete success | 204 No Content |
| Validation fail | 400 Bad Request |
| Id nahi mila | 404 Not Found |
| Duplicate email | 409 Conflict |
| Unexpected crash | 500 Internal Server Error |

Note: create ab `Location: /api/employees/5` header bhi bhejta hai — REST ka proper tareeka.

---

## 4. Lombok

Entity mein 100+ line getters/setters the. Ab:

```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Employee { ... }
```

Lombok ek **annotation processor** hai: compile ke waqt bytecode mein getters/setters/constructors daal deta hai.
Source file saaf rehti hai, `.class` file mein sab kuch maujood hai.

- `@NoArgsConstructor` — JPA ko empty constructor **mandatory** chahiye (reflection se object banata hai).
- `@AllArgsConstructor` + `@Builder` — `Employee.builder().firstName("A").email("a@b.com").build()`.
  Builder pattern readable hai aur 6 same-type parameters wale constructor ki galtiyan bachata hai.
- Entity pe `@Data` **mat** lagao — woh `equals`/`hashCode`/`toString` bhi generate karta hai, jo JPA
  lazy relationships ke saath infinite recursion aur galat equality deta hai.

IntelliJ mein Lombok plugin enable hona chahiye + `Settings → Build → Compiler → Annotation Processors →
Enable annotation processing` tick hona chahiye, warna IDE red underline dikhayega (build phir bhi chalega).

---

## 5. Auditing timestamps

Day 1 mein `createdAt` field thi par usme value kabhi set hi nahi hoti thi (hamesha `null`). Ab:

```java
@CreationTimestamp
@Column(name = "created_at", updatable = false)
private LocalDateTime createdAt;

@UpdateTimestamp
@Column(name = "updated_at")
private LocalDateTime updatedAt;
```

Ye Hibernate annotations hai — insert pe `createdAt`, har update pe `updatedAt` automatic bhar jata hai.
`updatable = false` ka matlab: UPDATE statement mein ye column jaayega hi nahi, yani create time kabhi badlega nahi.

Alternative (Spring Data JPA ka tareeka): `@EnableJpaAuditing` + `@CreatedDate` / `@LastModifiedDate` /
`@CreatedBy` / `@LastModifiedBy`. `@CreatedBy` tab useful hoga jab Day 5 mein Spring Security aayega — tab
"kisne banaya" bhi record kar payenge.

---

## 6. `@Transactional` — chhota sa intro (poora Day 3 mein)

Service pe:

```java
@Service
@Transactional(readOnly = true)   // class level default: sirf read
public class EmployeeService {

    @Transactional                 // write methods pe override
    public EmployeeResponse createEmployee(...) { ... }
}
```

- Transaction = "ya toh sab kuch, ya kuch bhi nahi" (atomicity). Method exception se fail hua toh DB changes rollback.
- `readOnly = true` se Hibernate dirty-checking skip karta hai → read queries thodi fast, aur galti se
  write hone se bachav.
- Ye **proxy** se kaam karta hai: Spring tumhari class ke around ek wrapper banata hai. Isliye same class ke
  andar ek method se doosre `@Transactional` method ko direct call karoge toh transaction apply **nahi** hoga
  (self-invocation problem) — Day 3 mein detail mein.

---

## 7. Ab API kaisa behave karega (test karne ke liye)

```bash
# Valid create → 201
curl -i -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Pushkar","lastName":"Mourya","email":"p@example.com","phone":"9999999999","position":"Developer","salary":50000}'

# Invalid → 400 + fieldErrors
curl -i -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"firstName":"","email":"not-an-email","salary":-5}'

# Duplicate email → 409
# (same email dobara POST karo)

# Missing id → 404
curl -i http://localhost:8080/api/employees/9999
```

---

## Day 2 checklist (khud se verify karo)

- [ ] Entity kyu expose nahi karte — 4 reason bata sako
- [ ] `@NotNull` vs `@NotEmpty` vs `@NotBlank` ka fark
- [ ] `@Valid` hataao toh kya hota hai (validate hi nahi hoga)
- [ ] `@RestControllerAdvice` kaise exception ko HTTP response banata hai
- [ ] 400 vs 404 vs 409 vs 500 kab bhejna hai
- [ ] Entity pe `@Data` kyu mana hai
- [ ] `readOnly = true` ka fayda

## Day 3 preview

Pagination + sorting (`Pageable`), derived query methods, `@Query` (JPQL + native), search/filter endpoints,
`@OneToMany` Department ↔ Employee relationship, N+1 problem aur `fetch join`, transaction propagation.
