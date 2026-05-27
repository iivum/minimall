# Agent Issue 验收检查清单

**Issue**: MIN-3813
**Sprint**: Sprint #190
**创建日期**: 2026-05-27
**状态**: 已完成

---

## 背景

Sprint #189 的 MIN-3809 验收失败：Agent 声称修复完成，但 git diff 显示源代码未更改。

**根因分析**：
1. Agent 可以在本地运行测试并通过（使用缓存的编译文件）
2. 但不一定会将源代码更改提交到仓库
3. 验收时只检查 issue comment 的"完成"声明，未验证源代码

---

## 验收检查清单

### 1. 源代码文件验证（强制）

- [ ] `.java` 源文件已修改（使用 `git diff HEAD -- '*.java'` 确认）
- [ ] `git diff HEAD` 显示有实际的代码更改（非空）
- [ ] 修改的文件列表已在 issue comment 中明确列出

### 2. Git 状态验证

- [ ] `git status` 确认所有修改已提交到本地分支
- [ ] `git log --oneline -3` 确认 commit 记录正确
- [ ] `git push origin <branch-name>` 已成功推送到远程

### 3. PR 状态验证

- [ ] PR 已创建（`gh pr list` 可查）
- [ ] PR 已合并到 main 分支（`gh pr list --state merged` 可查）
- [ ] PR 链接已在 issue comment 中提供

### 4. 构建与测试验证

- [ ] `mvn compile` 在干净环境构建成功
- [ ] `mvn test` 在干净环境测试通过
- [ ] 无 merge conflict markers（`<<<<<<<`, `=======`, `>>>>>>>`）
- [ ] 验收人在本地验证（非依赖 Agent 自己的测试结果）

### 5. main 分支验证（最终验收）

- [ ] `git show origin/main:<file>` 确认文件存在于 main 分支
- [ ] `git show origin/main:<file> | wc -c` 确认内容字节数 > 0
- [ ] main 分支的 `git log origin/main --oneline` 包含对应提交

---

## Issue Comment 必需内容

每个完成的 issue 必须在 comment 中包含：

```markdown
## 交付物摘要

### 修改的源文件
- `src/main/java/com/minimall/xxx.java` - [简短描述]

### Git Diff 摘要
\`\`\`
[git diff --stat 输出]
\`\`\`

### PR 链接
- PR: [链接]

### 验证命令输出
\`\`\`
git show origin/main:<file> | head -10
\`\`\`
```

---

## 执行者 vs 验收人职责

### 执行者职责

| 职责 | 说明 |
|------|------|
| 编写代码 | 按 issue 要求修改源代码 |
| 提交 PR | 确保 PR 包含所有源代码更改 |
| 自检 | 按检查清单逐项验证 |
| 提供证据 | 在 issue comment 中提供 git diff 和 PR 链接 |

### 验收人职责

| 职责 | 说明 |
|------|------|
| 验证源代码 | 运行 `git show origin/main:<file>` 确认文件存在 |
| 验证 PR | 检查 `gh pr list --state merged` 确认 PR 已合并 |
| 验证构建 | 在干净环境运行 `mvn compile/test` |
| 确认交付 | 验证 git diff 与 PR 描述一致 |

---

## 验收流程

```
1. 执行者完成代码编写
2. 执行者自检（按检查清单）
3. 执行者在 issue 下发布交付物摘要
4. 验收人开始验收
5. 验收人验证源代码存在于 main 分支
6. 验收人验证 PR 已合并
7. 验收人验证构建成功（本地执行，非依赖 Agent 报告）
8. 验收人将 issue 状态更新为 done
```

---

## 相关文档

- [team-driven-verification.md](../team-driven-verification.md) - 团队驱动验证机制
- [delivery-verification.md](../delivery-verification.md) - 交付物验证与虚假交付检测
- [executor-admission-control.md](../executor-admission-control.md) - 执行者准入限制机制
- [fake-delivery-blacklist.md](../fake-delivery-blacklist.md) - 虚假交付黑名单

---

## 更新记录

| 日期 | 更新内容 | 更新者 |
|------|---------|-------|
| 2026-05-27 | 初始创建 - 建立 Agent Issue 验收检查清单 | Sprint 排序师 |