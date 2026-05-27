# 交付流程 (Delivery Process)

本文档定义了整个交付生命周期中的标准流程，确保每次交付都经过充分的验证。

---

## 交付生命周期

```
┌─────────────────────────────────────────────────────────────────────┐
│                        交付生命周期                                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   1. 规划阶段                                                       │
│      └─ 任务分解 → 关键文件识别 → 验收标准定义                       │
│                                                                     │
│   2. 开发阶段                                                       │
│      └─ 代码实现 → 本地测试 → 提交到分支                             │
│                                                                     │
│   3. 交付前检查 (Pre-Delivery)                                      │
│      └─ git status → git push → 验证 main 分支 → pre-review-hook   │
│                                                                     │
│   4. PR 阶段                                                        │
│      └─ 创建 PR → 代码审查 → 合并到 main                            │
│                                                                     │
│   5. 交付后验证 (Post-Delivery)                                     │
│      └─ git show origin/main:<file> → 构建验证 → 测试验证           │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 交付前检查流程 (Pre-Delivery)

### Step 1: Git 状态检查

```bash
git status --porcelain
```

**要求**: 无未提交更改

### Step 2: Push 状态检查

```bash
git log origin/main..HEAD --oneline
```

**要求**: 所有 commits 已推送到 origin

### Step 3: 文件合并验证

对每个关键文件执行:

```bash
git show origin/main:<file-path>
```

**要求**: 所有关键文件存在于 main 分支

### Step 4: 虚假交付自检

```bash
./scripts/pre-review-hook.sh
```

**要求**: 退出码为 0

---

## PR 创建流程

### 1. PR 创建前检查

- [ ] 所有检查项通过
- [ ] 代码已提交到分支
- [ ] 分支已推送到 origin
- [ ] 关键文件已合并到 main（验证方法: `git show origin/main:<file>`）

### 2. PR 描述模板

```markdown
## 交付物清单

- [ ] 文件1: 描述
- [ ] 文件2: 描述

## 验证结果

- [ ] git show origin/main:文件1 → 通过
- [ ] git show origin/main:文件2 → 通过

## 测试结果

- [ ] mvn compile → 通过
- [ ] mvn test → 通过
```

### 3. PR 合并后

- [ ] 验证 `git log origin/main` 包含合并 commit
- [ ] 验证 `git show origin/main:<file>` 确认文件存在
- [ ] 在 issue 下发布验证结果

---

## 关键检查命令速查

| 检查项 | 命令 | 通过标准 |
|--------|------|----------|
| 未提交更改 | `git status --porcelain` | 无输出 |
| 未推送 commits | `git log origin/main..HEAD --oneline` | 无输出或已确认 |
| 文件存在 | `git show origin/main:<file>` | 显示文件内容 |
| 预审查 Hook | `./scripts/pre-review-hook.sh` | 退出码 0 |
| 构建成功 | `./mvnw compile` | BUILD SUCCESS |
| 测试通过 | `./mvnw test` | BUILD SUCCESS |

---

## 虚假交付预防机制

### 1. Worktree vs Main 区别

> ⚠️ **关键认知**: 本地 worktree 存在 ≠ main 分支存在

Agent 在 worktree 中完成修改后，必须:
1. `git add` + `git commit` → 提交到本地分支
2. `git push origin <branch>` → 推送到远程
3. 创建 PR 并合并到 main
4. **验证** `git show origin/main:<file>` → 确认 main 分支存在

### 2. 强制验证点

| 时间点 | 验证内容 |
|--------|----------|
| 交付前 | `git show origin/main:<file>` 确认存在 |
| PR 合并前 | pre-review-hook 执行通过 |
| PR 合并后 | `git log origin/main` 确认提交存在 |

### 3. 检测虚假交付

```bash
# 检查 worktree 中有但 main 中没有的文件
for file in <files>; do
    if git show "origin/main:$file" > /dev/null 2>&1; then
        echo "✅ $file in main"
    else
        echo "❌ $file NOT in main"
    fi
done
```

---

## 团队验证机制

### 评审者职责

1. **验证交付物真实性**
   - 执行 `git show origin/main:<file>` 验证
   - 检查文件内容是否完整

2. **检查测试覆盖**
   - 确认测试文件存在
   - 验证测试通过

3. **阻止虚假交付**
   - 发现文件不存在于 main 时拒绝合并
   - 记录到虚假交付追踪

### 被评审者职责

1. **自检后再请求 review**
   - 运行 pre-review-hook
   - 验证 git show 结果

2. **提供验证证据**
   - 在 PR 中包含 git show 输出
   - 说明每个文件的验证状态

---

## 验收标准

- [ ] 交付前检查全部通过
- [ ] 关键文件存在于 `origin/main`
- [ ] PR 描述包含验证结果
- [ ] pre-review-hook 退出码为 0
- [ ] 构建和测试通过

---

## 相关文档

- [交付检查清单](./delivery-checklist.md) - 详细的检查项定义
- [交付验证指南](./delivery-verification.md) - 验证流程详解
- [虚假交付追踪](./fake-delivery-tracker.md) - 历史案例记录