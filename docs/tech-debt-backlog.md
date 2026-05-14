# Tech Debt Backlog

**created**: 2026-05-15
**last updated**: 2026-05-15
**sprint capacity allocation**: 15% per sprint

---

## Overview

This document tracks technical debt for MiniMall project. Each item is scored using RICE methodology:
- **Reach**: Users impacted per quarter (1-1000)
- **Impact**: Effect on metric (0.25, 0.5, 1, 2, 3)
- **Confidence**: Estimation confidence (0.5, 0.8, 1.0)
- **Effort**: Person-days required
- **RICE Score** = (Reach × Impact × Confidence) / Effort

Items are prioritized by RICE score. Items with score > 50 should be addressed within current quarter.

---

## Tech Debt Registry

### 1. Missing Entity Projection DTOs

**Category**: Architecture
**Status**: Backlog
**Created**: 2026-05-15

**Description**:
JPA entities are returned directly from controllers (e.g., `Product`, `Order`). This exposes internal model structure and creates coupling between API and database schema.

**Impact**:
- API breaking changes when entity structure changes
- Potential N+1 queries due to lazy loading
- Circular reference risks in JSON serialization

**Evidence**:
```java
// src/main/java/com/minimall/controller/ProductController.java
@GetMapping("/{id}")
public Product getProduct(@PathVariable Long id) {
    return productService.findById(id); // Returns JPA entity
}
```

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 10 | All API users affected (estimation) |
| Impact | 2 | API stability and performance |
| Confidence | 0.8 | Confident in diagnosis |
| Effort | 5 | Create ~15 DTOs, update controllers |
| **RICE** | **3.2** | Low priority |

**Remediation**:
1. Create DTOs for all entity endpoints
2. Use JPA Entity Graph or JOIN FETCH for queries
3. Update controllers to return DTOs

---

### 2. Missing Pagination on List Endpoints

**Category**: Performance
**Status**: Backlog
**Created**: 2026-05-15

**Description**:
List endpoints (e.g., `/products`, `/orders`) return unbounded `List<T>`. No pagination or limits, causing potential memory and performance issues with large datasets.

**Evidence**:
```java
// src/main/java/com/minimall/repository/ProductRepository.java
List<Product> findAll(); // No limit
```

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 50 | All users of list endpoints |
| Impact | 2 | Performance degradation at scale |
| Confidence | 1.0 | Confirmed via code review |
| Effort | 3 | Add Pageable, update repositories |
| **RICE** | **33.3** | Medium priority |

**Remediation**:
1. Add `Pageable` to all list repository methods
2. Return `Page<T>` from controllers
3. Add `LIMIT` clauses to native queries

---

### 3. Missing @Modifying Annotation

**Category**: Data Integrity
**Status**: Backlog
**Created**: 2026-05-15

**Description**:
Mutating `@Query` methods in repositories lack `@Modifying` annotation. This can cause stale data or silent failures.

**Evidence**:
```java
// src/main/java/com/minimall/repository/ProductRepository.java
@Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :id")
void decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);
// Missing @Modifying
```

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 5 | Only write operations affected |
| Impact | 3 | Data inconsistency risk |
| Confidence | 1.0 | Confirmed via code review |
| Effort | 1 | Add annotation, add @Transactional |
| **RICE** | **15** | Medium priority |

**Remediation**:
1. Add `@Modifying` to all mutating queries
2. Add `@Transactional` on service layer for mutating operations
3. Verify update counts are checked

---

### 4. Unbounded @Async Thread Pool

**Category**: Concurrency
**Status**: Backlog
**Created**: 2026-05-15

**Description**:
`@Async` methods use default `SimpleAsyncTaskExecutor`, creating unbounded threads under load.

**Evidence**:
```java
// src/main/java/com/minimall/service/OrderService.java
@Async
public void sendNotification(Order order) {
    // Uses default unbounded executor
}
```

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 20 | All async operations (wechat notifications, etc.) |
| Impact | 2 | Thread exhaustion under load |
| Confidence | 0.8 | Confirmed via code review |
| Effort | 2 | Create custom ThreadPoolTaskExecutor bean |
| **RICE** | **16** | Medium priority |

**Remediation**:
1. Create custom `ThreadPoolTaskExecutor` with bounded queue
2. Configure rejection policy
3. Replace all `@Async` to use named executor

---

### 5. Field Injection in Services

**Category**: Code Quality
**Status**: Backlog
**Created**: 2026-05-15

**Description**:
`@Autowired` on fields instead of constructor injection. Makes testing harder and hides dependencies.

**Evidence**:
```java
// src/main/java/com/minimall/service/ProductService.java
@Autowired
private ProductRepository productRepository;
```

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 10 | All @Service classes |
| Impact | 1 | Testing difficulty, not runtime |
| Confidence | 1.0 | Confirmed via grep |
| Effort | 3 | Refactor all services |
| **RICE** | **3.3** | Low priority |

**Remediation**:
1. Convert field injection to constructor injection
2. Make dependencies final
3. Add `@RequiredArgsConstructor` or explicit constructors

---

### 6. Missing @RestControllerAdvice

**Category**: Error Handling
**Status**: Backlog
**Created**: 2026-05-15

**Description**:
Exception handling scattered across controllers. No centralized error response format.

**Evidence**:
```java
// Scattered try-catch in controllers
try {
    orderService.cancel(id);
} catch (OrderNotFoundException e) {
    return ResponseEntity.status(404).body("Not found");
}
```

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 30 | All API consumers |
| Impact | 2 | Inconsistent error responses |
| Confidence | 1.0 | Confirmed via code review |
| Effort | 2 | Create @RestControllerAdvice |
| **RICE** | **30** | Medium priority |

**Remediation**:
1. Create `GlobalExceptionHandler` class
2. Define standard error response format
3. Add exception mappings for all custom exceptions

---

### 7. Missing Unit Test Coverage for Service Layer

**Category**: Testing
**Status**: Backlog
**Created**: 2026-05-15

**Description**:
Service layer lacks comprehensive unit tests. Some services have basic tests but coverage is below 80%.

**Evidence**:
Current test coverage is estimated at ~40% for service layer.

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 10 | Developers maintain this code |
| Impact | 2 | Bugs harder to detect pre-production |
| Confidence | 0.6 | Estimated coverage, not measured |
| Effort | 10 | Write ~50 tests |
| **RICE** | **1.2** | Low priority |

**Remediation**:
1. Run `mvn test jacoco:report` to get baseline
2. Add tests for all public service methods
3. Use Mockito for dependencies

---

### 8. JaCoCo Configuration Management

**Category**: DevOps
**Status**: Backlog
**Created**: 2026-05-15

**Description**:
JaCoCo code coverage plugin is referenced in documentation (`mvn test jacoco:report`) but not properly configured in `pom.xml`. The plugin is not declared in the build plugins section, relying on Spring Boot's default behavior which may not generate detailed coverage reports.

**Evidence**:
```bash
# README.md and tech-debt-backlog.md reference:
./mvnw test jacoco:report

# But pom.xml has no jacoco-maven-plugin configuration
$ grep -A5 "jacoco" pom.xml
# (no results)
```

**Impact**:
- Inconsistent coverage reporting across environments
- Missing coverage data for CI/CD pipelines
- Difficulty tracking actual test coverage trends

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 5 | CI/CD and quality reporting |
| Impact | 1 | Reporting only, not runtime |
| Confidence | 1.0 | Confirmed via code inspection |
| Effort | 1 | Add plugin configuration |
| **RICE** | **5** | Low priority |

**Remediation**:
1. Add `jacoco-maven-plugin` to `pom.xml` build plugins
2. Configure report formats (HTML, XML, CSV)
3. Set coverage thresholds for CI enforcement
4. Update README to reflect actual coverage report location

---

## Repayment Plan

### Sprint Allocation
Each sprint dedicates **15%** of capacity to tech debt reduction.

For a 2-week sprint with 10 working days:
- 1.5 days (12 hours) allocated to tech debt
- Remaining 8.5 days for feature work

### Q2 2026 Schedule

| Sprint | Focus Area | Items |
|--------|------------|-------|
| Sprint 35 | Error Handling | GlobalExceptionHandler (#6) |
| Sprint 36 | Pagination | Add pagination to list endpoints (#2) |
| Sprint 37 | Data Integrity | Fix @Modifying issues (#3) |
| Sprint 38 | Concurrency | Configure async executor (#4) |
| Sprint 39 | Architecture | Create DTO projections (#1) |

### Progress Tracking

| Item | Sprint Target | Status | Notes |
|------|---------------|--------|-------|
| GlobalExceptionHandler | Sprint 35 | Not started | - |
| Pagination | Sprint 36 | Not started | - |
| @Modifying | Sprint 37 | Not started | - |
| Async Executor | Sprint 38 | Not started | - |
| DTO Projection | Sprint 39 | Not started | - |
| Field Injection | Future | Not started | - |
| Test Coverage | Future | Not started | - |
| JaCoCo Configuration | Future | Not started | Low priority (RICE=5) |

---

## Adding New Tech Debt

When new tech debt is identified:

1. **Document**: Create entry in this file
2. **Score**: Calculate RICE score
3. **Assign**: Add to next sprint's tech debt slot
4. **Track**: Update status in quarterly review

Template:
```markdown
### [Title]

**Category**: [Architecture|Performance|Data Integrity|Concurrency|Code Quality|Testing]
**Status**: Backlog
**Created**: YYYY-MM-DD

**Description**:
[Clear description of the debt]

**Evidence**:
```java
// Code snippet showing the issue
```

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | X | ... |
| Impact | X | ... |
| Confidence | X | ... |
| Effort | X | ... |
| **RICE** | **X** | [Low/Medium/High] |

**Remediation**:
[Steps to fix]
```

---

## References

- [Spring Boot JPA Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html#data.jpa)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [RICE Scoring Method](https://www.productplan.com/rice-scoring/)