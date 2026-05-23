# 交付物验证与虚假交付检测

本文档提供验证交付物是否已正确合并到 `main` 分支的检查步骤，以及虚假交付的检测方法。

---

## 核心原则：本地存在 ≠ Main 存在

> ⚠️ **Worktree 中的文件存在 ≠ Main 分支存在**
>
> 这是虚假交付的根本原因。Agent 在 worktree 中完成了修改，但忘记/未能推送到 main 分支，导致：
> - PR 状态显示"已合并"
> - 但 main 分支实际不存在对应文件
> - 验收失败，重复多次（Phase 66, 67, 73, 74, 117, 118, 119 均出现此问题）

---

## Worktree → Main 标准流程

```
┌─────────────────────────────────────────────────────────────────┐
│                        WORKTREE 生命周期                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   1. multica repo checkout <url>                                │
│      → 创建 worktree 和分支（如 agent/technical-writer/xxx）      │
│                                                                 │
│   2. 在 worktree 中编辑文件                                      │
│      → 文件存在于本地 worktree 分支                              │
│                                                                 │
│   3. git add + git commit                                       │
│      → 提交到 worktree 分支本地仓库                              │
│                                                                 │
│   4. git push origin <branch-name>                              │
│      → 推送到远程（origin）                                      │
│                                                                 │
│   5. 创建 PR 并合并到 main                                       │
│      → PR 合并后，代码进入 main 分支                              │
│                                                                 │
│   ⚠️ 6. 验证：git show origin/main:<file>                        │
│      → 确认 main 分支真正存在该文件                               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 每一步都必须验证

| 步骤 | 验证命令 | 通过标准 |
|------|----------|----------|
| 文件编辑完成 | `ls -la <file>` | 文件存在且非空 |
| commit 完成 | `git log --oneline -1` | 显示正确的 commit 信息 |
| push 完成 | 无错误输出 | 远程分支包含 commit |
| PR 合并完成 | `gh pr list --state merged` | PR 出现在列表中 |
| **main 分支验证** | `git show origin/main:<file>` | 显示文件内容（不是错误） |

---

## Agent 自检 Checklist

在将 issue 状态更新为 `in_review` 前，必须逐项确认：

### 1. 文件存在性验证（强制）

- [ ] `git show origin/main:<file>` 确认每个声称的文件存在于 main 分支
- [ ] 使用 `test -f` 验证（非 `test -d`）
- [ ] 文件内容非空且有意义
- [ ] `git show origin/main:<file> | wc -c` 确认内容字节数 > 0

### 2. Git 状态验证

- [ ] `git status` 确认所有修改已提交
- [ ] `git log --oneline -5` 确认 commit 记录正确
- [ ] `git push origin <branch-name>` 已成功执行

### 3. 构建与测试验证

- [ ] `mvn compile` 构建成功
- [ ] `mvn test` 测试通过
- [ ] 无 merge conflict markers (`<<<<<<<`, `=======`, `>>>>>>>`)

### 4. PR 状态验证

- [ ] PR 已合并到 main 分支
- [ ] `gh pr list --state merged --base main` 能看到 PR

### 5. 验证结果发布

- [ ] 在 issue 下发布 `git show origin/main:<file>` 的实际输出
- [ ] 说明每个交付物的验证状态

---

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

---

## 检查步骤

### 1. 检查 PR 是否已合并

使用 `gh pr list` 命令查看已合并到 main 的 PR 列表：

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
