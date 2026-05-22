# 交付物验证与虚假交付检测

本文档提供验证交付物是否已正确合并到 `main` 分支的检查步骤，以及虚假交付的检测方法。

## 1. 强制验证机制

### 1.1 文件存在性验证（`test -f`）

使用 `test -f` 进行强制性的文件存在性检查：

```bash
# 单文件验证（如果不存在则退出）
test -f "src/main/java/com/example/Service.java" || { echo "文件不存在"; exit 1; }

# 批量验证关键文件
for file in \
    "pom.xml" \
    "src/main/java/com/example/Service.java" \
    "src/test/java/com/example/ServiceTest.java" \
    "src/main/resources/application.yml"; do
    if test -f "$file"; then
        echo "✓ $file exists"
    else
        echo "✗ $file NOT found"
        exit 1
    fi
done
```

### 1.2 目录存在性验证（`test -d`）

```bash
# 验证目录结构
test -d "src/main/java" || { echo "源码目录不存在"; exit 1; }
test -d "src/test/java" || { echo "测试目录不存在"; exit 1; }
test -d "target/classes" || { echo "编译产物目录不存在"; exit 1; }
```

### 1.3 origin/main 验证

使用 `git show origin/main:<file>` 验证文件是否实际存在于远程 main 分支：

```bash
# 验证特定文件（如果不存在则退出）
git show origin/main:src/main/java/com/example/Service.java > /dev/null 2>&1 || { echo "文件不存在于 origin/main"; exit 1; }

# 批量验证（检查关键文件列表）
verify_file_in_main() {
    local file="$1"
    if git show "origin/main:$file" > /dev/null 2>&1; then
        echo "✓ $file exists in main"
        return 0
    else
        echo "✗ $file NOT found in main"
        return 1
    fi
}

# 使用示例
verify_file_in_main "docs/delivery-verification.md"
verify_file_in_main "src/main/java/com/example/Service.java"
```

### 1.4 组合验证模式

```bash
# 完整验证脚本
verify_delivery() {
    local file="$1"
    echo "Verifying: $file"

    # 1. 先验证工作区文件存在
    if ! test -f "$file"; then
        echo "✗ $file does not exist in working tree"
        return 1
    fi
    echo "  ✓ exists in working tree"

    # 2. 再验证 main 分支文件存在
    if ! git show "origin/main:$file" > /dev/null 2>&1; then
        echo "  ✗ not found in origin/main"
        return 1
    fi
    echo "  ✓ exists in origin/main"

    # 3. 验证文件非空
    local size=$(git show "origin/main:$file" | wc -c)
    if [ "$size" -lt 10 ]; then
        echo "  ✗ file is empty or too small"
        return 1
    fi
    echo "  ✓ file has content ($size bytes)"

    return 0
}
```

---

## 2. 虚假交付检测流程

### 2.1 PR 描述与实际交付物对比

虚假交付的典型特征：
- PR 描述声称完成了某功能
- 但代码中缺少相应的实现文件
- 或文件存在但内容为空/无意义

### 2.2 关键检测步骤

#### 文件存在性验证

使用 `git show origin/main:<file>` 验证文件是否实际存在于 main 分支：

```bash
# 验证特定文件
git show origin/main:src/main/java/com/example/Service.java

# 验证目录下的文件数量
git show origin/main:src/main/java | grep -c "\.java$"

# 批量验证（检查关键文件列表）
for file in "pom.xml" "src/main/java/..." "src/test/java/..."; do
  if git show "origin/main:$file" > /dev/null 2>&1; then
    echo "✓ $file exists in main"
  else
    echo "✗ $file NOT found in main"
  fi
done
```

#### Git log 与 PR 对比验证

```bash
# 查看 main 分支的最近提交
git log origin/main --oneline -20

# 查看某个 PR 的合并状态
gh pr list --state merged --base main --limit 100 | grep "PR编号或标题"

# 对比 PR 创建时间和实际提交时间
gh pr view PR编号 --json createdAt,mergedAt,title
```

#### 测试文件与构建产物验证

真实的 Java 项目交付应该包含：
- 单元测试文件 `*Test.java`
- 测试报告存在于 `target/surefire-reports/`

```bash
# 验证测试文件存在
find src/test/java -name '*Test.java' | wc -l

# 验证测试报告存在
ls -la target/surefire-reports/*.txt

# 验证编译产物
find target/classes -name '*.class' | wc -l
```

---

## 3. CI 验证机制

CI 的 `verify-deliverables` job 会自动检查：

1. **文件存在性检查** - 使用 `test -f` 验证关键文件
2. **目录存在性检查** - 使用 `test -d` 辅助验证
3. **测试文件数量检查** - 确保存在测试文件
4. **构建产物检查** - 验证编译成功

### 3.1 CI 验证脚本示例

```bash
#!/bin/bash
set -e

echo "=== CI Delivery Verification ==="

# 1. 文件存在性检查 (test -f)
echo "[1/4] Checking file existence..."
REQUIRED_FILES=(
    "pom.xml"
    "src/main/java/com/example/Service.java"
    "src/test/java/com/example/ServiceTest.java"
)
for file in "${REQUIRED_FILES[@]}"; do
    test -f "$file" || { echo "FAIL: $file not found"; exit 1; }
    echo "  ✓ $file"
done

# 2. 目录存在性检查 (test -d)
echo "[2/4] Checking directory structure..."
REQUIRED_DIRS=(
    "src/main/java"
    "src/test/java"
    "src/main/resources"
)
for dir in "${REQUIRED_DIRS[@]}"; do
    test -d "$dir" || { echo "FAIL: $dir not found"; exit 1; }
    echo "  ✓ $dir"
done

# 3. 测试文件数量检查
echo "[3/4] Checking test files..."
TEST_COUNT=$(find src/test/java -name '*Test.java' | wc -l)
if [ "$TEST_COUNT" -lt 1 ]; then
    echo "FAIL: No test files found"
    exit 1
fi
echo "  ✓ Found $TEST_COUNT test files"

# 4. 构建产物检查
echo "[4/4] Checking build artifacts..."
test -d target/classes || { echo "FAIL: target/classes not found"; exit 1; }
test -f target/classes/com/example/Service.class || { echo "FAIL: class file not found"; exit 1; }
echo "  ✓ Build artifacts exist"

echo "=== All checks passed ==="
```

---

## 4. Post-merge 验证清单

PR 合并后，PR 作者必须在 24 小时内完成以下验证：

| 检查项 | 命令 | 预期结果 |
|--------|------|----------|
| 文件存在于工作区 | `test -f <file>` | 返回 0 |
| 文件存在于 main | `git show origin/main:<file> > /dev/null` | 返回 0 |
| 提交记录存在 | `git log origin/main --oneline \| grep <keyword>` | 有匹配 |
| 构建成功 | `test -d target/classes` | 返回 0 |
| 测试通过 | `test -f target/surefire-reports/*.xml` | 有报告 |

### 4.1 快速验证命令

```bash
# 一行验证（文件存在 + 非空）
test -f "$file" && git show "origin/main:$file" | grep -q . && echo "OK"
```

---

## 5. 常见虚假交付特征

| 特征 | 说明 | 检测方法 |
|------|------|----------|
| PR 已合并但文件不存在 | Agent 声称完成但实际未提交 | `git show origin/main:<file>` |
| 文件存在但内容为空 | 创建了空文件或无意义内容 | `git show origin/main:<file> \| wc -c` |
| 目录存在但无文件 | 只有目录，没有实际源文件 | `find src -name '*.java' \| wc -l` |
| 测试文件不存在 | 声称有测试但实际没有 | `find src/test -name '*Test.java'` |
| 构建产物不存在 | 声称编译成功但 target 为空 | `ls target/classes/*.class` |

---

## 6. 检查步骤

### 1. 检查 PR 是否已合并

使用 `gh pr list` 命令查看已合并到 main 的 PR 列表：

```bash
gh pr list --state merged --base main --limit 100
```

查找目标 PR 的标题或编号，确认其状态为 `merged`。

### 2. 检查 git log 是否有对应提交

```bash
git log origin/main --oneline
```

在输出中查找与交付物相关的提交信息（通常包含 PR 编号或功能描述）。

### 3. 验证文件是否存在

```bash
git show origin/main:<file-path>
```

例如，验证 `docs/delivery-verification.md` 是否存在：

```bash
git show origin/main:docs/delivery-verification.md
```

如果文件存在，此命令会显示文件内容；如果不存在，会报错。

---

## 7. 验收标准

- [ ] `test -f <file>` 返回 0（文件存在于工作区）
- [ ] `git show origin/main:<file> > /dev/null 2>&1` 返回 0（文件存在于 main）
- [ ] `gh pr list --state merged` 中可找到对应 PR
- [ ] `git log origin/main --oneline` 中有对应提交记录
- [ ] 文件路径和内容符合预期

---

## 8. 常见问题

### PR 未显示在 merged 列表

可能原因：
- PR 尚未合并
- PR 合并到了其他分支
- 过滤条件不正确

解决方案：检查 PR 实际状态，确认合并目标分支。

### 文件不存在于 origin/main

可能原因：
- 文件尚未推送
- 合并冲突未解决
- 推送到了错误分支

解决方案：确认本地分支已正确合并并推送到远程 main 分支。