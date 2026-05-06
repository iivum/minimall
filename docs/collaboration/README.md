# 团队协作规范总览

## 目的

本目录包含团队协作所需的所有规范文档，旨在防止 agent 声称完成但代码未提交到代码库的问题再次发生。

---

## 文档列表

| 文档 | 说明 |
|------|------|
| [code-commit-checklist.md](./code-commit-checklist.md) | 代码提交检查清单，定义每个 issue 完成前必须通过的检查项 |
| [worktree-usage-guidelines.md](./worktree-usage-guidelines.md) | Worktree 使用规范，明确 worktree 目录位置和清理规范 |
| [team-collaboration-process.md](./team-collaboration-process.md) | 团队协作流程，定义 issue 状态变更规范和验收流程 |

---

## 快速参考

### Issue 完成流程

```
1. 开始工作: multica issue status <id> in_progress
2. 执行任务: 按照 code-commit-checklist.md 检查
3. 提交代码: git add . && git commit -m "..." && git push -u origin HEAD
4. 更新状态: multica issue status <id> in_review
5. 发布评论: multica issue comment add <id> --content-stdin (说明完成情况)
```

### 检查清单速查

**代码提交验证 (G)**
- [ ] G1: git status 确认有变更
- [ ] G2: git diff 确认变更内容正确
- [ ] G3: git log 确认提交历史正常

**分支与远程验证 (R)**
- [ ] R1: 分支正确
- [ ] R2: 远程仓库已配置
- [ ] R3: 远程分支存在

**提交信息规范 (C)**
- [ ] C1: 格式 `<type>: <description>`
- [ ] C2: 描述清晰
- [ ] C3: 无敏感信息

**代码质量检查 (Q)**
- [ ] Q1: 构建通过
- [ ] Q2: 无编译警告
- [ ] Q3: 无调试代码

**测试验证 (T)**
- [ ] T1: 有单元测试
- [ ] T2: 测试通过
- [ ] T3: 覆盖率 80%+

**文档更新 (D)**
- [ ] D1: 文档已更新
- [ ] D2: README 或用户指南已更新
- [ ] D3: API 文档已更新

**CI/CD 检查 (CI)**
- [ ] CI1: CI 状态通过
- [ ] CI2: 无阻塞问题

**Issue 状态 (S)**
- [ ] S1: 验收标准满足
- [ ] S2: 状态已更新为 in_review

---

## 相关资源

- [GitHub 工作流规范](../../docs/meetings/phase12-sprint-planning-meeting.md)
- [Sprint 回顾文档](../../docs/meeting-minutes-phase12-review.md)

---

*最后更新: 2026-05-07*
*更新者: Orion*