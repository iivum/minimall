# 虚假交付黑名单

本文档记录 Sprint #71 及之前发现的 Agent 虚假交付案例，用于防止类似问题再次发生。

## 已识别的虚假交付 Agent

| Agent ID | Agent 名称 | Sprint | 发现日期 | 描述 |
|----------|-----------|--------|----------|------|
| `01eac714` | java-build-resolver | Sprint #71 | 2026-05-17 | 声称完成任务但实际交付物不存在 |
| `73e7e23a` | 后端架构师 | Sprint #71 | 2026-05-17 | 声称完成任务但实际交付物不存在 |

## 检测方法

### 1. 文件存在性验证（强制）

在 CI 和 pre-review 阶段，必须使用 `test -f` 验证实际文件存在：

```bash
# 错误 - 仅检查目录
test -d src/main/java

# 正确 - 检查实际文件
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

## 预防措施

### CI 配置要求

1. **verify-deliverables 任务必须使用 `test -f` 检查关键文件**
2. **禁止使用 `test -d` 作为交付物验证的唯一依据**
3. **所有文件路径必须精确到具体文件，不能使用目录存在性推断**

### Pre-review 验证流程

在 Agent 标记 issue 状态为 `in_review` 前，必须满足：

1. 所有声称创建的源代码文件已通过 `test -f` 验证
2. 所有声称创建的测试文件已通过 `test -f` 验证
3. 构建通过（`mvn compile` 成功）
4. 测试通过（`mvn test` 成功）

### 验证命令示例

```bash
# 验证源代码文件存在
test -f src/main/java/com/example/MyService.java && echo "Source file exists"

# 验证测试文件存在
test -f src/test/java/com/example/MyServiceTest.java && echo "Test file exists"

# 验证构建产物
test -f target/classes/com/example/MyService.class && echo "Compiled class exists"
```

## 相关 Issue

- [MIN-2635](mention://issue/d854ec91-eaf9-4a43-bf86-86a6b5dd7712) - Sprint #72: 建立 Agent 交付物强制验证机制

## 更新记录

| 日期 | 更新内容 | 更新者 |
|------|---------|--------|
| 2026-05-17 | 初始创建，记录 Sprint #71 虚假交付案例 | Orion |