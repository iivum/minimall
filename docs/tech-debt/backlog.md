# Tech Debt Backlog

**created**: 2026-05-18
**last updated**: 2026-05-28
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

### 1. Missing Input Validation on DTOs (P0)

**Category**: Security
**Status**: Backlog
**Created**: 2026-05-18

**Description**:
DTO classes lack Jakarta Validation annotations (`@NotNull`, `@NotBlank`, `@Min`, etc.). This allows invalid data to enter the system.

**Impact**:
- Security risk: malicious input can bypass validation
- Data integrity issues
- Potential SQL injection or business logic errors

**Evidence**:
```java
// dto/CouponRequest.java - No validation annotations
public record CouponRequest(
    String code,
    BigDecimal discountAmount,
    Integer totalQuantity,
    String couponType
) {}

// dto/DeductPointsRequest.java - points field has no validation
public record DeductPointsRequest(
    Long accountId,
    Integer points  // Could be negative or zero
) {}
```

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 100 | All API users affected |
| Impact | 3 | Security risk, data corruption |
| Confidence | 1.0 | Confirmed via code review |
| Effort | 2 | Add annotations to ~10 DTOs |
| **RICE** | **150** | High priority |

**Remediation**:
1. Add `@NotNull`, `@NotBlank`, `@Min` annotations to all request DTOs
2. Add `@Valid` to controller methods accepting `@RequestBody`
3. Add unit tests for validation failure cases

---

### 2. Missing @Valid on Controller Endpoints (P0)

**Category**: Security
**Status**: Backlog
**Created**: 2026-05-18

**Description**:
Even when DTOs have validation annotations, `@Valid` is missing on controller methods, so validation is never triggered.

**Impact**:
- All DTO input validation is bypassed
- Security vulnerability on all write endpoints

**Evidence**:
```java
// controller/AuthController.java:39 - missing @Valid
@PostMapping("/register")
public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
    // request validation never triggered!
}

// controller/OrderController.java:57 - missing @Valid
@PostMapping
public ResponseEntity<?> createOrder(@RequestBody OrderDTO orderDTO) {
    // Invalid data enters system
}
```

**Files Affected**:
- `AuthController.java:39`
- `OrderController.java:57`
- `CategoryController.java:38,51`
- `CouponController.java:24`
- `ProductController.java:64,70`
- `PointController.java:62,69`
- `ShareController.java:23`

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 100 | All write API users |
| Impact | 3 | Complete validation bypass |
| Confidence | 1.0 | Confirmed via grep |
| Effort | 1 | Add @Valid to ~8 endpoints |
| **RICE** | **300** | Critical |

**Remediation**:
1. Add `@Valid` before `@RequestBody` on all POST/PUT/DELETE endpoints
2. Verify DTOs have proper validation annotations
3. Add integration tests for validation

---

### 3. GlobalExceptionHandler Incomplete (P1)

**Category**: Error Handling
**Status**: Backlog
**Created**: 2026-05-18

**Description**:
`GlobalExceptionHandler` doesn't handle several exception types, causing them to return generic 500 errors with potentially sensitive information.

**Impact**:
- Inconsistent error responses to API clients
- Information leakage via stack traces
- Poor user experience

**Evidence**:
```java
// exception/GlobalExceptionHandler.java - only handles:
- UnauthorizedException (returns 403)
// Missing handlers for:
- PaymentException (service/PayService.java:47,119)
- OrderException (class exists but not handled)
- ValidationException (class exists but not handled)
- IllegalArgumentException (service/ShareService.java, service/CouponService.java)
- IllegalStateException (service/CouponService.java)
```

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 50 | API consumers hitting errors |
| Impact | 2 | Inconsistent error format |
| Confidence | 1.0 | Confirmed via code review |
| Effort | 1 | Add ~5 exception handlers |
| **RICE** | **100** | High priority |

**Remediation**:
1. Add handlers for `PaymentException`, `IllegalArgumentException`, `IllegalStateException`
2. Return consistent error response format
3. Log full exception server-side, return generic message to client

---

### 4. N+1 Query in OrderService (P1)

**Category**: Performance
**Status**: Backlog
**Created**: 2026-05-18

**Description**:
`OrderService.create` iterates through order items and calls `productService.findById()` for each item, creating N+1 database queries.

**Impact**:
- Performance degradation with large orders
- Database load increased exponentially
- Potential timeout on large orders

**Evidence**:
```java
// service/OrderService.java:63-66
for (OrderItem item : items) {
    item.setPrice(productService.findById(item.getProduct().getId()).getPrice());
    // Each iteration = 1 DB query!
}
```

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 30 | Order creation users |
| Impact | 2 | Performance at scale |
| Confidence | 1.0 | Confirmed via code review |
| Effort | 2 | Create batch query method |
| **RICE** | **30** | Medium priority |

**Remediation**:
1. Create `findPricesByIds(List<Long> ids)` in ProductService
2. Use `IN` query with single round-trip
3. Add integration test with order > 5 items

---

### 5. Missing Pagination on Admin Endpoints (P1)

**Category**: Performance
**Status**: Backlog
**Created**: 2026-05-18

**Description**:
`AdminOrderController.getAllOrders()` returns all orders without pagination, causing potential OOM on large datasets.

**Impact**:
- Memory exhaustion on large order volumes
- Slow response times
- Timeout issues in production

**Evidence**:
```java
// controller/AdminOrderController.java:24-25
public ResponseEntity<List<Order>> getAllOrders() {
    return ResponseEntity.ok(orderService.findAll()); // No limit!
}
```

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 10 | Admin users only |
| Impact | 3 | OOM risk |
| Confidence | 1.0 | Confirmed via code review |
| Effort | 1 | Add Pageable to endpoint |
| **RICE** | **30** | Medium priority |

**Remediation**:
1. Add `@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size`
2. Return `Page<Order>` instead of `List<Order>`
3. Add LIMIT to database queries

---

### 6. Missing Database Indexes (P1)

**Category**: Performance
**Status**: Backlog
**Created**: 2026-05-18

**Description**:
Several models lack database indexes on frequently queried columns, causing slow queries at scale.

**Impact**:
- Slow queries on live room, comment, and like operations
- Poor performance as data grows

**Evidence**:
| Model | Missing Index | Query Pattern |
|-------|--------------|---------------|
| `LiveRoom.java` | `status` column | `findByStatus()` |
| `LiveComment.java` | `liveRoomId` column | `findByLiveRoomId()` |
| `LiveLike.java` | `liveRoomId, userId` composite | `findByLiveRoomIdAndUserId()` |
| `ShareReward.java` | `sharer_id` column | `findBySharerId()` |
| `PointTransaction.java` | `account_id` column | `findByAccountId()` |

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 50 | Live features users |
| Impact | 2 | Slow queries at scale |
| Confidence | 0.8 | Identified via repo analysis |
| Effort | 1 | Add @Index annotations |
| **RICE** | **80** | High priority |

**Remediation**:
1. Add `@Table(indexes = {...})` to entity classes
2. Create migration script for existing data
3. Verify with `EXPLAIN` query plans

---

### 7. Blocking WebClient Calls (P1)

**Category**: Concurrency
**Status**: Backlog
**Created**: 2026-05-18

**Description**:
`WeChatSubscribeService` uses `.block()` on WebClient calls, blocking threads for up to 10 seconds each.

**Impact**:
- Thread pool exhaustion under load
- Cascading failures when WeChat API is slow
- Poor scalability

**Evidence**:
```java
// service/WeChatSubscribeService.java
.getAccessToken().block(TOKEN_FETCH_TIMEOUT);  // Blocks 10s
.sendTemplateMessage().block(Duration.ofSeconds(10));  // Blocks 10s
```

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 20 | Order completion users |
| Impact | 2 | Thread exhaustion |
| Confidence | 0.8 | Confirmed via code review |
| Effort | 3 | Make async or use non-blocking |
| **RICE** | **10.7** | Medium priority |

**Remediation**:
1. Replace `.block()` with async `.retrieve().toEntity()` using `Mono`
2. Add circuit breaker pattern
3. Configure proper timeout and retry policies

---

### 8. Magic Numbers in Services (P2)

**Category**: Code Quality
**Status**: Backlog
**Created**: 2026-05-18

**Description**:
Hardcoded numeric values without explanation or constants.

**Impact**:
- Code readability issues
- Risk of typos or copy-paste errors
- Difficult to change values across the codebase

**Evidence**:
```java
// service/PointService.java:20-21
10  // Sign-in points
5   // Share points

// service/ShareService.java:25
new BigDecimal("5.00")  // Default reward amount

// service/ShareService.java:70
new BigDecimal("0.05")  // 5% reward rate

// controller/ImageUploadController.java:23
2 * 1024 * 1024  // 2MB file size
```

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 10 | Developers |
| Impact | 1 | Readability only |
| Confidence | 1.0 | Confirmed via grep |
| Effort | 1 | Extract to constants |
| **RICE** | **10** | Low priority |

**Remediation**:
1. Extract to `public static final` constants
2. Move to configuration file if values may change
3. Add JavaDoc explaining the meaning

---

### 9. No API Rate Limiting (P2)

**Category**: Security
**Status**: Backlog
**Created**: 2026-05-18

**Description**:
No rate limiting exists on any endpoint, making the system vulnerable to abuse.

**Impact**:
- DDoS vulnerability
- Resource exhaustion
- Cost overrun from API abuse

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 100 | All API users |
| Impact | 2 | Security risk |
| Confidence | 1.0 | No rate limiting found |
| Effort | 3 | Implement using Bucket4j |
| **RICE** | **66.7** | Medium priority |

**Remediation**:
1. Add rate limiting middleware using Bucket4j or similar
2. Configure per-endpoint limits based on business rules
3. Return 429 Too Many Requests with Retry-After header

---

### 10. Unbounded List Return in ProductController (P2)

**Category**: Performance
**Status**: Backlog
**Created**: 2026-05-18

**Description**:
`ProductController.getAllProducts()` returns all active products with no limit.

**Impact**:
- Memory issues with large product catalogs
- Slow response times
- Potential timeout

**Evidence**:
```java
// controller/ProductController.java:44-48
@GetMapping("/all")
public ResponseEntity<List<Product>> getAllProducts() {
    return ResponseEntity.ok(productService.findAllActive());
}
```

**RICE Scoring**:
| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 50 | Product listing users |
| Impact | 2 | Memory/performance |
| Confidence | 1.0 | Confirmed via code review |
| Effort | 1 | Add pagination |
| **RICE** | **100** | High priority |

**Remediation**:
1. Add pagination parameters
2. Return `Page<Product>` instead of `List<Product>`
3. Add default and max limit values

---

## Summary

| Priority | Count | Total Effort |
|----------|-------|--------------|
| P0 | 2 | 3 人天 |
| P1 | 5 | 8 人天 |
| P2 | 3 | 5 人天 |

---

## Recommended Processing Order

### Phase 1: Security First (Sprint 84)
| Item | Priority | Effort | Reason |
|------|----------|--------|--------|
| Missing @Valid on endpoints | P0 | 1天 | Complete validation bypass |
| Missing Input Validation | P0 | 2天 | Security risk |
| GlobalExceptionHandler | P1 | 1天 | Quick win, error consistency |

### Phase 2: Performance (Sprint 85)
| Item | Priority | Effort | Reason |
|------|----------|--------|--------|
| Missing Pagination (Admin) | P1 | 1天 | OOM risk |
| Missing Pagination (Products) | P2 | 1天 | Performance at scale |
| Missing Database Indexes | P1 | 1天 | Query performance |
| N+1 Query | P1 | 2天 | Order creation performance |

### Phase 3: Reliability (Sprint 86)
| Item | Priority | Effort | Reason |
|------|----------|--------|--------|
| Blocking WebClient | P1 | 3天 | Concurrency |
| API Rate Limiting | P2 | 3天 | Security |
| Magic Numbers | P2 | 1天 | Code quality |

---

## RICE Ranking (All Items)

| Rank | Item | RICE | Priority |
|------|------|------|----------|
| 1 | Missing @Valid on endpoints | 300 | P0 |
| 2 | Missing Input Validation | 150 | P0 |
| 3 | GlobalExceptionHandler | 100 | P1 |
| 4 | Missing Pagination (Products) | 100 | P2 |
| 5 | Missing Database Indexes | 80 | P1 |
| 6 | No API Rate Limiting | 66.7 | P2 |
| 7 | Missing Pagination (Admin) | 30 | P1 |
| 8 | N+1 Query | 30 | P1 |
| 9 | Blocking WebClient | 10.7 | P1 |
| 10 | Magic Numbers | 10 | P2 |

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

**Category**: [Security|Performance|Concurrency|Error Handling|Code Quality]
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

- [Jakarta Validation](https://jakarta.ee/specifications/validation/3.0/)
- [Spring Boot JPA Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html#data.jpa)
- [RICE Scoring Method](https://www.productplan.com/rice-scoring/)

---

## Sprint #210 验收结果 (2026-05-28)

| Issue | 标题 | 状态 | 结果 |
|-------|------|------|------|
| MIN-3955 | 技术债月报机制建立 | ✅ 部分通过 | 文件已创建，待合并到 main |
| MIN-3957 | 更新 tech-debt-backlog.md | ❌ 未通过 | 未执行，将重新执行 |

**关键发现**:
- E2E 测试发现根因：测试环境与生产环境配置不一致
- 技术债月报模板已创建：`docs/tech-debt/monthly-report-template.md`
- 2026-05 月报已创建：`docs/tech-debt/2026-05-monthly-report.md`

---

## Sprint #211 规划 (2026-05-28)

**目标**: 完成 Sprint #210 遗留工作 + 推进 P0 安全技术债

**容量分配**:
- 遗留任务: 20%
- 技术债 (P0): 15%
- 新功能: 65%

**Issue**:

| Issue | 标题 | 负责人 | 优先级 |
|-------|------|--------|--------|
| MIN-3963 | 合并技术债月报到 main | 后端架构师 | P0 |
| MIN-3964 | 更新 tech-debt-backlog.md | Orion | P1 |
| MIN-3965 | 修复 Missing @Valid 安全漏洞 | 后端架构师 | P0 |
| MIN-3966 | 修复 Missing Input Validation DTO | 后端架构师 | P0 |

**验收标准**:
1. `git show origin/main:docs/tech-debt/monthly-report-template.md` 可访问
2. `git show origin/main:docs/tech-debt/2026-05-monthly-report.md` 可访问
3. backlog.md 包含 Sprint #211 规划
4. Missing @Valid 修复方案已评审