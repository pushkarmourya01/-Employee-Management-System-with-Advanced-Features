# Day 3 — Pagination, Sorting, Dynamic Search, Custom Queries & Relationships

Day 2 mein API layer saaf ki (DTO, validation, exception handling). Day 3 **data layer** ka din hai:
badi tables ko efficiently handle karna, dynamic filters, custom queries aur entity relationships.

---

## 1. Pagination & Sorting — `Pageable`

**Problem:** `findAll()` 1 lakh employees load kar dega → memory blast + slow response.

**Solution:** Spring Data ka `Pageable`. Controller mein:

```java
@GetMapping
public ResponseEntity<PageResponse<EmployeeResponse>> searchEmployees(
        ...,
        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable)
```

Spring khud request params se `Pageable` bana deta hai:

```
GET /api/employees?page=0&size=2&sort=salary,desc
GET /api/employees?sort=lastName,asc&sort=salary,desc     # multi-sort bhi chalega
```

Andar DB pe ye chalta hai: `... order by salary desc limit 2 offset 0` + ek alag `count(*)` query
(total pages nikalne ke liye).

### Page vs Slice vs List

| Return type | Count query chalti hai? | Kab use karo |
|---|---|---|
| `Page<T>` | Haan | UI ko total pages/records dikhane hai |
| `Slice<T>` | Nahi | Infinite scroll ("next hai ya nahi" bas itna chahiye) — faster |
| `List<T>` | Nahi | Sirf ek chunk chahiye, metadata nahi |

### Spring ka `Page` API se bahar mat bhejo

`Page` ka JSON structure Spring ka internal detail hai (Boot 3.3 mein warning bhi aati hai ki iska
shape stable nahi). Isliye apna wrapper: `dto/PageResponse.java`

```java
public static <T> PageResponse<T> from(Page<T> page) {
    return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
            page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
}
```

Response:

```json
{ "content": [...], "page": 0, "size": 2, "totalElements": 5, "totalPages": 3, "first": true, "last": false }
```

**Ek trick:** `page.map(mapper::toResponse)` — `Page<Employee>` ko `Page<EmployeeResponse>` mein badalta hai
bina pagination metadata khoye.

### Offset pagination ki limitation

`limit 10 offset 100000` pe DB ko pehle 1,00,010 rows scan karni padti hai — deep pages slow hoti hai.
Bade data pe **keyset/cursor pagination** (`where id > :lastSeenId order by id limit 10`) better hai.

---

## 2. Query likhne ke 4 tareeke

### (a) Derived query — method ke naam se

```java
List<Employee> findByDepartment_NameIgnoreCaseOrderBySalaryDesc(String departmentName);
boolean existsByEmailAndIdNot(String email, Long id);
```

Spring method name parse karke query bana deta hai. `_` se nested property (`department.name`) point karte hai.
Keywords: `And`, `Or`, `Between`, `LessThan`, `Like`, `Containing`, `IgnoreCase`, `OrderBy`, `Top3`, `Distinct`.
Naam 4-5 condition se bada ho jaye toh `@Query` pe shift ho jao.

### (b) `@Query` JPQL — entity ki bhasha mein

```java
@Query("""
        select e from Employee e left join fetch e.department
        where e.id = :id
        """)
Optional<Employee> findByIdWithDepartment(@Param("id") Long id);
```

JPQL **tables nahi, entities** pe chalti hai (`Employee`, `e.department`), isliye DB badle toh bhi kaam karti hai.

### (c) Constructor expression — projection

```java
@Query("""
        select new com.example.employeemanagement.dto.SalaryByDepartment(
            coalesce(d.name, 'Unassigned'), count(e), avg(e.salary))
        from Employee e left join e.department d
        group by d.name
        order by avg(e.salary) desc
        """)
List<SalaryByDepartment> salaryStatsByDepartment();
```

Poori entity load karne ki zarurat hi nahi — DB se sirf 3 columns aate hai, seedha DTO mein.
(Interface-based projection bhi hota hai: bas ek interface with getters bana do.)

### (d) Native query — asli SQL

```java
@Query(value = "select count(*) from employees where salary > :salary", nativeQuery = true)
long countEarningMoreThan(@Param("salary") double salary);
```

Tab use karo jab DB-specific feature chahiye (window functions, `JSON_EXTRACT`, hints). Nuksaan: DB lock-in
aur entity mapping ka fayda nahi.

**Positional (`?1`) ki jagah hamesha named params (`:salary`) use karo** — readable aur refactor-safe.

---

## 3. Dynamic search — JPA `Specification` (Criteria API)

**Problem:** 5 optional filters = theoretically 32 combinations. 32 repository methods nahi likh sakte,
aur string concat karke SQL banana **SQL injection** ka rasta hai.

**Solution:** `JpaSpecificationExecutor` + `Specification` — runtime pe type-safe WHERE clause.

```java
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
        if (criteria.departmentId() != null) {
            predicates.add(cb.equal(root.get("department").get("id"), criteria.departmentId()));
        }
        ...
        return cb.and(predicates.toArray(new Predicate[0]));
    };
}
```

- `root` = FROM clause ka entity, `cb` = `CriteriaBuilder` (predicates banata hai), `query` = poori query.
- Null filter = predicate add hi nahi hota → clean SQL, sirf zaroori conditions.
- Values hamesha **bind parameters** banti hai → SQL injection impossible.
- Specifications combine bhi hote hai: `Specification.where(a).and(b).or(c)`.

Repository bas itna: `EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee>`
aur service mein `employeeRepository.findAll(spec, pageable)` — filter + pagination dono ek saath.

Try:
```
GET /api/employees?keyword=emp3&minSalary=1000
GET /api/employees?position=Developer&departmentId=1&page=0&size=5&sort=salary,desc
```

---

## 4. Relationships — `@ManyToOne` / `@OneToMany`

Naya `Department` entity, aur `Employee` uska owner:

```java
// Employee (owning side — foreign key isi table mein banta hai)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "department_id")
private Department department;

// Department (inverse side)
@OneToMany(mappedBy = "department")
private List<Employee> employees = new ArrayList<>();
```

- **Owning side** woh hai jiske table mein FK column hai (`employees.department_id`). `mappedBy` inverse side pe
  likhte hai aur uska matlab: "mai FK manage nahi karti, `Employee.department` karta hai".
- **`fetch = LAZY` bahut zaroori hai.** `@ManyToOne` ka default **EAGER** hai — matlab har employee fetch pe
  department ki extra query. `@OneToMany` ka default LAZY hi hai.
- `cascade` jaan boojh ke nahi diya: department delete karne pe employees delete nahi hone chahiye.

### N+1 problem

100 employees list kiye, phir har ek ka `getDepartment().getName()` padha →
**1 query employees ki + 100 queries departments ki**. Ye N+1 hai, aur app slow hone ka #1 karan.

Fix — **fetch join**:

```java
@Query("select e from Employee e left join fetch e.department where e.id = :id")
```

Ek hi SELECT mein employee + uska department. Alternatives: `@EntityGraph(attributePaths = "department")`,
ya batch fetching (`@BatchSize`).

**Caution:** `join fetch` + `Pageable` ek saath mat karo — Hibernate saari rows memory mein laake paginate
karta hai (`HHH90003004` warning). Uske liye do-step approach (pehle IDs page karo, phir un IDs ka fetch join)
ya `@EntityGraph` use karo.

### LazyInitializationException

Lazy field tabhi load hota hai jab session/transaction khula ho. Agar controller/JSON serializer transaction
ke baahar lazy field chhuye toh `LazyInitializationException` aata hai. Isi wajah se hum **service ke andar
(transaction ke andar) DTO bana lete hai** — entity kabhi controller tak jaati hi nahi. Ye Day 2 ke DTO
pattern ka ek aur bada fayda hai.

---

## 5. `@Transactional` — thoda gehra

```java
@Service
@Transactional(readOnly = true)     // class default
public class EmployeeService {
    @Transactional                   // writes pe override
    public EmployeeResponse createEmployee(...) { }
}
```

- **Propagation** (default `REQUIRED`): transaction chal rahi hai toh usi mein judo, warna nayi banao.
  `REQUIRES_NEW` hamesha nayi transaction (audit log likhne jaisa case, jo parent rollback hone pe bhi rahe).
  `MANDATORY` = transaction honi hi chahiye warna error.
- **Rollback rule:** default sirf **unchecked** (`RuntimeException`) pe rollback. Checked exception pe
  rollback chahiye toh `@Transactional(rollbackFor = Exception.class)`.
- **Self-invocation trap:** Spring proxy se kaam karta hai. Same class ke method A se method B (`@Transactional`)
  ko direct call karoge toh proxy bypass ho jata hai aur annotation **kaam nahi karega**. Solution: doosri bean
  mein daalo, ya self-inject karo.
- `readOnly = true` — Hibernate dirty checking skip karta hai, flush nahi hota → read queries halki.

---

## 6. Naye endpoints (Day 3)

| Method | Path | Kya karta hai |
|---|---|---|
| GET | `/api/employees?page=&size=&sort=&keyword=&position=&departmentId=&minSalary=&maxSalary=` | Paginated + dynamic filtered list |
| GET | `/api/employees/by-department/{name}` | Derived query, salary desc |
| GET | `/api/employees/stats/salary-by-department` | JPQL aggregate projection |
| GET | `/api/employees/stats/count-above?salary=40000` | Native query count |
| POST | `/api/departments` | Department banao (409 duplicate name pe) |
| GET | `/api/departments`, `/api/departments/{id}` | Department list / single |

Employee create/update mein ab optional `departmentId` bhej sakte ho; galat id pe 404 `Department not found`.

---

## Day 3 checklist

- [ ] `Page` vs `Slice` ka fark aur count query ka cost
- [ ] Spring ka `Page` API se directly kyu nahi bhejte
- [ ] Derived query kab, `@Query` kab, native kab
- [ ] Specification dynamic filters ke liye kyu behtar hai (aur SQL injection kyu nahi hota)
- [ ] Owning side vs `mappedBy`
- [ ] `@ManyToOne` ka default fetch type kya hai (EAGER — isliye LAZY likhna padta hai)
- [ ] N+1 problem samjhao aur 2 fix batao
- [ ] `@Transactional` self-invocation kyu fail hota hai

## Day 4 preview

JUnit 5 + Mockito se service unit tests, `@WebMvcTest` + MockMvc se controller tests, `@DataJpaTest` + H2 se
repository tests, Swagger/OpenAPI docs, `application-dev/prod` profiles, aur Actuator health endpoints.
