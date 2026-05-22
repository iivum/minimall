# Delivery Verification

本文档提供验证交付物是否已正确合并到 `main` 分支的检查步骤。

## 检查步骤

### 1. 检查 PR 是否已合并

使用 `gh pr list` 命令查看已合并到 main 的 PR 列表：

```bash
gh pr list --state merged --base main --limit 100
```

查找目标 PR 的标题或编号，确认其状态为 `merged`。

### 2. 检查 git log 是否有对应提交

```bash
git log origin/main --oneline
```

在输出中查找与交付物相关的提交信息（通常包含 PR 编号或功能描述）。

### 3. 验证文件是否存在

```bash
git show origin/main:<file-path>
```

例如，验证 `docs/delivery-verification.md` 是否存在：

```bash
git show origin/main:docs/delivery-verification.md
```

如果文件存在，此命令会显示文件内容；如果不存在，会报错。

## 验证命令示例

### 完整验证脚本

```bash
#!/bin/bash

# 变量定义
BRANCH_NAME="your-branch-name"
FILE_PATH="docs/delivery-verification.md"
MAIN_BRANCH="origin/main"

echo "=== Delivery Verification ==="

# 1. 检查 PR 合并状态
echo -e "\n[1] Checking PR merge status..."
PR_RESULT=$(gh pr list --state merged --base main --limit 100 | grep -i "$BRANCH_NAME" || echo "")
if [ -n "$PR_RESULT" ]; then
    echo "✓ PR found in merged list"
    echo "$PR_RESULT"
else
    echo "✗ PR not found in merged list"
fi

# 2. 检查 git log
echo -e "\n[2] Checking git log for commits..."
git log "$MAIN_BRANCH" --oneline | head -20

# 3. 验证文件存在
echo -e "\n[3] Verifying file exists in $MAIN_BRANCH..."
if git show "$MAIN_BRANCH:$FILE_PATH" > /dev/null 2>&1; then
    echo "✓ File exists: $FILE_PATH"
else
    echo "✗ File not found: $FILE_PATH"
fi
```

### 快速验证

单个文件快速验证：

```bash
# 检查文件是否存在于 main 分支
git show origin/main:docs/delivery-verification.md | head -5
```

## 验收标准

- [ ] `gh pr list --state merged` 中可找到对应 PR
- [ ] `git log origin/main --oneline` 中有对应提交记录
- [ ] `git show origin/main:<file>` 成功显示文件内容
- [ ] 文件路径和内容符合预期

## 常见问题

### PR 未显示在 merged 列表

可能原因：
- PR 尚未合并
- PR 合并到了其他分支
- 过滤条件不正确

解决方案：检查 PR 实际状态，确认合并目标分支。

### 文件不存在于 origin/main

可能原因：
- 文件尚未推送
- 合并冲突未解决
- 推送到了错误分支

解决方案：确认本地分支已正确合并并推送到远程 main 分支。
