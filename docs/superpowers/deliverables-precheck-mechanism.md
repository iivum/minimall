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
- 自动化运行 Maven 测试验证测试可执行性

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
4. 执行 `verify-deliverables.sh --mvn` 运行 Maven 测试验证
5. 生成验证报告
6. 上传验证报告作为 artifact

**退出码:**
- `0` - 全部验证通过
- `1` - 任意文件缺失或测试失败

### --mvn 参数功能 (Sprint #113 新增)

```bash
./scripts/verify-deliverables.sh --mvn [base_commit]
```

**功能说明:**

1. **测试文件统计** - 统计当前 `src/test/java` 目录下 `*Test.java` 文件数量
2. **测试文件列表** - 显示所有测试文件的完整路径
3. **基准比较** - 如果提供基准提交 SHA，比较测试数量变化
4. **新增文件检测** - 检测新增测试文件是否真实存在，防止虚假文件声明
5. **Maven 测试执行** - 运行 `mvn test -B -q` 验证测试可执行性
6. **测试结果统计** - 输出测试执行统计信息

**输出示例:**

```
=== Maven 测试验证模式 ===

✅ pom.xml 存在
✅ 使用 Maven Wrapper

=== 统计测试文件 ===
当前测试文件数量: 12

测试文件列表:
  - src/test/java/com/minimall/config/SecurityUtilsTest.java
  ...

=== 与基准提交 origin/main 比较 ===
基准提交测试文件数量: 10
📈 新增 2 个测试文件

=== 检测新增测试文件 ===
发现新增测试文件:
  ✅ src/test/java/com/minimall/service/NewServiceTest.java (真实存在)

=== 运行 Maven 测试 ===
执行: ./mvnw test -B -q
✅ Maven 测试执行成功

=== 测试执行统计 ===
测试类数量: 12
✅ Maven 测试验证完成
```

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

### 3. Maven 测试验证 (Sprint #113 新增)

```bash
./scripts/verify-deliverables.sh --mvn [base_commit]
```

- 验证 Maven 测试可执行性
- 检测新增测试文件是否真实存在
- 防止虚假测试文件交付

### 4. 覆盖率检查

JaCoCo 覆盖率阈值: **25%**

### 5. 惩戒机制

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
- [x] verify-deliverables.sh 支持 --mvn 参数 (Sprint #113)
- [x] CI 流程集成 Maven 测试验证 (Sprint #113)
- [x] 文档化验证机制 (Sprint #113)

---

## 版本历史

| 版本 | 日期 | 修改内容 |
|------|------|----------|
| 1.1 | 2026-05-22 | 新增 --mvn 参数和 Maven 测试验证功能 (MIN-3073) |
| 1.0 | 2026-05-16 | 初始版本 - 建立交付预检机制 (MIN-2413) |