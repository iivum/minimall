# 交付检查清单 (Delivery Checklist)

本文档定义了每次交付前必须完成的检查项，确保代码已正确合并到 `origin/main` 分支。

---

## 强制检查项 (每次交付前必须通过)

### 1. Git 状态检查

```bash
# 检查无未提交的更改
git status --porcelain

# 预期结果: 无输出（空表示所有更改已提交）
```

**通过标准**: 无 untracked files，无 modified files

---

### 2. Push 状态检查

```bash
# 检查当前分支是否有未推送的 commits
git log origin/main..HEAD --oneline

# 预期结果: 有 commits 输出（表示有已提交但未合并到 main 的内容）
```

**通过标准**: 有 commits 输出，或为空（如果确实没有需要 push 的内容）

---

### 3. 文件合并验证

```bash
# 验证关键文件是否已合并到 main 分支
git show origin/main:<file-path>

# 例如:
git show origin/main:docs/delivery-checklist.md
git show origin/main:scripts/pre-review-hook.sh
```

**通过标准**: 命令成功返回文件内容，非错误

---

### 4. 虚假交付自检

```bash
# 使用 pre-review-hook 进行自检
./scripts/pre-review-hook.sh --files <file1> <file2>

# 或跳过虚假交付检测
./scripts/pre-review-hook.sh --skip-fake-detection
```

**通过标准**: 退出码为 0

---

## 交付前检查流程

```
┌─────────────────────────────────────────────────────────────┐
│                      交付前检查流程                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. [必做] git status --porcelain                           │
│     → 有输出? 全部 add + commit 后再继续                      │
│                                                             │
│  2. [必做] git log origin/main..HEAD --oneline              │
│     → 有输出? push 到 origin 后再继续                        │
│                                                             │
│  3. [必做] git show origin/main:<file> 对每个关键文件        │
│     → 错误? 先合并到 main 分支                              │
│                                                             │
│  4. [建议] ./scripts/pre-review-hook.sh                     │
│     → 退出码非 0? 修复问题后重试                             │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 关键文件清单

每次交付必须验证以下文件是否存在于 `origin/main`:

| 文件路径 | 说明 | 验证命令 |
|----------|------|----------|
| `scripts/pre-review-hook.sh` | 预审查 Hook | `git show origin/main:scripts/pre-review-hook.sh` |
| `scripts/post-merge-hook.sh` | 后合并 Hook | `git show origin/main:scripts/post-merge-hook.sh` |
| `docs/delivery-checklist.md` | 本文档 | `git show origin/main:docs/delivery-checklist.md` |
| `docs/delivery-process.md` | 交付流程文档 | `git show origin/main:docs/delivery-process.md` |

---

## 快速验证脚本

```bash
#!/bin/bash
# delivery-check.sh - 快速交付验证

echo "=== 交付前检查 ==="

# 1. 检查 git status
if [ -n "$(git status --porcelain)" ]; then
    echo "❌ 有未提交的更改"
    exit 1
fi
echo "✅ git status 干净"

# 2. 检查 push 状态
BRANCH=$(git branch --show-current)
if git log "origin/$BRANCH"..HEAD --oneline | grep -q .; then
    echo "❌ 有未推送的 commits"
    exit 1
fi
echo "✅ 所有 commits 已推送"

# 3. 验证关键文件
for file in "docs/delivery-checklist.md" "docs/delivery-process.md"; do
    if git show "origin/main:$file" > /dev/null 2>&1; then
        echo "✅ $file 存在于 main"
    else
        echo "❌ $file 不存在于 main"
        exit 1
    fi
done

echo ""
echo "✅ 交付检查通过"
```

---

## 验收标准

- [ ] `git status --porcelain` 无输出
- [ ] `git log origin/main..HEAD` 无未推送的 commits（或已确认不需要推送）
- [ ] 所有关键文件通过 `git show origin/main:<file>` 验证
- [ ] `pre-review-hook.sh` 执行通过（退出码 0）

---

## 常见问题

### Q: `git show origin/main:<file>` 报错 "path not found"

**原因**: 文件未合并到 main 分支

**解决**:
1. 确保文件已 commit 到当前分支
2. 创建 PR 并合并到 main
3. 重新验证

### Q: `git status` 显示很多 untracked files

**原因**: 新创建的文件未添加到 git

**解决**:
```bash
git add .
git commit -m "chore: add new files"
```

### Q: `git log origin/main..HEAD` 有输出但我确定已经 push 了

**原因**: 当前分支的 upstream 设置不正确，或 remote 不可达

**解决**:
```bash
git push -u origin $(git branch --show-current)
```

---

## 相关文档

- [交付验证指南](./delivery-verification.md) - 详细的交付验证流程
- [虚假交付追踪](./fake-delivery-tracker.md) - 历史虚假交付案例
- [团队驱动验证](./team-driven-verification.md) - 团队验证机制