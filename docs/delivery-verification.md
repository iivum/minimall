# 交付物验证指南

本文档用于验证 Agent 交付物是否真实合并到 main 分支，防止虚假交付。

## 验证检查步骤

### 1. PR 合并状态检查

验证 PR 是否已合并到 main 分支：

```bash
gh pr list --state merged --base main --limit 100
```

查找对应 PR 记录，确认状态为 `merged`。

### 2. Git 提交历史检查

验证提交是否存在于 origin/main：

```bash
git log origin/main --oneline | grep -i "<关键词或描述>"
```

或者查看最近的提交：

```bash
git log origin/main --oneline -20
```

### 3. 文件存在性验证

通过 git show 验证文件在 main 分支中存在：

```bash
git show origin/main:<文件路径>
```

例如：

```bash
git show origin/main:docs/delivery-verification.md
```

如果文件存在，会显示文件内容；如果不存在，会报错。

## 验证命令示例

### 完整验证流程

```bash
# 步骤 1：检查 PR 是否已合并
gh pr list --state merged --base main --limit 50 | grep -i "delivery"

# 步骤 2：确认提交在 origin/main 中
git log origin/main --oneline | grep -i "delivery-verification"

# 步骤 3：验证文件确实存在于 main 分支
git show origin/main:docs/delivery-verification.md | head -20
```

### 快速验证

```bash
# 一行命令验证文件存在
git show origin/main:docs/delivery-verification.md > /dev/null 2>&1 && echo "文件已存在" || echo "文件不存在"
```

## 虚假交付识别

虚假交付的典型特征：
- PR 声称已完成但状态不是 `merged`
- git log 中找不到对应提交
- `git show origin/main:<file>` 报错文件不存在
- 文件内容为空或与描述不符

## 相关文档

- [虚假交付黑名单](./fake-delivery-blacklist.md) - 已识别的虚假交付案例

## 更新记录

| 日期 | 更新内容 | 更新者 |
|------|---------|--------|
| 2026-05-23 | 初始创建 | Orion |