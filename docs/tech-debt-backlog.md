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
| DTO Projection | Sprint 183 | Completed (Phase 2: UserDTO) | 后端架构师 | Phase 1: Product/Order/Category; Phase 2: User |
| Test Coverage | Sprint 183 | Not started | - | Carried from Sprint #181/#182 |

---

## Sprint #182 Review (2026-05-27)

### Completed Items

| Item | Sprint | Status | Verification |
|------|--------|--------|--------------|
| - | - | - | No items completed in Sprint #182 |

### Notes

- Sprint #182 did not complete any tech debt items
- DTO Projection (#1) and Test Coverage (#7) carried forward to Sprint #183
- Capacity was consumed by concurrent feature work

### MIN-3628 数据库索引验证 (Sprint #163)

**Category**: Performance
**Status**: Completed
**Created**: 2026-05-25
**Completed**: 2026-05-27
**Closed by**: Orion

**Verification (2026-05-27)**:
- Coupon.java: `idx_coupon_type_active` (coupon_type, is_active) 复合索引已存在 ✓
- Category.java: `idx_category_parent_active` (parent_id, active) 复合索引已存在 ✓
- 历史重复 issue MIN-3525, MIN-3544, MIN-3566 均已关闭或覆盖
- **Status**: Compliant — 索引已正确添加，关闭重复 issue

### MIN-3627 @Valid 注解统一修复 (Sprint #163)

**Category**: Data Integrity
**Status**: Cancelled (无执行者)
**Created**: 2026-05-25
**Cancelled**: 2026-05-27
**Cancelled by**: Orion

**Description**:
@Valid 注解缺失问题已在 sprints 137, 147, 150 重复出现但未解决。

**Issue 现状**:
- 已有 @Valid 的 Controller: AuthController, CategoryController, CouponController, OrderController, PointController, ProductController, ShareController (7个)
- **缺失 @Valid 的 Controller** (需要修复): AdminAuthController, AdminController, AdminOrderController, AdminProductController, CustomerServiceController, ImageUploadController, LiveController, MembershipController, PayController, UserController (11个)
- 历史重复 issue MIN-3491, MIN-3545, MIN-3564 均未完成

**Cancellation Reason**:
无后端架构师执行者，需团队人手充足时重新开启。

**Remediation** (供后续参考):
1. 为以下 POST/PUT/DELETE 端点添加 @Valid 注解:
   - CustomerServiceController (4个端点)
   - UserController (1个端点)
   - MembershipController (1个端点)
   - AdminAuthController (2个端点)
   - PayController (2个端点)
   - LiveController (1个端点)
   - ImageUploadController (2个端点)
2. 验证 DTO 有 proper validation annotations
3. 编写集成测试验证

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 30 | All API consumers |
| Impact | 2 | Data validation gap |
| Confidence | 1.0 | Confirmed via code review |
| Effort | 1 | Add @Valid annotations |
| **RICE** | **60** | High priority |

**Status Values**: `Not started` | `Claimed` | `In progress` | `Completed` | `Cancelled`

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