# 交付验证 / Delivery Verification

## 概述

本文档定义 Sprint 交付验证的标准流程，用于确保所有交付物已正确合并到 main 分支，防止虚假交付。

---

## 验证检查项

### 1. PR 合并状态检查

确认 PR 已合并到 main 分支：

```bash
# 查看 PR 是否已合并
gh pr view <PR号> --json state,mergedAt
```

**预期结果:** `state: "MERGED"`

### 2. 提交记录检查

确认提交记录存在于 main 分支：

```bash
# 查看 main 分支的提交历史
git log main --oneline

# 搜索特定提交（按标题或内容）
git log main --oneline --grep="<关键字>"

# 查看特定文件的提交记录
git log main --oneline -- <文件路径>
```

**预期结果:** 应能看到对应的提交记录

### 3. 代码变更检查

确认分支上的修改已体现在 main 中：

```bash
# 查看 agent/sprint/xxx 分支与 main 的差异
git diff main agent/sprint/xxx

# 查看特定文件的差异
git diff main agent/sprint/xxx -- <文件路径>

# 只看文件路径统计
git diff main agent/sprint/xxx --stat
```

**预期结果:** 应能看到对应的代码变更

### 4. 文件存在性检查

确认关键文件已创建：

```bash
# 检查文件是否存在
test -f <文件路径> && echo "存在" || echo "不存在"

# 批量检查
for file in <文件1> <文件2>; do
  test -f "$file" || echo "缺失: $file"
done
```

---

## 快速验证命令

### 单次 Sprint 验证脚本

```bash
#!/bin/bash
# verify-sprint-delivery.sh - Sprint 交付验证脚本

SPRINT_BRANCH="agent/sprint/xxx"
MAIN_BRANCH="main"

echo "=== Sprint 交付验证 ==="
echo ""

echo "1. 检查 PR 合并状态..."
gh pr list --base main --head "$SPRINT_BRANCH" --state MERGED --json number,title

echo ""
echo "2. 检查提交记录..."
git log main --oneline --grep="$SPRINT_BRANCH" | head -5

echo ""
echo "3. 检查代码变更..."
git diff main "$SPRINT_BRANCH" --stat

echo ""
echo "4. 验证完成"
```

### 验证清单

| 检查项 | 命令 | 预期 |
|--------|------|------|
| PR 已合并 | `gh pr list --state MERGED` | PR 出现在列表中 |
| 提交存在 | `git log main --oneline` | 看到对应提交 |
| 变更存在 | `git diff main agent/sprint/xxx` | 显示变更内容 |
| 文件存在 | `test -f <path>` | 退出码为 0 |

---

## 虚假交付处理

如果验证未通过：

1. **立即停止** - 不要标记为完成
2. **报告问题** - 在 issue 中记录具体缺失项
3. **联系执行者** - 要求补充交付
4. **触发惩戒** - 按照 [deliverables-precheck-mechanism.md](./superpowers/deliverables-precheck-mechanism.md) 中的规则处理

---

## 验收标准

- [ ] PR 已合并到 main 分支
- [ ] git log main 中有对应提交
- [ ] git diff main 显示对应修改
- [ ] 关键文件存在于 main 分支