# 交付预检机制 / Deliverables Pre-Check Mechanism

## 概述

本文档定义了 Phase 75 的交付预检机制，用于防止虚假交付再次发生（Phase 66, 67, 73, 74均出现虚假交付问题）。

---

## 背景

### 问题描述

- Phase 66, 67, 73, 74 均出现虚假交付问题
- `verify-deliverables.sh` 已存在但未被强制执行
- 需要建立自动化检测机制

### 目标

- 所有 PR 必须通过交付物验证
- CI 中集成 `verify-deliverables.sh` 检查
- 建立虚假交付检测规则

---

## 机制设计

### 核心组件

1. **verify-deliverables.sh** - 交付物文件存在性验证脚本
2. **CI verify-deliverables job** - GitHub Actions 中的自动化验证步骤
3. **merge-gate** - 合并门禁，确保所有检查通过才允许合并
4. **team-driven-verification.md** - 团队驱动的验证文档

### 验证流程

```
PR 创建 → CI Pipeline → verify-deliverables (新增) → merge-gate → 合并
```

---

## CI 验证步骤

### verify-deliverables 任务

**触发条件:** `github.event_name == 'pull_request'`

**执行内容:**

1. 检查 `src/test/java` 目录是否存在
2. 统计测试文件数量
3. 执行 `verify-deliverables.sh` 验证交付物
4. 生成验证报告
5. 上传验证报告作为 artifact

**退出码:**
- `0` - 全部验证通过
- `1` - 任意文件缺失

### merge-gate 依赖

`verify-deliverables` 任务现在作为 `merge-gate` 的必需依赖项，确保：
- 只有通过交付物验证的 PR 才能合并
- 阻止虚假交付进入主干分支

---

## 虚假交付检测规则

### 1. 文件存在性检查

```bash
./scripts/verify-deliverables.sh <文件1> [文件2] ...
```

验证每个文件是否存在，使用 `test -f` 检查。

### 2. 测试文件检查

```bash
# 统计测试文件数量
TEST_FILES=$(find src/test/java -name '*Test.java' 2>/dev/null | wc -l)

# 如果测试文件数量为0，输出警告
if [ "$TEST_FILES" -eq 0 ]; then
  echo "警告: 未找到测试文件"
fi
```

### 3. 覆盖率检查

JaCoCo 覆盖率阈值: **25%**

### 4. 惩戒机制

| 次数 | 惩戒措施 |
|------|----------|
| 第1次 | 警告 - 在 issue 中记录违规 |
| 第2次 | 暂停 - 该 Agent 暂停接新任务 24 小时 |
| 第3次 | 移除 - 该 Agent 从项目移除 |

---

## 验收标准

- [x] 所有 PR 必须通过交付物验证
- [x] CI 中集成 verify-deliverables.sh 检查
- [x] 建立虚假交付检测规则
- [x] verify-deliverables 作为 merge-gate 的必需依赖
- [x] 生成并上传验证报告

---

## 版本历史

| 版本 | 日期 | 修改内容 |
|------|------|----------|
| 1.0 | 2026-05-16 | 初始版本 - 建立交付预检机制 (MIN-2413) |