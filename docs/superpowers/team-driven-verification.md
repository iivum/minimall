# 团队驱动验证 / Team-Driven Verification

## 概述

本文档定义了步骤结构规范，建立可验证的交付机制，防止虚假交付。

---

## 步骤结构规范

每个步骤必须包含以下三个部分：

### 1. 交付物 (Deliverables)
- 明确列出步骤完成后必须存在的文件
- 每个文件必须通过 `test -f` 验证

### 2. 验证命令 (Verification Commands)
- 提供可执行的命令来验证交付物
- 命令必须返回退出码 0 表示成功

### 3. 预期输出 (Expected Output)
- 记录命令执行后的预期输出
- 用于自动比对验证

---

## 虚假交付惩戒机制

发现虚假交付后，按以下规则处理：

| 次数 | 惩戒措施 |
|------|----------|
| 第1次 | 警告 - 在 issue 中记录违规 |
| 第2次 | 暂停 - 该 Agent 暂停接新任务 24 小时 |
| 第3次 | 移除 - 该 Agent 从项目移除 |

---

## 步骤模板

```markdown
### [步骤名称]

**交付物:**
- `path/to/file1.ext`
- `path/to/file2.ext`

**验证命令:**
```bash
./scripts/verify-deliverables.sh path/to/file1.ext path/to/file2.ext
```

**预期输出:**
```
=== 交付物验证 ===

✅ path/to/file1.ext
✅ path/to/file2.ext

=== 汇总 ===
总计: 2
通过: 2
失败: 0

✅ 全部验证通过
```

**验收标准:**
- [ ] 文件存在性已验证
- [ ] 验证命令已执行
- [ ] 退出码为 0
```

---

## 示例：创建配置文件

### 步骤 1: 创建配置文件

**交付物:**
- `config/application.yml`
- `config/logging.properties`

**验证命令:**
```bash
./scripts/verify-deliverables.sh config/application.yml config/logging.properties
```

**预期输出:**
```
=== 交付物验证 ===

✅ config/application.yml
✅ config/logging.properties

=== 汇总 ===
总计: 2
通过: 2
失败: 0

✅ 全部验证通过
```

**验收标准:**
- [ ] `config/application.yml` 存在且包含有效配置
- [ ] `config/logging.properties` 存在且格式正确
- [ ] 验证命令退出码为 0

---

## 自动验证集成

在 CI/CD 流程中集成验证：

```yaml
# .github/workflows/verify.yml
- name: Verify Deliverables
  run: |
    ./scripts/verify-deliverables.sh \
      config/application.yml \
      config/logging.properties
```

---

## 测试任务验证指南

所有测试任务必须遵循以下验证流程：

### 测试任务交付标准

1. **文件存在性验证** - 使用 `verify-deliverables.sh` 验证所有测试文件
2. **测试通过验证** - `mvn test -B` 必须全部通过
3. **覆盖率验证** - JaCoCo 报告覆盖率 ≥ 目标值（通常 70%）
4. **验证报告提交** - 填写并提交 `deliverables-verification-report-template.md`

### 测试任务检查清单

参见 `test-task-checklist.md`，包含：

- 任务接收检查
- 任务规划检查
- 测试实现检查
- 交付物验证检查
- 验证报告生成检查

### CI 强制验证

CI 流程中的 `verify-deliverables` job 自动执行：

```yaml
verify-deliverables:
  name: Verify Deliverables
  runs-on: ubuntu-latest
  needs: [build, quality]
  steps:
    - name: Run deliverables verification
      run: ./scripts/verify-deliverables.sh \
        src/test/java/com/minimall/controller \
        src/test/java/com/minimall/service \
        target/surefire-reports
```

### 测试相关文档

| 文档 | 用途 |
|------|------|
| `team-driven-verification.md` | 团队验证机制 |
| `test-task-checklist.md` | 测试任务检查清单 |
| `deliverables-verification-report-template.md` | 验证报告模板 |

---

## 版本历史

| 版本 | 日期 | 修改内容 |
|------|------|----------|
| 1.0 | 2026-05-15 | 初始版本 - 定义步骤结构规范和惩戒机制 |
| 1.1 | 2026-05-16 | 添加测试任务验证指南和检查清单 |