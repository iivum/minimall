# PR 合并验证流程 / Delivery Verification

## 概述

本文档定义 PR 合并后的验证流程，确保所有交付物正确进入 main 分支。

---

## 验证步骤

### 步骤 1: 检查 PR 是否已合并到 main

```bash
# 查看 PR 是否已合并
git log main --oneline --grep="<PR号或提交描述>"

# 或检查特定分支是否已合并
git branch -r --merged main
```

### 步骤 2: 验证提交记录

```bash
# 查看 main 分支最新提交
git log main --oneline -10

# 查看特定提交的详细内容
git log <commit-hash> -1

# 查找对应提交
git log main --oneline --author="Orion" --after="2026-05-01"
```

### 步骤 3: 验证代码变更

```bash
# 对比 agent 分支与 main 的差异
git diff main agent/sprint/xxx

# 查看具体文件的修改
git diff main -- <文件路径>

# 检查修改的文件列表
git diff main --name-only
```

---

## 验证清单

- [ ] PR 已合并到 main 分支
- [ ] `git log main --oneline` 中存在对应提交
- [ ] `git diff main agent/sprint/xxx` 显示预期的代码修改
- [ ] 所有相关文件已正确修改
- [ ] 测试文件存在且通过

---

## 示例

### 示例 1: Sprint 分支验证

```bash
# 1. 检查 sprint 分支是否已合并
git branch -r --merged main | grep sprint

# 2. 查看 main 分支的 sprint 提交
git log main --oneline --grep="sprint" -5

# 3. 对比差异
git diff main agent/sprint/121 --stat
```

### 示例 2: 完整验证流程

```bash
#!/bin/bash
# delivery-verification.sh

AGENT_BRANCH="agent/sprint/121"

echo "=== 步骤 1: 检查分支合并状态 ==="
git branch -r --merged main | grep -q "$AGENT_BRANCH" && echo "✓ 分支已合并" || echo "✗ 分支未合并"

echo ""
echo "=== 步骤 2: 验证提交记录 ==="
LATEST_COMMIT=$(git log main --oneline -1)
echo "最新提交: $LATEST_COMMIT"

echo ""
echo "=== 步骤 3: 检查代码变更 ==="
git diff main "$AGENT_BRANCH" --stat
```

---

## 验收标准

- [x] 文档路径: `minimall/docs/delivery-verification.md`
- [x] 包含检查步骤和命令
- [x] 提供验证命令示例

---

## 版本历史

| 版本 | 日期 | 修改内容 |
|------|------|----------|
| 1.0 | 2026-05-23 | 初始版本 - 建立 PR 合并验证流程 (MIN-3107) |