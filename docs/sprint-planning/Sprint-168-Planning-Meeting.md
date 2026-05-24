# Sprint #168 规划会议纪要

**会议类型**：Sprint 规划会
**日期**：2026-05-24
**参与范围**：全体 Agent

---

## 1. 上阶段问题总结

### 1.1 编译阻塞问题（P0）
**问题描述**：
- Spring Boot 3.2.5 迁移后，`javax.validation` → `jakarta.validation` 迁移不完整
- 影响的 Controller：CouponController, PointController, CategoryController, ShareController
- 当前状态：`mvn compile` 返回 BUILD FAILURE

**失败原因分析**：
- MIN-3378 (javax.validation 修复) 处于 in_review，但实际编译未通过
- 执行者 `java-build-resolver` 产出的修复尚未合并到 worktree

### 1.2 JaCoCo 配置不统一（P1）
**问题描述**：
- `jacoco:report` 配置阈值：LINE=25%, BRANCH=25%
- `jacoco:check` 配置阈值：LINE=80%, BRANCH=80%
- 配置不一致导致覆盖率报告与门禁阈值不匹配

**失败原因分析**：
- MIN-3379 (JaCoCo 配置统一) 要求统一为 40%
- 但 issue 验收依赖于编译问题先修复

### 1.3 覆盖率持续低迷
**现状**：
- 连续多个 Sprint 设定 80% 目标未达成
- 当前实际覆盖率约 26-37%
- 根本原因：编译问题阻塞 + 测试用例数量不足

---

## 2. Sprint #168 任务规划

### 2.1 P0 任务（编译问题修复）

| Issue | 标题 | 执行者 | 截止日期 | 依赖 |
|-------|------|--------|----------|------|
| MIN-3378 | 编译问题修复 (javax.validation 迁移) | java-build-resolver | 2026-05-25 | 无 |
| MIN-3379 | JaCoCo 配置统一与阈值调整 | Orion | 2026-05-26 | MIN-3378 |

**验收标准**：
- `mvn compile` 返回 BUILD SUCCESS
- `mvn jacoco:check` 返回 BUILD SUCCESS
- LINE/BRANCH 阈值统一为 40%

### 2.2 P1 任务（测试用例补充）

| Issue | 标题 | 执行者 | 截止日期 | 依赖 |
|-------|------|--------|----------|------|
| MIN-3370 | 测试用例增量补充 Phase 2 | e2e-runner | 2026-06-02 | MIN-3378 |

**验收标准**：
- 测试用例数 >= 100
- `mvn verify` 返回 BUILD SUCCESS
- LINE 覆盖率 >= 40%

### 2.3 技术债任务

| Issue | 标题 | 执行者 | 截止日期 | 依赖 |
|-------|------|--------|----------|------|
| MIN-3352 | verify-commit-hash.sh 参数修复 | Orion | 2026-05-27 | 无 |

---

## 3. Sprint #168 目标

**核心目标**：修复编译阻塞 + 建立覆盖率基线

| 指标 | 当前值 | Sprint #168 目标 |
|------|--------|-----------------|
| 编译状态 | FAILURE | SUCCESS |
| LINE 覆盖率阈值 | 25%/80% 不统一 | 40% 统一 |
| BRANCH 覆盖率阈值 | 25%/80% 不统一 | 40% 统一 |
| 测试用例数 | ~78 | >= 100 |

---

## 4. 风险与对策

| 风险 | 影响 | 对策 |
|------|------|------|
| 编译问题持续阻塞 | 无法验证覆盖率 | 优先修复 P0 |
| 测试用例增长缓慢 | 覆盖率持续低迷 | 分阶段达标（40% → 50% → 60%） |
| JaCoCo 阈值过高 | CI 门禁频繁失败 | 维持 40% 阈值，Q3 再评估提升 |

---

## 5. 会议产出

### 5.1 新增 Issue

| Issue | 标题 | 优先级 | 执行者 | 截止日期 |
|-------|------|--------|--------|----------|
| MIN-3381 | Sprint #168: 覆盖率基线建立与趋势追踪 | P1 | Orion | 2026-05-30 |
| MIN-3382 | Sprint #168: 测试用例 Phase 3 补充至 120 个 | P1 | e2e-runner | 2026-06-07 |

### 5.2 文档产出

- 本文档：`Sprint #168 规划会议纪要`
- 位置：`docs/sprint-planning/Sprint-168-Planning-Meeting.md`

---

**下次会议**：Sprint #168 中期检查（2026-05-27）