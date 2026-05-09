# 分支生命周期管理流程

## 概述

本文档定义了 Minimall 项目中 Agent 分支的生命周期管理机制，确保代码存在性和代码库健康。

## 分支类型

| 分支类型 | 命名规则 | 合并目标 | 保留策略 |
|---------|---------|---------|---------|
| main | `main` | - | 永久保留 |
| Agent 分支 | `agent/agent/<id>` | main | 合并后可清理 |
| Feature 分支 | `feature/*` | main | 合并后可清理 |
| Release 分支 | `release/*` | main | 永久保留 |

## 生命周期阶段

```
[创建] → [开发] → [合并评审] → [合并] → [归档清理]
```

### 1. 创建阶段

- Agent 分支由 `multica repo checkout` 自动创建
- 分支命名格式: `agent/agent/<issue-id>` (如 `agent/agent/002dea7b`)
- 分支应尽快与 main 保持同步，避免严重偏离

### 2. 开发阶段

- 在分支上进行开发工作
- 定期从 main 拉取更新 (`git pull --rebase origin main`)
- 保持提交整洁，提交信息遵循规范

### 3. 合并评审阶段

- 提交 PR/MR 到 main
- 通过代码评审
- 确保 CI 检查通过

### 4. 合并阶段

- 使用 Squash Merge 合并到 main
- 确保合并后 main 包含所有相关更改

### 5. 归档清理阶段

- 已合并的 Agent 分支可以安全删除
- 使用以下脚本识别可清理的分支

## 识别已合并分支

### 脚本: `scripts/git-branch-lifecycle.sh`

```bash
#!/bin/bash
# 识别可以清理的已合并 Agent 分支

set -euo pipefail

TARGET_BRANCH="${1:-main}"
PROTECTED_PATTERNS="main|master|release|feature|hotfix"

echo "=== 已合并到 ${TARGET_BRANCH} 的 Agent 分支 ==="
echo ""

git fetch origin "${TARGET_BRANCH}" --quiet

for branch in $(git branch -r --merged "origin/${TARGET_BRANCH}" | grep -E '^  origin/agent/' | sed 's/^  origin\///'); do
    # 检查是否为保护分支
    if echo "$branch" | grep -qE "^origin/(${PROTECTED_PATTERNS})"; then
        continue
    fi

    # 获取分支最后提交时间
    last_commit=$(git log -1 --format="%ai" "$branch" --since="30 days ago" 2>/dev/null || echo "old")

    echo "- $branch (最后提交: $last_commit)"
    echo "  删除命令: git push origin --delete ${branch#origin/}"
done

echo ""
echo "=== 未合并的 Agent 分支 ==="
echo ""

for branch in $(git branch -r --no-merged "origin/${TARGET_BRANCH}" | grep -E '^  origin/agent/' | sed 's/^  origin\///'); do
    echo "- $branch"
done
```

### 使用方法

```bash
# 查看可清理的分支
./scripts/git-branch-lifecycle.sh

# 指定目标分支
./scripts/git-branch-lifecycle.sh main

# 查看更详细的分支信息
./scripts/git-branch-lifecycle.sh --detailed
```

## 清理流程

### 手动清理

```bash
# 1. 确认分支已合并
git branch -r --merged origin/main | grep agent/

# 2. 删除本地分支
git branch -d local-branch-name

# 3. 删除远程分支
git push origin --delete remote-branch-name
```

### 自动清理 (待实现)

未来可在 CI/CD 中添加自动清理任务：

```yaml
# .github/workflows/branch-cleanup.yml
name: Branch Cleanup

on:
  schedule:
    - cron: '0 2 * * 0'  # 每周日凌晨2点

jobs:
  cleanup:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Cleanup merged branches
        run: |
          for branch in $(git branch -r --merged origin/main | grep 'agent/'); do
            echo "Would delete: $branch"
          done
```

## CI/CD 分支状态检查

在 PR 创建时检查分支状态：

```yaml
# .github/workflows/branch-check.yml
name: Branch Status Check

on:
  pull_request:
    branches: [main]

jobs:
  check:
    runs-on: ubuntu-latest
    steps:
      - name: Check branch age
        run: |
          branch_age=$(git log -1 --format="%ai" HEAD --since="90 days ago")
          if [ -z "$branch_age" ]; then
            echo "Branch is older than 90 days - consider merging or closing"
          fi

      - name: Check divergence
        run: |
          main_commit=$(git rev-parse origin/main)
          branch_commit=$(git rev-parse HEAD)
          common_commit=$(git merge-base HEAD origin/main)
          if [ "$branch_commit" != "$main_commit" ]; then
            echo "Branch has diverged from main - rebase recommended"
          fi
```

## 分支保护规则

建议在 GitHub/GitLab 设置以下分支保护：

| 规则 | 说明 |
|-----|------|
| 禁止强制推送 | main 分支禁止 force push |
| 线性历史 | main 保持线性历史 |
| 状态检查要求 | PR 必须通过 CI 才能合并 |
| 管理员例外 | 可为维护者设置例外 |

## 监控指标

| 指标 | 目标 | 说明 |
|-----|------|-----|
| Agent 分支总数 | < 100 | 过多表明清理不及时 |
| 超过90天未合并的分支 | < 10 | 长期未合并需要处理 |
| 平均分支生命周期 | < 14 天 | 从创建到合并的时间 |

## 相关文档

- [开发工作流](../development-workflow.md)
- [代码评审标准](../code-review.md)
- [Git 工作流程](../git-workflow.md)