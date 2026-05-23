# 虚假交付预防机制团队培训

## 概述

本文档为所有 Agent 提供虚假交付预防机制的完整培训，确保团队理解并能正确使用检测工具，防止类似问题再次发生。

**适用对象**：所有参与 MiniMall 项目的 Agent
**培训周期**：新 Agent 入职必读，在职 Agent 季度复训

---

## 1. 虚假交付的定义与危害

### 1.1 什么是虚假交付

**虚假交付（Fake Delivery）**是指 Agent 声称完成任务并标记 issue 为完成，但实际交付物未存在于 `main` 分支的情况。

### 1.2 虚假交付的根本原因

```
Worktree 中的文件存在 ≠ Main 分支存在
```

Agent 在 worktree 中完成了修改，但忘记/未能推送到 main 分支，导致：
- PR 状态显示"已合并"
- 但 main 分支实际不存在对应文件
- 验收失败

### 1.3 历史案例

| Sprint | 问题描述 | 责任人 | 后果 |
|--------|----------|--------|------|
| Sprint #66, #67 | 交付物未合并到 main | — | 重复返工 |
| Sprint #73, #74 | 同上 | — | 重复返工 |
| Sprint #117-#135 | @Modifying clearAutomatically 修复连续虚假交付 11 次 | 后端架构师 | F级黑名单 |
| Sprint #127-#135 | CI verify-deliverables 多次虚假交付 | Orion | D级观察 |

---

## 2. Worktree → Main 标准流程

### 2.1 完整流程图

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

### 2.2 每一步都必须验证

| 步骤 | 验证命令 | 通过标准 |
|------|----------|----------|
| 文件编辑完成 | `ls -la <file>` | 文件存在且非空 |
| commit 完成 | `git log --oneline -1` | 显示正确的 commit 信息 |
| push 完成 | 无错误输出 | 远程分支包含 commit |
| PR 合并完成 | `gh pr list --state merged` | PR 出现在列表中 |
| **main 分支验证** | `git show origin/main:<file>` | 显示文件内容（不是错误） |

### 2.3 正确流程示例

```bash
# 1. 检出仓库
multica repo checkout https://github.com/iivum/minimall.git

# 2. 在 worktree 中完成修改...

# 3. 提交修改
git add .
git commit -m "feat: description"

# 4. 推送到远程（必须！）
git push origin agent/technical-writer/xxx

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

---

## 3. detect-fake-delivery.sh 使用指南

### 3.1 脚本位置

```
scripts/detect-fake-delivery.sh
```

### 3.2 功能说明

该脚本检测 worktree 中有修改但未合并到 main 分支的情况，帮助识别虚假交付风险。

### 3.3 使用方法

```bash
# 基本检测
./scripts/detect-fake-delivery.sh

# 详细输出
./scripts/detect-fake-delivery.sh --verbose
```

### 3.4 退出码

| 退出码 | 含义 |
|--------|------|
| `0` | 无虚假交付，检测通过 |
| `1` | 发现虚假交付，需要处理 |
| `2` | 检测失败（如不是 git 仓库） |

### 3.5 输出示例

```
==========================================
虚假交付检测
执行时间: 2026-05-23 10:30:00
==========================================

=== 检测到的 Worktrees ===

  📁 /path/to/worktree
     分支: agent/technical-writer/xxx
     状态: ⚠️  发现未合并到 main 的提交
     提交记录:
       abc1234 feat: description
       def5678 fix: another change

     验证关键文件:
       ✅ docs/sprints/example.md (已存在于 main)
       ❌ src/main/java/NewService.java (未合并到 main)

==========================================
检测结果汇总
==========================================

[ERROR] 检测到 1 个疑似虚假交付

⚠️  请在 Sprint 开始前确认以上文件已合并到 main 分支
```

---

## 4. 常见错误与解决方案

### 4.1 忘记 push

**错误场景**：
```
在 worktree 中编辑文件 → git commit → 忘记 git push → PR 无法合并
```

**解决方案**：
```bash
# 检查远程分支状态
git status

# 如果有未推送的提交
git push origin <branch-name>
```

### 4.2 PR 合并到错误分支

**错误场景**：
```
在 feature-branch 完成 → PR 合并到 develop 而非 main → main 没有交付物
```

**解决方案**：
- 创建 PR 时仔细确认目标分支为 `main`
- 使用 `gh pr create --base main` 明确指定

### 4.3 合并后不验证

**错误场景**：
```
PR 合并成功 → 不执行 git show origin/main → 假设文件存在 → 验收失败
```

**解决方案**：
- 合并后立即执行验证命令
- 在 issue 下发布验证结果

### 4.4 使用 test -d 而非 test -f

**错误场景**：
```bash
# 错误 - 仅检查目录存在
test -d src/main/java

# 正确 - 检查实际文件
test -f src/main/java/com/example/Service.java
```

**解决方案**：
- CI 验证必须使用 `test -f` 检查具体文件
- 禁止使用 `test -d` 作为唯一验证手段

---

## 5. Agent 自检 Checklist

在将 issue 状态更新为 `in_review` 前，必须逐项确认：

### 5.1 文件存在性验证

- [ ] `git show origin/main:<file>` 确认每个声称的文件存在于 main 分支
- [ ] 使用 `test -f` 验证（非 `test -d`）
- [ ] 文件内容非空且有意义

### 5.2 Git 状态验证

- [ ] `git status` 确认所有修改已提交
- [ ] `git log --oneline -5` 确认 commit 记录正确
- [ ] `git push origin <branch-name>` 已成功执行

### 5.3 构建与测试验证

- [ ] `mvn compile` 构建成功
- [ ] `mvn test` 测试通过
- [ ] 无 merge conflict markers (`<<<<<<<`, `=======`, `>>>>>>>`)

### 5.4 PR 状态验证

- [ ] PR 已合并到 main 分支
- [ ] `gh pr list --state merged --base main` 能看到 PR

### 5.5 验证结果发布

- [ ] 在 issue 下发布 `git show origin/main:<file>` 的实际输出
- [ ] 说明每个交付物的验证状态

---

## 6. 惩戒机制

| 次数 | 惩戒措施 |
|------|----------|
| 第1次 | 警告 - 在 issue 中记录违规 |
| 第2次 | 暂停 - 该 Agent 暂停接新任务 24 小时 |
| 第3次 | 移除 - 该 Agent 从项目移除 |

---

## 7. 相关文档

| 文档 | 说明 |
|------|------|
| `scripts/detect-fake-delivery.sh` | 虚假交付检测脚本 |
| `docs/superpowers/code-merge-checklist.md` | 代码合并检查清单 |
| `docs/superpowers/deliverables-precheck-mechanism.md` | 交付预检机制 |
| `docs/fake-delivery-tracker.md` | 虚假交付追踪机制 |
| `docs/fake-delivery-blacklist.md` | 虚假交付黑名单 |

---

## 8. 培训验证

完成本培训后，Agent 应能够：

1. 解释什么是虚假交付及其根本原因
2. 正确执行 worktree → main 的完整流程
3. 使用 `detect-fake-delivery.sh` 检测虚假交付风险
4. 识别常见的虚假交付错误场景并知道如何避免
5. 在提交工作前完成自检 checklist
6. 理解惩戒机制并遵守交付规范

---

**版本历史**

| 版本 | 日期 | 修改内容 |
|------|------|----------|
| 1.0 | 2026-05-23 | 初始版本 (MIN-3215) |