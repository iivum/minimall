# RICE Prioritization - Technical Debt

**created**: 2026-05-18
**last updated**: 2026-05-25
**sprint capacity allocation**: 15% per sprint

---

## Overview

This document tracks the top RICE-scored technical debt items for MiniMall project.
Items are sorted by RICE score (descending) to prioritize high-impact, low-effort fixes.

---

## Top 3 Technical Debt Items

### 1. Missing @Valid on Controller Endpoints

**RICE Score**: 300
**Priority**: P0
**Status**: Backlog

| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 100 | All write API users |
| Impact | 3 | Complete validation bypass |
| Confidence | 1.0 | Confirmed via grep |
| Effort | 1 | Add @Valid to ~8 endpoints |
| **RICE** | **300** | Critical |

**Problem**:
Even when DTOs have validation annotations, `@Valid` is missing on controller methods, so validation is never triggered.

**Files Affected**:
- `AuthController.java:39`
- `OrderController.java:57`
- `CategoryController.java:38,51`
- `CouponController.java:24`
- `ProductController.java:64,70`
- `PointController.java:62,69`
- `ShareController.java:23`

**Remediation**:
1. Add `@Valid` before `@RequestBody` on all POST/PUT/DELETE endpoints
2. Verify DTOs have proper validation annotations
3. Add integration tests for validation

---

### 2. Missing Input Validation on DTOs

**RICE Score**: 150
**Priority**: P0
**Status**: Backlog

| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 100 | All API users affected |
| Impact | 3 | Security risk, data corruption |
| Confidence | 1.0 | Confirmed via code review |
| Effort | 2 | Add annotations to ~10 DTOs |
| **RICE** | **150** | High priority |

**Problem**:
DTO classes lack Jakarta Validation annotations (`@NotNull`, `@NotBlank`, `@Min`, etc.). This allows invalid data to enter the system.

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

**Remediation**:
1. Add `@NotNull`, `@NotBlank`, `@Min` annotations to all request DTOs
2. Add `@Valid` to controller methods accepting `@RequestBody`
3. Add unit tests for validation failure cases

---

### 3. GlobalExceptionHandler Incomplete

**RICE Score**: 100
**Priority**: P1
**Status**: Backlog

| Factor | Value | Rationale |
|--------|-------|-----------|
| Reach | 50 | API consumers hitting errors |
| Impact | 2 | Inconsistent error format |
| Confidence | 1.0 | Confirmed via code review |
| Effort | 1 | Add ~5 exception handlers |
| **RICE** | **100** | High priority |

**Problem**:
`GlobalExceptionHandler` doesn't handle several exception types, causing them to return generic 500 errors with potentially sensitive information.

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

**Remediation**:
1. Add handlers for `PaymentException`, `IllegalArgumentException`, `IllegalStateException`
2. Return consistent error response format
3. Log full exception server-side, return generic message to client

---

## RICE Ranking (All Items)

| Rank | Item | RICE | Priority | Effort |
|------|------|------|----------|--------|
| 1 | Missing @Valid on endpoints | 300 | P0 | 1天 |
| 2 | Missing Input Validation | 150 | P0 | 2天 |
| 3 | GlobalExceptionHandler | 100 | P1 | 1天 |
| 4 | Missing Pagination (Products) | 100 | P2 | 1天 |
| 5 | Missing Database Indexes | 80 | P1 | 1天 |
| 6 | No API Rate Limiting | 66.7 | P2 | 3天 |
| 7 | Missing Pagination (Admin) | 30 | P1 | 1天 |
| 8 | N+1 Query | 30 | P1 | 2天 |
| 9 | Blocking WebClient | 10.7 | P1 | 3天 |
| 10 | Magic Numbers | 10 | P2 | 1天 |

---

## Execution Plan

### Phase 1: Security Fixes (Sprint 84)
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

## References

- [RICE Scoring Method](https://www.productplan.com/rice-scoring/)
- [Jakarta Validation](https://jakarta.ee/specifications/validation/3.0/)
- [Spring Boot Exception Handling](https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.exception-handling)