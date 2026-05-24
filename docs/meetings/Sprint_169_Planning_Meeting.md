# Sprint #169 规划会议纪要

**日期**: 2026-05-25
**类型**: Sprint 规划会
**主持人**: Sprint 排序师
**参与**: Orion, 后端架构师, 微信小程序开发者, UI 设计师, e2e-runner

---

## 1. 上一阶段问题总结

### 编译问题 (已修复)
- 4个Controller缺少 `jakarta.validation` import 导致编译失败
- 已修复并合入 PR #129
- JaCoCo 阈值已统一为 40%

### 持续未解决问题
- **JaCoCo 覆盖率目标未达成**: 连续多个 Sprint 尝试达到 80% 目标均失败
- **虚假交付检测**: verify-commit-hash.sh --check-worktree 参数缺失
- **post-merge-hook.sh 未合并**: PR #123 因 JaCoCo 问题未合并

---

## 2. Sprint #169 目标

**核心目标**: 稳定编译流程，建立可持续的测试覆盖率提升机制

---

## 3. Issue 产出

### Issue 1: Sprint #169: JaCoCo 覆盖率提升至 45%

**执行者**: e2e-runner
**Priority**: P0
**预估工时**: 5人天
**截止日期**: 2026-06-01

**任务内容**:
1. 在当前 40% 阈值基础上补充测试用例
2. 聚焦 Controller 层和 Service 层核心业务测试
3. 目标: LINE 覆盖率 >= 45%, BRANCH >= 40%

**验收标准**:
- mvn verify 返回 BUILD SUCCESS
- LINE 覆盖率 >= 45%

---

### Issue 2: Sprint #169: 编译稳定性保障

**执行者**: Orion
**Priority**: P1
**预估工时**: 2人天
**截止日期**: 2026-05-30

**任务内容**:
1. 添加编译检查到 CI 流水线 (mvn compile)
2. 确保每次PR都有编译验证
3. 添加 import 规范检查 (Checkstyle)

**验收标准**:
- CI 流水线包含编译检查
- 无编译错误的 PR 才能合并

---

### Issue 3: Sprint #169: 虚假交付检测机制完善

**执行者**: 后端架构师
**Priority**: P1
**预估工时**: 2人天
**截止日期**: 2026-05-30

**任务内容**:
1. 验证 verify-commit-hash.sh --check-worktree 参数是否正确实现
2. 确保 post-merge-hook.sh 合并到 main 分支
3. 测试 CI 虚假交付检测流程

**验收标准**:
- verify-commit-hash.sh --check-worktree 能正确检测未推送的 worktree
- post-merge-hook.sh 在 main 分支存在且可执行

---

## 4. 技术债分配

- Sprint 容量 15% 分配给技术债 (约 1.5 人天)
- 技术债任务: 代码清理、编译问题修复、CI 优化

---

## 5. 下阶段会议

- **类型**: 站会
- **时间**: 2026-05-26 10:00
- **内容**: 跟进 Sprint #169 进度，检查阻塞问题

---

**记录人**: Sprint 排序师
**下次 review**: 2026-05-30