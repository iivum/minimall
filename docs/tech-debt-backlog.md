# Tech Debt Backlog

**created**: 2026-05-15
**last updated**: 2026-05-27
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
**Status**: Completed
**Created**: 2026-05-15
**Completed**: 2026-05-25

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
**Status**: Completed
**Created**: 2026-05-15
**Completed**: 2026-05-25

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

**Verification (2026-05-25)**:
- All @Query methods in repository layer are SELECT queries (read-only)
- LiveLikeRepository.deleteByLiveRoomIdAndUserId() already has @Modifying
- Service layer methods that modify data have @Transactional annotation
- No nativeQuery = true mutations found in codebase
- Status: Compliant — no code changes required

---

### 4. Unbounded @Async Thread Pool

**Category**: Concurrency
**Status**: Completed
**Created**: 2026-05-15
**Completed**: 2026-05-26

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

**Verification (2026-05-26)**:
- AsyncConfig.java created with bounded thread pool (corePoolSize=5, maxPoolSize=10, queueCapacity=100)
- CallerRunsPolicy rejection policy configured
- @EnableAsync with custom executor enabled
- Commit: d0f962c feat: add bounded thread pool AsyncConfig
- Status: Completed

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

### 8. E2E Test Infrastructure

**Category**: Testing
**Status**: Backlog
**Created**: 2026-05-28

**Description**:
E2E 测试（OrderFlowE2ETest、PaymentFlowE2ETest）持续失败，AuthFlowE2ETest 虽然已修复但其他两个测试仍有问题。根因是 Spring Boot Test 配置问题，需要 mock WebClient 和正确的测试环境配置。

**Evidence**:
```java
// E2E tests return 400/500 errors
// ApplicationContext fails to load for OrderFlowE2ETest and PaymentFlowE2ETest
```

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 10 | All E2E test users |
| Impact | 2 | Cannot verify E2E flows |
| Confidence | 0.8 | Confirmed via多次尝试 |
| Effort | 3 | Debug and fix test config |
| **RICE** | **5.3** | Low priority |

**Remediation**:
1. Debug OrderFlowE2ETest 和 PaymentFlowE2ETest
2. Consider removing unstable E2E tests if they cannot be fixed
3. Ensure mvn test passes all tests

---

## Repayment Plan

### Tech Debt Claiming Mechanism

When an agent or developer claims a tech debt item for a sprint:

1. **Claim Format**: Update the item's status to `Claimed` and add a claiming entry:
   ```
   **Claimed by**: [Agent Name] on YYYY-MM-DD
   **Sprint Target**: Sprint #XX
   ```

2. **Completion Format**: When the tech debt is resolved:
   - Update the item's status to `Completed`
   - Record the completion date and any relevant notes
   - Update the Progress Tracking table

3. **Claim Rules**:
   - Only one agent should claim an item at a time
   - If the item is not completed within the target sprint, it should be re-claimed or moved back to `Backlog`
   - PR for tech debt must include updates to this document

### Sprint Allocation
Each sprint dedicates **15%** of capacity to tech debt reduction.

For a 2-week sprint with 10 working days:
- 1.5 days (12 hours) allocated to tech debt
- Remaining 8.5 days for feature work

### Q2 2026 Schedule

| Sprint | Focus Area | Items | Status |
|--------|------------|-------|--------|
| Sprint 35 | Error Handling | GlobalExceptionHandler (#6) | Completed |
| Sprint 36 | Pagination | Add pagination to list endpoints (#2) | Completed |
| Sprint 37 | Data Integrity | Fix @Modifying issues (#3) | Completed |
| Sprint 38 | Concurrency | Configure async executor (#4) | Completed |
| Sprint 39 | Architecture | Create DTO projections (#1) | Backlog |

### Progress Tracking

| Item | Sprint Target | Status | Claimed By | Notes |
|------|---------------|--------|------------|-------|
| GlobalExceptionHandler | Sprint 35 | Completed | 后端架构师 | - |
| Pagination | Sprint 36 | Completed | 后端架构师 | Added Page<T> support to 5 controllers |
| @Modifying | Sprint 37 | Completed | 后端架构师 | Verified compliant — no code changes needed |
| Async Executor | Sprint 38 | Completed | 后端架构师 | AsyncConfig with bounded queue |
| DTO Projection | Sprint 183 | Not started | - | Carried from Sprint #179/#180/#181/#182 |
| Test Coverage | Sprint 183 | Not started | - | Carried from Sprint #181/#182; blocked by E2E infrastructure issue |

---

## Sprint #207 Review (2026-05-28)

### Completed Items

| Item | Sprint | Status | Verification |
|------|--------|--------|--------------|
| Sprint #202 Legacy Issue Assignment | Sprint #202 | Completed | MIN-3884 - 遗留 issues 已指派给团队成员 |
| Sprint #201 Continuation: E2E Test Infrastructure | Sprint #201 | Completed | MIN-3875 - application-test.yml 配置已完善 |
| Sprint #201 Continuation: tech-debt-backlog.md Planning | Sprint #201 | Completed | MIN-3871 - 文档更新已合并到 main |

### Notes

- Phase 26 Sprint Review and Planning meeting held on 2026-05-28
- Sprint #202 fully completed with legacy issue tracking
- Sprint #201 continuation tasks (E2E test configuration and documentation) completed
- E2E test infrastructure fix (MIN-3891) remains P0 priority for Sprint #207

**Status Values**: `Not started` | `Claimed` | `In progress` | `Completed` | `Backlog`

---

## Sprint #207 Planning

### Planned Tech Debt Items

| Item | Priority | Estimated Effort | Notes |
|------|----------|------------------|-------|
| E2E Test Infrastructure Final Fix (#8) | P0 | 3 days | Continuation from Sprint #200/#201/#202 - mvn test must pass |
| Controller Unit Test Coverage (#7) | P1 | 1 day | Coverage target: 20%+ for Controller layer |
| Legacy Issue Detection Script Enhancement | P2 | 0.5 days | Script validation and completion |

### Focus Area

Sprint #207 will focus on **Testing** infrastructure improvements. E2E test fixes remain P0 priority to unblock other work and ensure CI pipeline reliability.

---

## Sprint #201 Review (2026-05-28)

### Completed Items

| Item | Sprint | Status | Verification |
|------|--------|--------|--------------|
| - | - | - | Sprint in progress |

### Notes

- Sprint #201 Planning meeting held on 2026-05-28
- E2E Test Infrastructure fix (MIN-3862 continuation) is P0 priority
- Controller Unit Test Coverage improvement (MIN-3870) is P1 priority

**Status Values**: `Not started` | `Claimed` | `In progress` | `Completed` | `Backlog`

---

## Sprint #201 Planning

### Planned Tech Debt Items

| Item | Priority | Estimated Effort | Notes |
|------|----------|------------------|-------|
| E2E Test Infrastructure (#8) | P0 | 0.5 days | Continuation from Sprint #200 — application-test.yml needs customer-service.auto-reply config |
| Controller Unit Test Coverage (#7) | P1 | 1 day | Coverage target: 20%+ for Controller layer |

### Focus Area

Sprint #201 will focus on **Testing** infrastructure improvements. E2E test fixes are P0 priority to unblock other work.

---

## Sprint #184 Review (2026-05-28)

### Completed Items

| Item | Sprint | Status | Verification |
|------|--------|--------|--------------|
| - | - | - | No items completed in Sprint #184 |

### Notes

- MIN-3818 (测试覆盖率提升) 降级到 backlog - 执行者多次声称完成但代码未合并到 main
- MIN-3816 (E2E 测试 ApplicationContext) 降级到 backlog - Sprint #191/#192/#193 连续三次未完成
- 两个 blocked issue 都与后端架构师执行能力相关，需要独立的问题解决者

### New Tech Debt Identified

| Item | Category | RICE | Reason |
|------|----------|------|--------|
| E2E Test Infrastructure (#8) | Testing | 20 | AuthFlowE2ETest 已修复，但 OrderFlowE2ETest 和 PaymentFlowE2ETest 仍有问题；需要深度调试或重构测试架构 |

**Status Values**: `Not started` | `Claimed` | `In progress` | `Completed` | `Backlog`

---

## Sprint #183 Review (2026-05-27)

### Completed Items

| Item | Sprint | Status | Verification |
|------|--------|--------|--------------|
| - | - | - | No items completed in Sprint #182 |

### Notes

- Sprint #182 did not complete any tech debt items
- DTO Projection (#1) and Test Coverage (#7) carried forward to Sprint #183
- Capacity was consumed by concurrent feature work

**Status Values**: `Not started` | `Claimed` | `In progress` | `Completed`

---

## Sprint #183 Planning

### Planned Tech Debt Items

| Item | Priority | Estimated Effort | Notes |
|------|----------|------------------|-------|
| Missing Entity Projection DTOs (#1) | High | 5 days | Architectural improvement — carried from Sprint #179/#180/#181/#182 |
| Missing Unit Test Coverage (#7) | Medium | 10 days | Ongoing effort — carried from Sprint #181/#182 |

### Focus Area

Sprint #183 will focus on **Architecture** improvements with DTO projections as the primary tech debt item. Unit test coverage will be addressed concurrently.

---

## Sprint #181 Review (2026-05-27)

### Completed Items

| Item | Sprint | Status | Verification |
|------|--------|--------|--------------|
| - | - | - | No items completed in Sprint #181 |

### Notes

- Sprint #181 did not complete any tech debt items
- DTO Projection (#1) and Test Coverage (#7) carried forward to Sprint #182
- Capacity was consumed by concurrent feature work

**Status Values**: `Not started` | `Claimed` | `In progress` | `Completed`

---

## Sprint #182 Planning

### Planned Tech Debt Items

| Item | Priority | Estimated Effort | Notes |
|------|----------|------------------|-------|
| Missing Entity Projection DTOs (#1) | High | 5 days | Architectural improvement — carried from Sprint #179/#180/#181 |
| Missing Unit Test Coverage (#7) | Medium | 10 days | Ongoing effort |

### Focus Area

Sprint #182 will focus on **Architecture** improvements with DTO projections as the primary tech debt item. Unit test coverage will be addressed concurrently.

---

## Sprint #179 Planning

### Planned Tech Debt Items

| Item | Priority | Estimated Effort | Notes |
|------|----------|------------------|-------|
| Missing Entity Projection DTOs (#1) | High | 5 days | Architectural improvement |
| Field Injection in Services (#5) | Medium | 3 days | Code quality improvement |
| Missing Unit Test Coverage (#7) | Medium | 10 days | Ongoing effort |

### Focus Area

Sprint #179 will focus on **Architecture** improvements with DTO projections as the primary tech debt item.

---

## Sprint #176 Review (2026-05-26)

### Completed Items

| Item | Sprint | Status | Verification |
|------|--------|--------|--------------|
| Missing Pagination | Sprint 36 | Completed | PR #142 - Added Page<T> to 5 endpoints |
| @Modifying Annotation | Sprint 37 | Completed | Verified compliant - no code changes needed |
| Async Executor | Sprint 38 | Completed | AsyncConfig with bounded thread pool |

### Notes

- All planned Q2 tech debt items completed ahead of schedule
- Remaining items (DTO projections, Field Injection, Test Coverage) moved to future sprints

**Status Values**: `Not started` | `Claimed` | `In progress` | `Completed`

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