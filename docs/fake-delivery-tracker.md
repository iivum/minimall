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

## 8. Sprint #221 虚假交付验证报告 (MIN-4177)

**验证时间**: 2026-05-30
**验证者**: Sprint 排序师
**任务**: 对所有 in_review issue 强制执行 main 分支验证

### 8.1 验证方法

1. 使用 `git show origin/main:<file>` 验证交付物存在
2. 使用 `git ls-files origin/main` 检查测试文件是否存在
3. 使用 `git log origin/main --oneline` 验证提交记录

### 8.2 in_review Issue 验证结果

| Issue | 标题 | 执行者 | 验证结果 | 说明 |
|-------|------|--------|----------|------|
| MIN-4147 | Sprint #240: 测试覆盖率提升至 60%+ | Orion | 需核实 | 需 mvn test jacoco:report 验证 |
| MIN-4174 | Sprint #221: E2E 测试完整验证 | Orion | 通过 | 12 tests pass, Build Success |
| MIN-4173 | 团队驱动 | Sprint 排序师 | 完成 | Autopilot 触发执行 |
| MIN-4165 | 团队驱动 | Sprint 排序师 | 完成 | Autopilot 触发执行 |
| MIN-4162 | 团队驱动 | Sprint 排序师 | 完成 | Autopilot 触发执行 |
| MIN-4159 | 团队驱动 | Sprint 排序师 | 完成 | Autopilot 触发执行 |
| MIN-4156 | 团队驱动 | Sprint 排序师 | 完成 | Autopilot 触发执行 |
| MIN-4153 | 团队驱动 | Sprint 排序师 | 完成 | Autopilot 触发执行 |
| MIN-4150 | Sprint #241: 测试覆盖率提升至 65%+ | Orion | 需核实 | 需 jacoco:report 验证 |
| MIN-4143 | Sprint #239: 测试覆盖率提升（重新执行）| Orion | 需核实 | 需 jacoco:report 验证 |
| MIN-4136 | Sprint #238: 测试覆盖率提升 | Orion | 需核实 | 需 jacoco:report 验证 |
| MIN-4148 | 团队驱动 | Sprint 排序师 | 完成 | Autopilot 触发执行 |
| MIN-4145 | 团队驱动 | Sprint 排序师 | 完成 | Autopilot 触发执行 |
| MIN-4141 | 团队驱动 | Sprint 排序师 | 完成 | Autopilot 触发执行 |
| MIN-4138 | 团队驱动 | Sprint 排序师 | 完成 | Autopilot 触发执行 |
| MIN-4134 | 团队驱动 | Sprint 排序师 | 完成 | Autopilot 触发执行 |

### 8.3 main 分支测试文件验证

| Issue | 声称的文件 | 验证结果 |
|-------|-----------|----------|
| MIN-4147 | ServiceCoverage.txt | 文件不存在 |
| MIN-4147 | ControllerCoverage.txt | 文件不存在 |
| MIN-4174 | PaymentFlowE2ETest.java | 存在，76行 |
| MIN-4174 | AuthFlowE2ETest.java | 存在 |
| MIN-4174 | OrderFlowE2ETest.java | 存在 |
| MIN-4150 | ProductServiceTest.java | 存在 |
| MIN-4150 | OrderServiceTest.java | 存在，198行 |
| MIN-4150 | PaymentServiceTest.java | 存在，64行 |

### 8.4 结论

- **虚假交付风险**: 多个 coverage issue 声称的覆盖率报告文件未在 main 分支找到
- **E2E 测试**: MIN-4174 验证通过，12 tests pass, BUILD SUCCESS
- **团队驱动 issues**: 均为 Autopilot 触发，符合预期

### 8.5 建议措施

1. Orion 需重新执行 coverage 提升任务，提供 jacoco:report 截图证明
2. 覆盖率目标应基于实际 mvn test jacoco:report 结果
3. 建议在验收标准中增加"截图或日志证明"