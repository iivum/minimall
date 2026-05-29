# 团队驱动验证机制

本文档记录团队驱动的交付物验证机制，确保所有 issue 交付物真实合并到 main 分支。

## 背景

Sprint #179 验收失败，所有 issue 均未真正交付到 main 分支。需要建立强制验证机制防止虚假交付再次发生。

---

## 一、重复 Issue 预防机制 (Sprint #248 新增)

### 问题描述

团队驱动 autopilot 每 15 分钟创建一次 issue，形成无限循环。无实际交付物产生。

### 根因分析

1. Autopilot 在创建新 issue 前未检查是否存在 open/in_progress 状态的团队驱动 issue
2. 即使 description 中添加了 Precondition 检查要求，也未被执行
3. 多个 Sprint 并行执行同一任务，导致重复 issue 堆积

### 预防流程

```
┌─────────────────────────────────────────────────────────────────────┐
│                    团队驱动 Issue 创建检查流程                        │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   1. [必做] 检查是否存在 open/in_progress 状态的团队驱动 issue         │
│      └─ 命令: multica issue list --status open,in_progress          │
│      └─ 筛选: title 包含 "团队驱动" 的 issue                         │
│                                                                     │
│   2. [决策] 如果存在 open/in_progress 状态的团队驱动 issue            │
│      └─ 选项 A: 贡献到已有 issue（评论加入当前 Sprint 进度）          │
│      └─ 选项 B: 关闭已有 issue，重新创建                              │
│      └─ 选项 C: 等待已有 issue 完成                                  │
│                                                                     │
│   3. [执行] 仅当没有 open/in_progress 状态的团队驱动 issue 时         │
│      └─ 才能创建新的团队驱动 issue                                    │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 验证命令

```bash
# 检查当前是否存在 open/in_progress 状态的团队驱动 issue
multica issue list --status open,in_progress --limit 50 --output json | \
  jq '.issues[] | select(.title | contains("团队驱动"))'

# 检查特定 Sprint 的团队驱动 issue 状态
multica issue list --limit 100 --output json | \
  jq '.issues[] | select(.title | test("Sprint #[0-9]+: 团队驱动"))'
```

### 重复 issue 清理流程

当发现重复的团队驱动 issue 时：

1. **识别重复**: 检查 title 相同的 issue
2. **保留最新**: 保留最近创建的 issue (用于当前 Sprint)
3. **关闭重复**: 将旧的重复 issue 状态改为 `done` 或 `cancelled`
4. **记录原因**: 在关闭的 issue 下评论说明关闭原因和保留的 issue ID

---

## 二、main 分支验证流程

### 强制验证命令

所有交付物必须通过以下命令验证存在性：

```bash
# 验证文件存在于 main 分支
git show origin/main:<file-path>

# 验证目录存在
git show origin/main:<dir-path>/.
```

### 验证检查清单

在标记 issue 为 `in_review` 前，必须满足：

- [ ] 运行 `git show origin/main:<file>` 验证文件存在
- [ ] 验证命令有实际输出（非空）
- [ ] 文件内容符合 issue 要求
- [ ] PR 已合并到 main 分支

## Sprint #179 虚假交付案例

### 问题概述

Sprint #179 期间所有 issue 均未真正交付到 main 分支。执行者声称完成任务但验证发现交付物不存在于 origin/main。

### 虚假交付特征

- Issue 标记为 `in_review` 或 `done`
- 执行者报告完成
- 但 `git show origin/main:<file>` 返回不存在

### 检测方法

```bash
# 验证文档交付物
git show origin/main:docs/team-driven-verification.md

# 验证代码交付物
git show origin/main:src/main/java/<path>

# 验证配置交付物
git show origin/main:.github/workflows/ci.yml
```

## 预防措施

### 1. Pre-review 验证（强制）

在 Agent 标记 issue 为 `in_review` 前：

1. 运行 `git show origin/main:<file>` 验证每个交付物
2. 确认命令有实际输出
3. 在 issue 下发布验证结果截图/输出

### 2. CI 验证增强

- `verify-deliverables` 必须使用 `git show origin/main:<file>` 检查
- 禁止仅依赖本地 git 状态判断交付

### 3. 结果公示

- 每个 issue 的验证结果必须发布在 issue 评论中
- 使用 `git show origin/main:<file>` 的实际输出作为证据

## 相关文档

- [虚假交付黑名单](fake-delivery-blacklist.md) - 记录已确认的虚假交付案例
- [交付物验证机制](delivery-verification.md) - CI 验证配置
- [虚假交付追踪器](fake-delivery-tracker.md) - 持续追踪虚假交付

## 更新记录

| 日期 | 更新内容 | 更新者 |
|------|---------|-------|
| 2026-05-27 | 初始创建，记录 Sprint #179 虚假交付案例和 main 分支验证流程 | Orion |
| 2026-05-30 | Sprint #248: 添加重复 Issue 预防机制（团队驱动 issue 创建前状态检查） | Orion |