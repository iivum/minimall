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

## 交付完成定义

满足以下**全部条件**，方可视为"交付完成"：

1. **文件存在于 main 分支** — 交付物文件必须已 merge 到 `main` 分支
2. **文件内容非空** — 文件存在且包含有效内容（非仅空白或占位符）
3. **验证通过** — 所有验证命令返回退出码 0

> **判定原则：** 文件必须存在于 main 分支，而非仅存在于特性分支或本地环境。未合并到 main 的文件不视为完成交付。

---

## 虚假交付惩戒机制

### 触发条件

以下任一情况触发惩戒机制：

| 场景 | 说明 |
|------|------|
| 虚假声明 | 声称已交付但文件未存在于 main 分支 |
| 内容空洞 | 文件存在但内容为空或仅含占位符 |
| 验证失败 | 验证命令执行后退出码非 0 |
| 伪造验证 | 伪造或绕过验证命令输出 |

### 惩戒规则

| 次数 | 惩戒措施 |
|------|----------|
| 第1次 | 警告 — 在 issue 中记录违规 |
| 第2次 | 暂停 — 该 Agent 暂停接新任务 24 小时 |
| 第3次 | 移除 — 该 Agent 从项目移除 |

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

## 版本历史

| 版本 | 日期 | 修改内容 |
|------|------|----------|
| 1.0 | 2026-05-15 | 初始版本 - 定义步骤结构规范和惩戒机制 |
| 1.1 | 2026-05-18 | 添加"交付完成"定义，明确 main 分支判定标准，更新惩戒机制触发条件 |