# PR 合并验证流程

## 背景

Sprint #117、Sprint #118 验收失败的根本原因是：**修改在 agent worktree，未实际合并到 main 分支**。

本流程用于每次 PR 合并后验证代码已真正到达 main 分支。

## 验收检查项

每次合并 PR 前，必须确认以下三项全部通过：

| 检查项 | 说明 |
|--------|------|
| 代码修改在 main 分支 | 本地 main 分支包含本次提交的改动 |
| PR 已合并 | GitHub PR 状态为 `merged` |
| git log 有对应提交 | `main` 分支的 git log 中可看到本次提交 |

## 检查命令

### 1. 确认本地 main 分支是最新的

```bash
git checkout main
git pull origin main
```

### 2. 确认提交已在 main 分支上

```bash
git log main --oneline -10
# 或搜索特定关键词
git log main --oneline --grep="你的提交关键词"
```

### 3. 确认 PR 已合并

在 GitHub PR 页面确认状态为 `Merged`。

### 4. 检查 worktree 与 main 的差异

如果从 worktree 合并，需确认 worktree 的修改已推送：

```bash
# 查看 worktree 列表
git worktree list

# 对比 worktree 分支与 main 的差异
git diff agent/xxx/main...main
```

## 合入后验证清单

- [ ] PR 状态为 `Merged`
- [ ] `git log origin/main --oneline` 包含本次合入的 commit
- [ ] 若有关联的 worktree 分支，确认已删除或已合并

## 常见失败原因

1. **修改在 worktree 未推送** — worktree 分支的提交未合并到 main 就结束任务
2. **PR 未合并** — 以为 PR 已合但实际是 draft 或未合并
3. **合并冲突未解决** — PR 显示 merged 但 main 实际未更新

## 相关文档

- [Sprint #117 验收失败复盘](./meeting-minutes/sprint-117-retro.md)（待建立）
- [Sprint #118 验收失败复盘](./meeting-minutes/sprint-118-retro.md)（待建立）