# PR 合并验证流程

## 目的

确保每次 PR 合并都经过充分的验证，避免将未测试或有问题 的代码合并到 main 分支。

## PR 合并检查清单

### 合并前检查

- [ ] PR 已通过所有 CI 检查
- [ ] 至少 1 人 review 通过
- [ ] 所有 conversation 已解决
- [ ] 分支是最新的（已 rebase/main）
- [ ] 测试覆盖率未下降
- [ ] 无 merge conflict

### 代码质量检查

- [ ] 无 hardcoded secrets
- [ ] 无 console.log/debug 语句
- [ ] 新功能有对应的测试
- [ ] 文档已更新（如需要）

### 合并后检查

- [ ] main 分支 CI 仍然通过
- [ ] 功能测试通过
- [ ] 无回归问题

## 验证命令示例

### 本地验证

```bash
# 检查分支状态
git status

# 拉取最新 main
git fetch origin main

# 检查 diff
git diff origin/main...HEAD

# 运行测试
npm test

# 运行 lint
npm run lint
```

### CI 验证

```bash
# 触发 GitHub Actions
gh workflow run ci.yml --ref feature-branch

# 检查 Actions 状态
gh run list --workflow=ci.yml
```

### 合并后验证

```bash
# 确认 main 分支正常
git checkout main
git pull origin main
npm test
git log --oneline -5
```

## 流程图

```
┌─────────────┐
│  创建 PR    │
└──────┬──────┘
       │
       ▼
┌─────────────┐     否
│ CI 通过？   │────────► 修复问题
└──────┬──────┘
       │ 是
       ▼
┌─────────────┐     否
│ Review 通过？│────────► 等待/修改
└──────┬──────┘
       │ 是
       ▼
┌─────────────┐
│  合并到 main │
└──────┬──────┘
       │
       ▼
┌─────────────┐     否
│ Main CI 通过？│──────► 回滚/修复
└─────────────┘
```

## 回滚流程

如发现合并后问题：

```bash
# 找到最后一个正常 commit
git log --oneline -10

# 创建回滚 commit
git revert <bad-commit-hash>
git push origin main
```

## 相关文档

- [Tech Debt Backlog](../tech-debt-backlog.md)
- [Sprint 流程](../meetings/sprint-34-report.md)