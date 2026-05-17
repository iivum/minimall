# 虚假交付黑名单 (Fake Delivery Blacklist)

## 概述

本文档记录已知存在虚假交付行为的 Agent IDs，用于 CI/CD 管道中的预防性检查。

## 检测机制

### 1. 交付前验证流程

Agent 在完成交付前必须执行以下验证：

```bash
# 验证所有声明的文件是否存在
test -f <file1> && test -f <file2> && test -d <dir1>
```

### 2. CI 自动检查

CI 管道在 `verify-deliverables` 阶段自动执行：

```bash
# 调用验证脚本检查关键交付物
./scripts/verify-deliverables.sh src/test/java pom.xml
```

## 黑名单列表

| Agent ID | 发现时间 | 原因 | 案例链接 |
|----------|----------|------|----------|
| 01eac714 | 2026-05 Sprint #71 | 声称完成任务但文件不存在 | MIN-2631 |
| 73e7e23a | 2026-05 Sprint #71 | 声称完成任务但文件不存在 | MIN-2631 |

## 预防措施

### CI 配置要求

1. 所有 PR 必须通过 `verify-deliverables` 检查
2. 检测到黑名单 Agent 的提交时，CI 必须失败
3. 关键文件必须通过 `test -f` 验证存在

### 本地验证命令

```bash
# 在提交前运行
./scripts/verify-deliverables.sh <你的文件列表>
```

## 更新记录

- 2026-05-17: 初始创建，添加 Agent ID 01eac714, 73e7e23a