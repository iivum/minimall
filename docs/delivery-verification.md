# 交付验证指南

本文档提供 Sprint 交付验证的标准流程，用于确保 Agent 实际完成了声称的工作，防止虚假交付。

## 验证检查清单

### 1. PR 是否已合并到 main

```bash
# 查看 PR 状态
gh pr list --author <agent-name> --state merged

# 查看特定 PR 是否已合并
gh pr view <pr-number> --json state
```

### 2. git log main --oneline 是否有对应提交

```bash
# 查看 main 分支最近提交
git log main --oneline -20

# 搜索特定关键词（如 sprint 编号、功能描述）
git log main --oneline --grep="Sprint #122"

# 搜索特定文件或路径
git log main --oneline --all -- docs/delivery-verification.md
```

### 3. git diff main agent/sprint/xxx 显示对应修改

```bash
# 对比 agent 分支与 main 的差异
git diff main agent/orion/a32d290f --stat

# 查看具体文件差异
git diff main agent/orion/a32d290f -- docs/

# 检查特定文件是否包含实质内容
git diff main agent/orion/a32d290f -- <file-path>
```

## 自动化验证命令

### 文件存在性验证

```bash
# 检查文档是否存在（精确到文件）
test -f docs/delivery-verification.md && echo "Document exists"

# 检查多个关键文件
for file in docs/delivery-verification.md src/main/java/Example.java; do
  test -f "$file" || echo "Missing: $file"
done
```

### 提交验证

```bash
# 验证提交是否在 main 历史中
git log main --oneline | grep "Sprint #122" && echo "Commit found"

# 验证特定文件的提交记录
git log main --oneline -- docs/delivery-verification.md
```

### 分支状态验证

```bash
# 查看分支与 main 的偏离
git rev-list --left-right --count main...agent/sprint/xxx

# 查看 agent 分支相对于 main 的新增提交
git log main..agent/sprint/xxx --oneline
```

## 验收标准

- [ ] 文档路径：`minimall/docs/delivery-verification.md`
- [ ] 包含上述三个检查步骤
- [ ] 包含验证命令示例
- [ ] 已提交到 main 分支
- [ ] 在 CI 或 pre-review 阶段通过 `test -f` 验证

## 相关文档

- [虚假交付黑名单](fake-delivery-blacklist.md) - 已发现的虚假交付案例
- [Sprint 合并状态检查指南](sprint-19-code-merge-status.md) - 代码合并状态验证

## 更新记录

| 日期 | 更新内容 | 更新者 |
|------|---------|--------|
| 2026-05-23 | 初始创建 | Orion |