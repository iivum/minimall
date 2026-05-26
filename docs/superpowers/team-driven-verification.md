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

## 虚假交付判定标准

以下情况被视为虚假交付：

1. **伪造交付物** - 提交不存在的文件或目录
2. **伪造验证结果** - 声称验证通过但实际未执行验证
3. **部分交付** - 仅提交部分要求的交付物
4. **无效内容** - 文件存在但内容为空或不符合要求
5. **时间戳作弊** - 伪造 commit 时间以规避惩戒

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

## 自动验证检查

所有交付物必须通过以下自动验证检查方可视为有效交付：

### 1. 交付物存在性验证
```bash
# 批量验证文件存在性
verify_files_exist() {
  local exit_code=0
  for file in "$@"; do
    if [[ ! -f "$file" ]]; then
      echo "❌ $file - 文件不存在"
      exit_code=1
    else
      echo "✅ $file"
    fi
  done
  return $exit_code
}
```

### 2. 交付物路径规范验证
```bash
# 验证路径符合项目规范
verify_path_convention() {
  local file="$1"
  # 路径必须以项目根目录的相对路径开头
  if [[ "$file" =~ ^[a-zA-Z0-9_/-]+\.[a-zA-Z]+$ ]]; then
    echo "✅ 路径规范: $file"
    return 0
  else
    echo "❌ 路径不规范: $file"
    return 1
  fi
}
```

### 3. 交付物内容完整性验证
```bash
# 验证文件非空且内容有效
verify_content_valid() {
  local file="$1"
  if [[ ! -s "$file" ]]; then
    echo "❌ $file - 文件为空"
    return 1
  fi
  # 检查文件是否有可读内容（至少10字节）
  local size=$(stat -f%z "$file" 2>/dev/null || stat -c%s "$file" 2>/dev/null)
  if [[ "$size" -lt 10 ]]; then
    echo "❌ $file - 内容过少(${size}字节)"
    return 1
  fi
  echo "✅ $file - 内容完整(${size}字节)"
  return 0
}
```

### 4. 自动验证脚本
```bash
#!/bin/bash
# auto-verify-deliverables.sh - 自动验证所有交付物

set -e

DELIVERABLES=("$@")
FAILED=0

echo "=== 自动交付物验证 ==="
echo "时间: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
echo ""

for file in "${DELIVERABLES[@]}"; do
  echo "验证: $file"
  
  # 1. 存在性检查
  if [[ ! -f "$file" ]]; then
    echo "  ❌ 文件不存在"
    ((FAILED++))
    continue
  fi
  
  # 2. 路径规范检查
  if [[ ! "$file" =~ ^[a-zA-Z0-9_/-]+\.[a-zA-Z]+$ ]]; then
    echo "  ⚠️ 路径不符合规范"
  fi
  
  # 3. 内容完整性检查
  if [[ ! -s "$file" ]]; then
    echo "  ❌ 文件为空"
    ((FAILED++))
    continue
  fi
  
  echo "  ✅ 验证通过"
done

echo ""
echo "=== 汇总 ==="
echo "总计: ${#DELIVERABLES[@]}"
echo "通过: $((${#DELIVERABLES[@]} - FAILED))"
echo "失败: $FAILED"

if [[ $FAILED -eq 0 ]]; then
  echo "✅ 全部验证通过"
  exit 0
else
  echo "❌ 验证失败"
  exit 1
fi
```

### 5. 验证执行时机
- **CI/CD 阶段**: 在代码提交后自动触发
- **代码审查前**: 必须通过自动验证方可提交 PR
- **人工复核前**: 自动验证结果作为必填项

---

## 代码审查要求

所有交付物必须通过代码审查确认方可视为有效交付：

### 审查检查项
- [ ] 交付物文件已真实创建（查看 commit 内容）
- [ ] 文件路径符合项目规范
- [ ] 文件内容完整有效
- [ ] 验证命令已执行且输出正确
- [ ] 无虚假交付风险（见下方风险检测）

### 虚假交付风险检测
审查时需检查以下风险点：

| 风险类型 | 检测方法 | 判定标准 |
|---------|---------|---------|
| 空文件交付 | `stat -f%z` 或 `stat -c%s` | 文件大小 > 0 |
| 路径伪造 | 检查 commit diff | 文件确实被创建 |
| 内容篡改 | Git history 审查 | 无异常 commit |
| 验证跳过 | CI log 审查 | 验证命令执行成功 |

### 审查流程
1. **初审**: 检查文件存在性和内容完整性
2. **细审**: 验证路径规范和内容有效性
3. **风险扫描**: 检查是否有虚假交付特征
4. **批准**: 所有检查项通过后方可合并

---

## 验收检查表

### 步骤验收标准

| 步骤类型 | 验收检查项 | 标准 |
|---------|-----------|------|
| 交付物创建 | 文件存在性 | `test -f <file>` 返回 0 |
| 交付物创建 | 文件非空 | `test -s <file>` 返回 0 |
| 交付物创建 | 内容格式正确 | 通过 schema 验证或格式检查 |
| 交付物创建 | 路径规范 | 符合 `[a-zA-Z0-9_/-]+\.[a-zA-Z]+` 模式 |
| 验证命令 | 命令可执行 | `test -x <command>` 返回 0 |
| 验证命令 | 退出码正确 | 执行后 `$?` 为 0 |
| 验证命令 | 输出匹配 | 实际输出与预期输出一致 |
| 代码审查 | 审查通过 | Reviewer 批准并签署 |
| 步骤完成 | 所有检查通过 | 验收检查表全部勾选 |
| 步骤完成 | commit 已推送 | 远程分支包含该 commit |

### 虚假交付检查清单

在验证步骤时，逐项确认以下情况均不存在：

- [ ] 交付物文件实际存在于文件系统中
- [ ] 验证命令已真实执行（非模拟）
- [ ] 交付物数量与要求一致（无遗漏）
- [ ] 文件内容符合规格（非空、无损坏）
- [ ] commit 时间戳真实（非伪造）
- [ ] 验证输出与执行结果一致（非篡改）
- [ ] 代码审查已通过（非自动批准）
- [ ] 路径符合项目规范（非临时路径）

### 验收流程

1. **自动验证** - 执行 `auto-verify-deliverables.sh` 验证交付物
2. **代码审查** - 提交 PR 并获得至少 1 人批准
3. **人工复核** - 检查自动验证报告和代码审查意见
4. **结果比对** - 实际输出与预期输出匹配
5. **状态更新** - 检查表勾选完成状态

---

## 版本历史

| 版本 | 日期 | 修改内容 |
|------|------|----------|
| 1.3 | 2026-05-27 | 添加自动验证检查和代码审查要求，强化虚假交付检测 |
| 1.2 | 2026-05-18 | 添加虚假交付判定标准 + 验收检查表 |
| 1.0 | 2026-05-15 | 初始版本 - 定义步骤结构规范和惩戒机制 |