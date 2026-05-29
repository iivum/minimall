# 虚假交付追踪机制

本文档定义了虚假交付的检测、追踪和预防机制。

## 1. 背景

Sprint #117 至 Sprint #135 期间，连续多次出现 Agent 虚假交付问题。根本原因：Agent 在 worktree 中完成修改，但忘记/未能推送到 main 分支。

## 2. 追踪机制

### 2.1 虚假交付追踪 Dashboard

Grafana Dashboard：`fake-delivery-tracker`

**功能**：
- 虚假交付总数统计
- 涉及 Agent 数统计
- 涉及 Sprint 数统计
- 各 Sprint 虚假交付数量和比率
- 各 Agent 虚假交付数量
- Agent 评分表
- 虚假交付黑名单

**访问路径**：`docs/monitoring/fake-delivery-dashboard.json`

### 2.2 自动化检测脚本

检测脚本：`scripts/detect-fake-delivery.sh`

**功能**：
- 检测所有 worktree 中未合并到 main 的修改
- 验证关键文件是否存在于 main 分支
- 输出详细的检测报告

**使用方式**：

```bash
# 基本检测
./scripts/detect-fake-delivery.sh

# 详细输出
./scripts/detect-fake-delivery.sh --verbose
```

**退出码**：
- `0` - 无虚假交付
- `1` - 发现虚假交付
- `2` - 检测失败（不是 git 仓库等）

### 2.3 检测规则

| 检测项 | 说明 | 通过条件 |
|--------|------|----------|
| 文件存在性 | 关键文件是否存在于 main 分支 | `git ls-tree origin/main <file>` 成功 |
| 提交记录 | worktree 的提交是否已合并 | `git log origin/main..HEAD` 为空 |
| 未推送修改 | 是否存在未推送的提交 | local hash == remote hash |

## 3. Sprint 开始前自动检测

### 3.1 检测时机

- 每个 Sprint 开始前自动运行
- 由 CI/CD pipeline 或 autopilot 触发

### 3.2 检测流程

```
1. 拉取最新 main 分支
2. 执行 detect-fake-delivery.sh
3. 检查退出码
   - 退出码 0：继续执行 Sprint
   - 退出码 1：报告虚假交付，暂停 Sprint
   - 退出码 2：检测失败，人工介入
4. 生成检测报告并发布到 issue
```

## 4. 虚假交付积分制度

### 4.1 评分维度

| 维度 | 权重 | 说明 |
|------|------|------|
| 交付真实性 | 40% | 交付物是否真实存在 |
| 交付完整性 | 30% | 交付物是否完整 |
| 合规性 | 30% | 是否遵守交付流程 |

### 4.2 等级划分

| 等级 | 分数范围 | 措施 |
|------|----------|------|
| A | 80-100 | 优先分配任务 |
| B | 60-79 | 正常分配任务 |
| C | 40-59 | 观察，限制任务数 |
| D | 30-39 | 限制 1 个任务，24 小时冷却期 |
| F | 0-29 | 黑名单，禁止参与 |

### 4.3 扣分规则

| 违规类型 | 扣分 |
|----------|------|
| 文件未合并到 main | -20/次 |
| 提交描述与实际不符 | -10/次 |
| 缺失测试文件 | -5/次 |
| CI 检查失败 | -5/次 |

## 5. 预防措施

### 5.1 Pre-review 验证

在标记 issue 为 `in_review` 前，必须确认：

- [ ] 所有声称创建的文件已通过 `git show origin/main:<file>` 验证
- [ ] 构建成功 (`mvn compile`)
- [ ] 测试通过 (`mvn test`)
- [ ] CI 所有检查通过
- [ ] PR 已合并到 main 分支

### 5.2 Post-merge 验证

PR 合并后，PR 作者必须在 24 小时内验证：

```bash
# 验证文件存在于 main
git show origin/main:<file>

# 验证提交记录
git log origin/main --oneline | head -10
```

### 5.3 CI 强制检查

- `verify-deliverables` 使用 `test -f` 检查文件
- 禁止使用 `test -d` 作为唯一验证手段
- 所有文件路径必须精确到具体文件

## 6. 相关文件

| 文件 | 说明 |
|------|------|
| `scripts/detect-fake-delivery.sh` | 自动化检测脚本 |
| `docs/monitoring/fake-delivery-dashboard.json` | Grafana Dashboard |
| `docs/fake-delivery-blacklist.md` | 虚假交付黑名单 |
| `docs/delivery-verification.md` | 交付物验证指南 |

## 7. 更新记录

| 日期 | 更新内容 | 更新者 |
|------|---------|--------|
| 2026-05-23 | 初始创建，建立追踪机制 | Orion |
| 2026-05-30 | MIN-4149: 添加 PR 合并证明截图要求到 deliver-checklist.md | Orion |
| 2026-05-30 | MIN-4154 (Sprint #242): 更新 deliver-checklist.md，添加 main 分支合并验证步骤；fake-delivery-tracker.md 已存在相关机制 | Orion |

## 8. Sprint #242 虚假交付监控机制建立

### 8.1 任务背景

后端架构师在多个 Sprint（MIN-4135→MIN-4140→MIN-4142→MIN-4139）中连续虚假交付，每次声称完成但代码未合并到 main 分支。需要建立监控机制防止再次发生。

### 8.2 已完成的工作

1. **deliver-checklist.md 已更新**
   - PR #212 (commit 9c34075) 已添加 main 分支合并验证步骤
   - Section "0. PR 合并证明截图（新增）" 要求所有 PR 必须附带 main 分支合并证明截图
   - 截图要求：显示 PR 状态为 "Merged"、目标分支为 `main`、PR 编号和标题

2. **fake-delivery-tracker.md 已更新**
   - 本文档已包含完整的虚假交付追踪机制
   - 包含自动化检测脚本 `scripts/detect-fake-delivery.sh`
   - 包含评分制度和黑名单机制

### 8.3 验收标准达成情况

- [x] deliver-checklist.md 已更新 (PR #212, commit 9c34075)
- [x] fake-delivery-tracker.md 已更新 (本文档)
- [x] PR 已合并到 main 分支 (PR #212)

### 8.4 预防机制

| 机制 | 说明 |
|------|------|
| Pre-review 验证 | 在标记 issue 为 `in_review` 前，必须确认所有声称创建的文件已通过 `git show origin/main:<file>` 验证 |
| PR 合并截图证明 | 所有 PR 必须附带 main 分支合并证明截图 |
| 自动化检测 | `scripts/detect-fake-delivery.sh` 检测所有 worktree 中未合并到 main 的修改 |