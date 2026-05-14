# 团队驱动验收机制 / Team-Driven Verification Mechanism

> **Phase 45**: 建立团队驱动验收机制，防止虚假交付

## 背景

Phase 42/43/44 多个 agent 声称完成工作但实际文件不存在，缺少强制校验机制。

## 核心原则

1. **声明即验证**: 每个交付物必须在步骤中包含实际验证命令
2. **未见即不存在**: 只有通过 `ls` 或 `test -f` 验证的文件才算存在
3. **连带责任**: 连续两次虚假交付则暂停该 agent

---

## 步骤结构规范

所有团队驱动 issue 的 steps 必须包含以下三个阶段：

### 标准步骤模板

```markdown
- [ ] **Step X: [任务描述]**
  - **交付物**: `src/main/java/.../Example.java`
  - **验证命令**: `test -f src/main/java/.../Example.java && echo "EXISTS" || echo "MISSING"`
  - **预期输出**: `EXISTS`
```

### 验收阶段（所有交付物完成后）

```markdown
- [ ] **Step Y: 文件存在性校验**
  ```bash
  # 验证所有声明的交付物
  test -f src/main/java/com/minimall/model/Example.java && \
  test -f src/main/java/com/minimall/service/ExampleService.java && \
  test -f src/test/java/com/minimall/model/ExampleTest.java && \
  echo "ALL DELIVERABLES EXIST" || echo "MISSING FILES DETECTED"
  ```
  - **预期**: `ALL DELIVERABLES EXIST`
  - **未通过**: 立即停止，报告缺失文件
```

---

## 虚假交付惩戒机制

| 次数 | 处理 |
|------|------|
| 第1次 | 警告，标记 issue 为 `blocked`，要求重新验证 |
| 第2次 | 暂停该 agent，通知 workspace owner |
| 第3次 | 从团队中移除该 agent |

### 虚假交付认定标准

满足以下任一条件即视为虚假交付：
- 声称文件已创建但 `test -f` 返回非 0
- 声称测试通过但实际未运行测试
- 声称已提交但 `git log` 查无此 commit

---

## 执行流程

### 1. 计划阶段

在创建 issue 时，明确列出：
- 所有交付物（文件路径）
- 每个交付物的验证命令
- 最终校验步骤

### 2. 执行阶段

每个 step 完成后：
1. 运行该 step 的验证命令
2. 截图/保存输出作为证据
3. 在 comment 中记录验证结果

### 3. 验收阶段

在标记 `done` 前：
1. 运行最终校验脚本（见下方）
2. 所有验证必须通过
3. 结果截图发 comment

---

## 最终校验脚本

在所有 steps 完成后，运行以下校验：

```bash
#!/bin/bash
# verify-deliverables.sh - 最终交付物验证

set -e

echo "=== 团队驱动交付物最终校验 ==="

# 从 issue 描述中提取的交付物列表
DELIVERABLES=(
  "src/main/java/com/minimall/model/Example.java"
  "src/main/java/com/minimall/service/ExampleService.java"
  "src/test/java/com/minimall/model/ExampleTest.java"
)

MISSING_COUNT=0

for file in "${DELIVERABLES[@]}"; do
  if test -f "$file"; then
    echo "✓ $file"
  else
    echo "✗ MISSING: $file"
    MISSING_COUNT=$((MISSING_COUNT + 1))
  fi
done

echo ""
if [ $MISSING_COUNT -eq 0 ]; then
  echo "=== 校验通过：所有交付物存在 ==="
  exit 0
else
  echo "=== 校验失败：$MISSING_COUNT 个文件缺失 ==="
  exit 1
fi
```

---

## 示例：完整步骤结构

```markdown
## Task 1: 创建用户实体

- [ ] **Step 1: 编写测试**
  ```java
  // src/test/java/com/minimall/model/UserTest.java
  // 测试代码...
  ```
  - **交付物**: `src/test/java/com/minimall/model/UserTest.java`
  - **验证命令**: `test -f src/test/java/com/minimall/model/UserTest.java`
  - **预期**: 退出码 0

- [ ] **Step 2: 运行测试（验证失败）**
  ```bash
  cd /path/to/project && mvn test -Dtest=UserTest -q
  ```
  - **预期**: 测试失败（RED phase）

- [ ] **Step 3: 创建实体**
  ```java
  // src/main/java/com/minimall/model/User.java
  // 实体代码...
  ```
  - **交付物**: `src/main/java/com/minimall/model/User.java`
  - **验证命令**: `test -f src/main/java/com/minimall/model/User.java`

- [ ] **Step 4: 运行测试（验证通过）**
  ```bash
  mvn test -Dtest=UserTest -q
  ```
  - **预期**: 测试通过

- [ ] **Step 5: 提交**
  ```bash
  git add src/test/java/com/minimall/model/UserTest.java src/main/java/com/minimall/model/User.java
  git commit -m "feat: add User entity"
  ```

- [ ] **Step 6: 最终校验**
  ```bash
  test -f src/test/java/com/minimall/model/UserTest.java && \
  test -f src/main/java/com/minimall/model/User.java && \
  echo "ALL DELIVERABLES VERIFIED"
  ```
  - **预期**: `ALL DELIVERABLES VERIFIED`
```

---

## 验证检查清单

在 issue 标记 `done` 前，确认：

- [ ] 每个交付物都有对应的验证命令
- [ ] 所有验证命令都已执行并通过
- [ ] 最终校验脚本已运行并通过
- [ ] 验证输出已保存到 comment

---

## 版本历史

| 版本 | 日期 | 修改内容 |
|------|------|----------|
| 1.0 | 2026-05-15 | 初始版本 - Phase 45 验收机制 |