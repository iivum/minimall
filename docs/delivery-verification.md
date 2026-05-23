# 交付物验证与虚假交付检测

本文档提供验证交付物是否已正确合并到 `main` 分支的检查步骤，以及虚假交付的检测方法。

## 虚假交付检测流程

### 1. PR 描述与实际交付物对比

虚假交付的典型特征：
- PR 描述声称完成了某功能
- 但代码中缺少相应的实现文件
- 或文件存在但内容为空/无意义

### 2. 关键检测步骤

#### 2.1 文件存在性验证（强制）

使用 `git show origin/main:<file>` 验证文件是否实际存在于 main 分支：

```bash
# 验证特定文件
git show origin/main:src/main/java/com/example/Service.java

# 验证目录下的文件数量
git show origin/main:src/main/java | grep -c "\.java$"

# 批量验证（检查关键文件列表）
for file in "pom.xml" "src/main/java/..." "src/test/java/..."; do
  if git show "origin/main:$file" > /dev/null 2>&1; then
    echo "✓ $file exists in main"
  else
    echo "✗ $file NOT found in main"
  fi
done
```

#### 2.2 Git log 与 PR 对比验证

```bash
# 查看 main 分支的最近提交
git log origin/main --oneline -20

# 查看某个 PR 的合并状态
gh pr list --state merged --base main --limit 100 | grep "PR编号或标题"

# 对比 PR 创建时间和实际提交时间
gh pr view PR编号 --json createdAt,mergedAt,title
```

#### 2.3 测试文件与构建产物验证

真实的 Java 项目交付应该包含：
- 单元测试文件 `*Test.java`
- 测试报告存在于 `target/surefire-reports/`

```bash
# 验证测试文件存在
find src/test/java -name '*Test.java' | wc -l

# 验证测试报告存在
ls -la target/surefire-reports/*.txt

# 验证编译产物
find target/classes -name '*.class' | wc -l
```

### 3. CI 验证机制

CI 的 `verify-deliverables` job 会自动检查：

1. **文件存在性检查** - 使用 `test -f` 验证关键文件
2. **目录存在性检查** - 辅助验证
3. **测试文件数量检查** - 确保存在测试文件
4. **构建产物检查** - 验证编译成功

### 4. Post-merge 验证清单

PR 合并后，PR 作者必须在 24 小时内完成以下验证：

- [ ] `git show origin/main:<file>` 确认关键文件存在于 main 分支
- [ ] `git log origin/main --oneline` 确认提交记录存在
- [ ] 构建成功且测试通过
- [ ] 在 issue 下发布验证结果

### 5. 常见虚假交付特征

| 特征 | 说明 | 检测方法 |
|------|------|----------|
| PR 已合并但文件不存在 | Agent 声称完成但实际未提交 | `git show origin/main:<file>` |
| 文件存在但内容为空 | 创建了空文件或无意义内容 | `git show origin/main:<file> \| wc -c` |
| 目录存在但无文件 | 只有目录，没有实际源文件 | `find src -name '*.java' \| wc -l` |
| 测试文件不存在 | 声称有测试但实际没有 | `find src/test -name '*Test.java'` |
| 构建产物不存在 | 声称编译成功但 target 为空 | `ls target/classes/*.class` |
| **Worktree 未推送** | 本地有修改但未推送到远程 | `git log origin/main..HEAD` |

---

## 验证脚本使用指南

### verify-commit-hash.sh

验证指定 commit 是否已合并到 `origin/main`：

```bash
# 基本用法 - 仅验证 commit 存在性
./scripts/verify-commit-hash.sh <commit-hash>

# 增强检测 - 验证 commit 且检查 worktree 未推送情况
./scripts/verify-commit-hash.sh <commit-hash> --check-worktree

# 示例
./scripts/verify-commit-hash.sh abc1234
./scripts/verify-commit-hash.sh abc1234 --check-worktree
```

**返回值：**
- `0` - commit 存在且无虚假交付
- `1` - commit 不存在或检测到虚假交付
- `2` - 检测失败（remote 不存在等）

**增强检测场景：**
- 检测当前分支是否有未推送的提交
- 显示未推送的 commit 列表
- 提醒 Agent 先推送再进行验证

### detect-fake-delivery.sh

批量检测所有 worktree 的虚假交付情况：

```bash
./scripts/detect-fake-delivery.sh           # 基本检测
./scripts/detect-fake-delivery.sh --verbose  # 详细输出
```

### post-merge-hook.sh

工作树一致性检查钩子，可在 git merge 后自动执行：

```bash
# 安装方法
cp scripts/post-merge-hook.sh .git/hooks/post-merge
chmod +x .git/hooks/post-merge
```

**检测内容：**
- origin/main 是否有新提交
- 当前分支是否有未推送的提交
- 未推送的 commit 列表

---

## 检测流程图

```
┌─────────────────────────────────────────────────────────────┐
│                      Agent 标记 in_review                    │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│ 1. verify-commit-hash.sh --check-worktree <commit-hash>    │
│    - 验证 commit 是否存在于 origin/main                     │
│    - 检测 worktree 是否有未推送的提交                        │
└──────────────────────────────┬──────────────────────────────┘
                               │
              ┌────────────────┴────────────────┐
              │                                 │
              ▼                                 ▼
        ✓ 验证通过                           ✗ 验证失败
              │                                 │
              ▼                                 ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. detect-fake-delivery.sh --verbose                        │
│    - 批量检测所有 worktree                                  │
│    - 检查文件是否存在于 origin/main                         │
└──────────────────────────────┬──────────────────────────────┘
                               │
              ┌────────────────┴────────────────┐
              │                                 │
              ▼                                 ▼
        ✓ 无虚假交付                         ✗ 发现虚假交付
              │                                 │
              ▼                                 ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. Post-merge hook (自动触发)                               │
│    - 检测 main 分支更新                                     │
│    - 检测未推送提交                                         │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │  最终验收决定        │
                    └─────────────────────┘
```

---

## 验收标准

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
