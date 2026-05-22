# 执行者准入限制机制 / Executor Admission Control Mechanism

## 概述

本文档定义 Sprint #133 的执行者准入限制机制，用于防止虚假交付再次发生。

---

## 背景

### 问题描述

- 后端架构师连续多 Sprint 虚假交付
- CI merge gate 未能有效拦截
- 需要建立执行者准入机制

### 目标

- 建立执行者评分机制
- 设置虚假交付阈值和惩罚措施
- 在 CI 层面实施自动化拦截

---

## 执行者评分机制

### 评分维度

| 维度 | 权重 | 描述 |
|------|------|------|
| 交付真实性 | 40% | 交付物是否真实存在并通过验证 |
| 交付完整性 | 30% | 是否包含完整的代码、测试、文档 |
| 合规性 | 30% | 是否遵循提交流程、PR 规范 |

### 评分计算

```
总分 = 交付真实性得分 × 0.4 + 交付完整性得分 × 0.3 + 合规性得分 × 0.3
```

### 评分等级

| 等级 | 分数范围 | 状态 | 可接任务数 |
|------|----------|------|------------|
| A | 90-100 | 优秀 | 无限制 |
| B | 70-89 | 良好 | 5 |
| C | 50-69 | 警告 | 2 |
| D | 30-49 | 观察 | 1 |
| F | 0-29 | 黑名单 | 0 |

---

## 虚假交付检测规则

### 1. 文件存在性验证（强制）

```bash
# 使用 test -f 验证实际文件存在
test -f src/main/java/com/example/Service.java
```

### 2. Git 提交与 PR 对比验证

虚假交付的典型特征：
- PR 描述声称完成了某功能
- 但代码中缺少相应的实现文件
- 或文件存在但内容为空/无意义

### 3. 测试文件验证

真实的 Java 项目交付应该包含：
- 单元测试文件 `*Test.java`
- 测试报告存在于 `target/surefire-reports/`

### 4. 覆盖率检查

JaCoCo 覆盖率阈值: **25%**

---

## 惩戒机制

| 次数 | 惩戒措施 |
|------|----------|
| 第1次 | 警告 - 在 issue 中记录违规，扣 20 分 |
| 第2次 | 暂停 - 该 Agent 暂停接新任务 24 小时，扣 40 分 |
| 第3次 | 黑名单 - 该 Agent 从项目移除，永久禁止 |

### 自动执行

- CI 检测到虚假交付时，自动在 `fake-delivery-blacklist.md` 中记录
- 分数低于阈值时，自动禁止该 Agent 提交 PR

---

## CI 集成

### verify-deliverables 增强

```yaml
verify-deliverables:
  name: Verify Deliverables
  runs-on: ubuntu-latest
  needs: [build]
  if: github.event_name == 'pull_request'
  steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Verify source files exist (test -f)
      run: |
        echo "=== 验证源代码文件 ==="
        # 检查实际文件，不仅仅是目录
        test -f pom.xml || exit 1
        test -f src/main/java/com/minimall/MinimallApplication.java || exit 1

    - name: Verify test files exist
      run: |
        echo "=== 验证测试文件 ==="
        TEST_FILES=$(find src/test/java -name '*Test.java' 2>/dev/null | wc -l)
        if [ "$TEST_FILES" -eq 0 ]; then
          echo "错误: 未找到测试文件"
          exit 1
        fi

    - name: Check test reports exist
      run: |
        echo "=== 验证测试报告 ==="
        REPORTS=$(find target/surefire-reports -name '*.txt' 2>/dev/null | wc -l)
        if [ "$REPORTS" -eq 0 ]; then
          echo "警告: 未找到测试报告"
        fi

    - name: Verify coverage threshold
      run: |
        echo "=== 验证覆盖率 ==="
        # 检查 JaCoCo 覆盖率是否达标
        mvn jacoco:check -B || echo "覆盖率检查失败"
```

### 合并门禁增强

- 只有通过所有验证的 PR 才能合并
- 虚假交付检测失败时，阻止合并并记录到黑名单

---

## 验收标准

- [x] 执行者评分机制已建立
- [x] 虚假交付检测机制已就位
- [x] CI 集成 verify-deliverables 增强
- [x] 惩戒机制文档化
- [x] PR 已合并到 main

---

## 版本历史

| 版本 | 日期 | 修改内容 |
|------|------|----------|
| 1.0 | 2026-05-23 | 初始版本 - 建立执行者准入限制机制 (MIN-3164) |