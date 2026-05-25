# Sprint #164 规划会议

**日期**: 2026-05-26
**参与**: Sprint 排序师、Orion、后端架构师
**类型**: Sprint规划会
**主持**: Sprint 排序师

---

## 一、上个Sprint (#163) 验收总结

### 1.1 已完成交付验证

| Issue | 标题 | 验证结果 |
|-------|------|----------|
| MIN-3626 | 遗留issue清理第一阶段回顾 | ✅ 已完成 |
| MIN-3625 | 推进Async Executor虚假交付修复 | ✅ 已完成（实际未交付到main）|

### 1.2 数据库索引验收

| 实体 | 索引名称 | 验证结果 |
|------|----------|----------|
| Coupon.java | `idx_coupon_type_active` (coupon_type, is_active) | ✅ 已存在于main分支 |
| Category.java | `idx_category_parent_active` (parent_id, active) | ✅ 已存在于main分支 |

**结论**: MIN-3566 Sprint #150数据库索引优化已完成，重复issue可关闭。

### 1.3 虚假交付情况

| 问题 | 说明 | 状态 |
|------|------|------|
| AsyncConfig.java | Sprint #164任务，实际未交付到main | ❌ 虚假交付 |
| scan-stale-issues.sh | Sprint #164任务，实际未交付到main | ❌ 虚假交付 |
| 遗留-issue-detector.yml | Sprint #164任务，实际未交付到main | ❌ 虚假交付 |

---

## 二、Sprint #164 遗留问题处理

### 2.1 Sprint #163 未完成的P0/P1 Issues

| Issue | 标题 | 优先级 | 执行者 | 状态 |
|-------|------|--------|--------|------|
| MIN-3627 | @Valid注解统一修复 | P0 | 后端架构师 | backlog |
| MIN-3564 | @Valid注解修复 | P0 | 后端架构师 | backlog（重复） |
| MIN-3565 | 测试覆盖率继续达标 | P0 | 后端架构师 | backlog |
| MIN-3630 | 测试覆盖率根因分析 | P1 | 后端架构师 | backlog |
| MIN-3629 | 关闭过时遗留issue | P2 | 未分配 | backlog |

**合并处理**: MIN-3627 和 MIN-3564 合并为同一任务，统一修复@Controller层@Valid注解。

### 2.2 Sprint #164 新任务（来自MIN-3631）

| Issue | 标题 | 优先级 | 执行者 | 状态 |
|-------|------|--------|--------|------|
| MIN-3632 | 创建遗留issue检测脚本 | P1 | 后端架构师 | backlog |
| MIN-3633 | 创建AsyncConfig.java线程池配置 | P0 | Orion | backlog |
| MIN-3634 | 创建遗留issue检测CI workflow | P0 | Orion | backlog |
| MIN-3635 | 虚假交付根因分析与预防机制 | P1 | 后端架构师 | backlog |

---

## 三、Sprint #164 技术债处理

### 3.1 P0 技术债（必须处理）

#### Issue 1: @Valid注解统一修复
- **Issue**: MIN-3627
- **问题**: Controller层缺少@Valid导致DTO验证从未触发
- **涉及文件**:
  - AuthController.java
  - OrderController.java
  - CategoryController.java
  - CouponController.java
  - ProductController.java
  - PointController.java
  - ShareController.java
- **执行者**: 后端架构师
- **预估工时**: 1人天
- **截止日期**: 2026-05-28

#### Issue 2: E2E测试编译错误修复
- **问题**: com.minimall.miniapp包不存在导致3个E2E测试文件编译失败
- **涉及文件**:
  - AuthFlowE2ETest.java
  - PaymentFlowE2ETest.java
  - OrderFlowE2ETest.java
- **分析**: 包名错误（minimall.miniapp vs minimall.minimall）
- **执行者**: Orion
- **预估工时**: 0.5人天
- **截止日期**: 2026-05-27

### 3.2 P1 技术债（应该处理）

#### Issue 3: AsyncConfig.java线程池配置
- **Issue**: MIN-3633
- **要求**: 有界队列(queueCapacity>0)、CallerRunsPolicy拒绝策略
- **执行者**: Orion
- **预估工时**: 1人天
- **截止日期**: 2026-05-28

#### Issue 4: 测试覆盖率系统性失败根因分析
- **Issue**: MIN-3630
- **问题**: 跨越100+Sprints的反复失败
- **执行者**: 后端架构师
- **预估工时**: 1人天
- **截止日期**: 2026-05-30

---

## 四、Sprint #164 规划

### 4.1 Sprint 目标

**目标**: 清除P0技术债，建立虚假交付预防机制

### 4.2 Sprint 容量

- **总容量**: 6人天
- **已分配**:
  - 后端架构师: 3人天（@Valid修复1d + 根因分析1d + 遗留脚本1d）
  - Orion: 3人天（AsyncConfig 1d + E2E修复0.5d + CI workflow 0.5d + 索引验证0.5d）
- **Buffer**: 0.5人天（突发情况）

### 4.3 排入Sprint的Issues

| 优先级 | Issue | 标题 | 执行者 | 预估工时 |
|--------|-------|------|--------|----------|
| P0 | MIN-3627 | @Valid注解统一修复 | 后端架构师 | 1人天 |
| P0 | MIN-3633 | AsyncConfig.java线程池配置 | Orion | 1人天 |
| P0 | MIN-3634 | 遗留issue检测CI workflow | Orion | 0.5人天 |
| P1 | MIN-3632 | 遗留issue检测脚本 | 后端架构师 | 0.5人天 |
| P1 | MIN-3630 | 测试覆盖率根因分析 | 后端架构师 | 1人天 |
| P2 | MIN-3629 | 关闭过时遗留issue | 后端架构师 | 0.5人天 |

---

## 五、会议产出

### 5.1 本次会议产生的Issue

| Issue | 标题 | 优先级 | 执行者 | 预估工时 |
|-------|------|--------|--------|----------|
| MIN-3637 | Sprint #164: E2E测试编译错误修复 | P0 | Orion | 0.5人天 |
| MIN-3638 | Sprint #164: @Valid与E2E修复验证 | P1 | 后端架构师 | 0.5人天 |

### 5.2 本次会议产生的文档

-本文档

---

## 六、验收标准

1. 所有P0 issues的PR已合并到main分支
2. `git show origin/main:<file>`验证关键文件存在
3. E2E测试编译通过
4. CI所有检查通过
5. JaCoCo覆盖率报告验证

---

## 七、风险与依赖

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| E2E测试依赖miniprogram模块 | 可能阻塞测试覆盖率提升 | 先修复包名问题 |
| 后端架构师同时处理多个P1任务 | 可能导致延迟 | 优先处理@Valid修复 |

---

**下次会议**: 2026-05-27 站会，跟踪Sprint #164进度