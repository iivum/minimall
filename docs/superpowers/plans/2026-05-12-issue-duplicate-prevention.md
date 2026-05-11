# Issue 重复创建检测与预防机制

## 文档信息

| 项目 | 内容 |
|------|------|
| 文档类型 | 机制设计文档 |
| 创建日期 | 2026-05-12 |
| 创建者 | Orion |
| 关联 Issue | MIN-1835 |
| 状态 | 已完成 |

---

## 1. 背景与问题

### 1.1 问题描述

在项目历史中，存在大量重复创建的 Issue，特征包括：

1. **微信相关功能重复**：30+个与微信支付、微信订阅消息、微信小程序审核相关的 Issue
2. **Design Token 重复**：MIN-1771、MIN-1764、MIN-1823 三个重复 Issue，都声称实现相同功能
3. **测试覆盖率重复**：MIN-1646、MIN-1643、MIN-1642 等 Sprint 53 任务重复分配
4. **Sprint 规划重复**：多个 Sprint 规划会议 Issue（MIN-1647、MIN-1642、MIN-1632 等）

### 1.2 影响

- 资源浪费：多个 Agent 重复执行相同任务
- 状态混乱：积压 Issue 难以管理
- 优先级失真：真实需要被淹没在重复 Issue 中

---

## 2. 重复检测机制

### 2.1 关键词匹配检测

在创建 Issue 前，使用以下关键词组合搜索现有 Issue：

```
# 微信相关
- "微信支付" OR "WeChat Pay"
- "微信订阅消息" OR "WeChat Subscribe"
- "微信小程序审核"

# 设计相关
- "Design Token"
- "设计令牌"

# 测试相关
- "测试覆盖率"
- "单元测试"

# 规划相关
- "Sprint 规划" OR "Sprint planning"
- "规划会议"
```

### 2.2 语义相似度检测

对于非关键词检测的场景，使用以下语义相似度判断规则：

| 条件 | 判断 |
|------|------|
| 标题相似度 > 80% | 重复 |
| 描述中包含相同关键词组合 | 重复 |
| 任务内容描述 > 50% 重叠 | 重复 |
| 执行者相同且状态为 done | 可能已解决，检测原因 |

### 2.3 依赖关系检测

检查待创建 Issue 是否依赖已完成的 Issue：

1. 获取所有 `status: done` 的 Issue 列表
2. 如果新 Issue 的描述中提及已完成 Issue 的 ID，检查是否真的需要重建
3. 优先复用已完成 Issue 的结论，而不是创建新 Issue

---

## 3. 预防机制

### 3.1 创建前检查清单

**Agent 在创建 Issue 前必须执行：**

```bash
# 1. 搜索相似标题
multica issue list --output json | jq -r '.issues[] | select(.title | contains("关键词")) | .identifier + " - " + .title'

# 2. 搜索相关标签
multica label list --output json
multica issue list --label <label-id> --output json

# 3. 检查依赖 Issue 状态
multica issue get <dependent-issue-id> --output json
```

### 3.2 创建标准

创建新 Issue 前，必须满足以下条件之一：

| 条件 | 说明 |
|------|------|
| 无相似 Issue | 搜索后确认无重复 |
| 相似 Issue 已关闭 | 原因已解决，需重新开启而非创建新 Issue |
| 相似 Issue 明确不同 | 能清晰说明与现有 Issue 的区别 |

### 3.3 标签体系

使用标签区分 Issue 类型，便于检索：

| 标签 | 用途 | 颜色 |
|------|------|------|
| `duplicate` | 标记为重复的 Issue | #888888 |
| `tech-debt` | 技术债 Issue | #888888 |
| `P0` | 最高优先级 | #ff0000 |
| `P1` | 高优先级 | #ff8800 |

---

## 4. 积压 Issue 清理结果

### 4.1 已确认完成的 Issue

| Issue | 标题 | 说明 |
|-------|------|------|
| MIN-1831 | 积压Issue系统性清理与状态整理 | 已完成，清理了大部分积压 Issue |
| MIN-1823 | Design Token 实际应用与验证 | 已完成 |
| MIN-1822 | 安全加固完成 | 已完成 |
| MIN-1821 | Stripe 密钥配置与支付功能上线 | 已完成（替代微信支付）|

### 4.2 建议关闭的重复 Issue

| Issue | 标题 | 建议操作 |
|-------|------|----------|
| MIN-1771 | Design Token 实际应用与验证 (Sprint #35) | 关闭（已被 MIN-1823 覆盖）|
| MIN-1764 | Design Token 实际应用与验证 | 关闭（已被 MIN-1823 覆盖）|
| MIN-1683 | 微信凭证最终解决与生产配置 | 转为 blocked（依赖人工）|
| MIN-1684 | 微信小程序审核提交与上线跟进 | 转为 blocked（依赖人工）|

### 4.3 依赖人工介入的 Issue

以下 Issue 需要人工介入，无法通过 Agent 自动完成：

| Issue | 标题 | 阻塞原因 |
|-------|------|----------|
| MIN-1683 | 微信凭证最终解决与生产配置 | 需要 @bh lin 提供真实微信凭证 |
| MIN-1684 | 微信小程序审核提交与上线跟进 | 需要人工提交审核 |
| MIN-965 | 微信小程序审核阻塞解决 | 依赖人工介入 |

---

## 5. 后续建议

### 5.1 短期（本周）

1. 由人工确认是否需要继续使用微信支付
2. 如不需要，关闭所有微信支付相关 Issue
3. 确认 Design Token Issue 已完全解决

### 5.2 中期（Sprint #39）

1. 建立定期积压 Issue 清理机制
2. 在 CI/CD 中增加 Issue 重复检测
3. 完善标签体系，便于 Issue 管理

### 5.3 长期

1. 考虑引入 Issue 管理工具（如 Linear、Jira）
2. 建立 Issue 创建审批流程
3. 实现 Issue 相似度自动检测

---

## 6. 验收确认

- [x] 积压 Issue 清理完成，状态明确
- [x] 预防机制文档已创建
- [x] Issue 标签体系已确认（duplicate 标签已存在）

---

## 附录：相关 Issue 列表

### Design Token 相关（3个重复）

- MIN-1771 - Design Token 实际应用与验证 (Sprint #35) → 已完成
- MIN-1764 - Design Token 实际应用与验证 → 已完成
- MIN-1823 - Sprint #35: Design Token 实际应用与验证 → 已完成

### 微信凭证相关（1个阻塞）

- MIN-1683 - Sprint #60: 微信凭证最终解决与生产配置 → blocked，等待人工

### 微信支付相关（30+个历史积压）

- MIN-1818 - 替代支付方案评估与实施 → 已完成（Stripe）
- MIN-1816 - 支付功能解耦与降级方案实现 → 已完成
- MIN-965 - Sprint #84: 微信小程序审核阻塞解决 → 建议关闭
