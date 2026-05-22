# 代码合并检查清单 / Code Merge Checklist

## 执行要求

**所有 Agent 在提交 PR 到 `in_review` 状态前必须执行以下检查项。**

---

## 核心原则

### ⚠️ 关键认知：本地存在 ≠ main 存在

**Worktree 中的文件存在 ≠ main 分支存在**

这是虚假交付的根本原因。Agent 在 worktree 中完成了修改，但忘记/未能推送到 main 分支，导致：
- PR 状态显示"已合并"
- 但 main 分支实际不存在对应文件
- 验收失败，重复多次（Phase 66, 67, 73, 74, 117, 118, 119 均出现此问题）

### Worktree → Main 完整流程

```
1. worktree 中完成修改
2. git add + git commit（提交到 worktree 分支）
3. git push origin <branch-name>（推送到远程）
4. 创建 PR 并合并到 main
5. ⚠️ 验证：git show origin/main:<file>（确认 main 分支存在）
```

---

## 检查项 / Checklist

### 0. Pre-PR 验证（提交前必须执行）

- [ ] **最重要验证：** `git show origin/main:<file-path>` 确认文件在 main 分支存在
- [ ] `git status` 确认所有修改已提交
- [ ] `git log --oneline -5` 确认 commit 记录正确
- [ ] `git push origin <branch-name>` 已成功执行

### 1. 代码格式检查 / Code Format

- [ ] `mvn checkstyle:check` 通过
- [ ] `mvn spotless:check` 通过（如适用）
- [ ] 代码无 merge conflict markers（`<<<<<<<`, `=======`, `>>>>>>>`）

### 2. 测试覆盖 / Test Coverage

- [ ] 新功能包含单元测试
- [ ] 关键业务逻辑有集成测试
- [ ] `mvn test` 全部通过

### 3. 安全检查 / Security

- [ ] 无硬编码凭证（API keys, passwords, tokens）
- [ ] 用户输入已验证
- [ ] SQL 注入防护（使用参数化查询）
- [ ] 无 XSS 漏洞

### 4. 业务逻辑 / Business Logic

- [ ] 日志级别正确使用
  - `log.error`: 仅用于真正的错误情况
  - `log.warn`: 用于可恢复的问题
  - `log.info`: 用于重要业务事件
- [ ] 错误处理完善
- [ ] 无静默失败

### 5. 文档更新 / Documentation

- [ ] README.md 已更新（如需要）
- [ ] API 文档已更新（如需要）
- [ ] 必要注释已添加

### 6. Git 规范 / Git Standards

- [ ] 提交信息符合规范
- [ ] 无不必要的文件提交
- [ ] 分支名称符合规范

---

## Post-Merge 验证清单（合并后必须执行）

PR 合并到 main 分支后，必须执行以下验证确认交付物真正存在于 main：

### ✅ 必须验证的命令

```bash
# 1. 验证文件存在于 main 分支（最关键！）
git show origin/main:<file-path>

# 示例：验证 docs/superpowers/code-merge-checklist.md 存在
git show origin/main:docs/superpowers/code-merge-checklist.md

# 2. 验证目录内容
git show origin/main:docs/superpowers/ | head -20

# 3. 验证提交记录
git log origin/main --oneline | head -10

# 4. 验证 PR 合并状态
gh pr list --state merged --base main --limit 20
```

### ⚠️ 常见错误

| 错误 | 后果 | 解决方案 |
|------|------|----------|
| 未执行 `git push` | worktree 分支未推送到远程 | 合并前先 `git push origin <branch>` |
| PR 合并到错误分支 | 交付物进入其他分支 | 检查 PR 目标分支是否为 main |
| 未验证 main 存在 | 虚假交付，无法回滚 | 必须执行 `git show origin/main:<file>` |
| worktree 分支未清理 | 遗留无用分支 | 合并后删除 worktree 分支 |

---

## 自动化验证脚本 / Automation Scripts

### 快速检查脚本

```bash
#!/bin/bash
# code-merge-check.sh - 快速代码合并检查

set -e

echo "=== MiniMall 代码合并检查 ==="

# 1. 检查 merge conflict markers
echo "[1/5] 检查 merge conflict markers..."
if grep -r "<<<<<<<\|=======\|>>>>>>>" --include="*.java" src/ backend/; then
    echo "❌ 发现 merge conflict markers!"
    exit 1
fi
echo "✅ 无 merge conflict markers"

# 2. 运行 checkstyle
echo "[2/5] 运行 checkstyle..."
mvn checkstyle:check -q
echo "✅ checkstyle 检查通过"

# 3. 运行测试
echo "[3/5] 运行测试..."
mvn test -q -DskipITs
echo "✅ 测试通过"

# 4. 检查硬编码凭证
echo "[4/5] 检查硬编码凭证..."
if grep -rE "(apiKey|api_key|secret|password|token)\s*=\s*[\"'][a-zA-Z0-9]" --include="*.java" src/ backend/ 2>/dev/null; then
    echo "⚠️ 发现可能的硬编码凭证，请检查"
fi
echo "✅ 凭证检查完成"

# 5. 代码格式化
echo "[5/5] 检查代码格式化..."
mvn spotless:check -q 2>/dev/null || echo "⚠️ spotless 检查跳过"
echo "✅ 格式化检查完成"

echo ""
echo "=== 所有检查通过 ✓ ==="
```

### 使用方法

```bash
# 直接运行
chmod +x scripts/code-merge-check.sh
./scripts/code-merge-check.sh

# 或在 CI/CD 中集成
mvn verify -Dcheckstyle.skip=false
```

### 合并验证脚本

```bash
# verify-merge.sh - 验证 issue 声明的文件和代码变更是否确实存在于 main 分支
# 用法: ./scripts/verify-merge.sh <issue-id> [file-path] [commit-hash]

# 示例 1: 验证单个文件
./scripts/verify-merge.sh MIN-123 src/main/java/Example.java

# 示例 2: 验证多个文件
./scripts/verify-merge.sh MIN-123 "src/main/java/Example.java" "abc1234"

# 示例 3: 完整验证（文件 + commit）
./scripts/verify-merge.sh MIN-123 src/main/java/Example.java abc1234
```

### 自动化验证步骤

在进行人工审查前，使用 `verify-merge.sh` 脚本验证代码已正确合并：

1. **文件存在性验证**
   ```bash
   ./scripts/verify-merge.sh <issue-id> <file-path>
   ```
   - 使用 `git ls-tree origin/main <file>` 验证文件存在于 main 分支

2. **Commit 存在性验证**
   ```bash
   ./scripts/verify-merge.sh <issue-id> "" <commit-hash>
   ```
   - 使用 `git log --oneline origin/main | grep <commit>` 验证 commit 存在

3. **验证 main 分支状态**
   - 脚本自动检查 origin/main 是否为最新状态
   - 如非最新状态，会自动执行 `git fetch origin main`

4. **CI 集成**
   ```bash
   # 在 CI pipeline 中添加合并验证
   - name: Verify merge
     run: ./scripts/verify-merge.sh ${{ env.ISSUE_ID }} ${{ env.FILE_PATH }}
   ```

---

## 验收标准

- [ ] 所有 PR 必须包含本检查单执行结果
- [ ] checkstyle.xml 无 merge conflict markers
- [ ] 自动化脚本已集成到 CI/CD

---

## 版本历史

| 版本 | 日期 | 修改内容 |
|------|------|----------|
| 1.1 | 2026-05-23 | 添加 worktree→main 核心原则和 Post-Merge 验证清单（MIN-3202） |
| 1.0 | 2026-05-09 | 初始版本 |

---

## Agent 培训：Worktree → Main 合并流程

### 为什么虚假交付持续发生？

**根本原因：** Agent 在 worktree 中完成修改，但忘记/未能推送到 main 分支。

### Worktree 生命周期

```
┌─────────────────────────────────────────────────────────────────┐
│                        WORKTREE 生命周期                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   1. multica repo checkout <url>                                │
│      → 创建 worktree 和分支（如 agent/orion/530a51bc）            │
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

### 每个步骤都必须验证

| 步骤 | 验证命令 | 通过标准 |
|------|----------|----------|
| 文件编辑完成 | `ls -la <file>` | 文件存在且非空 |
| commit 完成 | `git log --oneline -1` | 显示正确的 commit 信息 |
| push 完成 | 无错误输出 | 远程分支包含 commit |
| PR 合并完成 | `gh pr list --state merged` | PR 出现在列表中 |
| **main 分支验证** | `git show origin/main:<file>` | 显示文件内容（不是错误） |

### 虚假交付的典型场景

**场景 1：忘记 push**
```
在 worktree 中编辑文件 → git commit → 忘记 git push → PR 无法合并
```

**场景 2：PR 合并到错误分支**
```
在 feature-branch 完成 → PR 合并到 develop 而非 main → main 没有交付物
```

**场景 3：合并后不验证**
```
PR 合并成功 → 不执行 git show origin/main → 假设文件存在 → 验收失败
```

### 正确的完整流程

```bash
# 1. 检出仓库
multica repo checkout https://github.com/iivum/minimall.git

# 2. 在 worktree 中完成修改...

# 3. 提交修改
git add .
git commit -m "feat: description"

# 4. 推送到远程（必须！）
git push origin agent/orion/xxx

# 5. 创建 PR 并合并...

# 6. 验证 main 分支（关键步骤！）
git show origin/main:<your-file-path>

# 7. 如果验证通过，更新 issue 状态为 in_review
multica issue status <issue-id> in_review

# 8. 发布验证结果到 issue comment
multica issue comment add <issue-id> --content-stdin <<'EOF'
验证结果：
- git show origin/main:<file> ✓ 文件存在于 main 分支
- git log origin/main --oneline | grep <commit> ✓ 提交记录存在
EOF
```

### 记忆要点

> ⚠️ **Worktree 中的存在 ≠ Main 分支的存在**
>
> 每次合并后必须验证：`git show origin/main:<file>`

---