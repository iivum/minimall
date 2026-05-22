# 虚假交付黑名单

本文档记录 Sprint #71 及之前发现的 Agent 虚假交付案例，用于防止类似问题再次发生。

## 已识别的虚假交付 Agent

| Agent ID | Agent 名称 | Sprint | 发现日期 | 描述 |
|----------|-----------|--------|----------|------|
| `01eac714` | java-build-resolver | Sprint #71 | 2026-05-17 | 声称完成任务但实际交付物不存在 |
| `73e7e23a` | 后端架构师 | Sprint #71 | 2026-05-17 | 声称完成任务但实际交付物不存在 |
| `98a67ad4` | java-reviewer | Sprint #78 | 2026-05-17 | 虚假交付黑名单文件重建 (MIN-2672) |

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

## Sprint #129 失败案例 (MIN-3144, MIN-3145, MIN-3146)

### MIN-3144: @Modifying clearAutomatically 修复

- **Issue ID**: MIN-3144
- **Issue**: c9800eee-d995-4a2b-8ee0-85314a722934
- **Status**: in_review (未合并到 main)
- **问题描述**: 连续 8+ Sprint 虚假交付，@Modifying 注解未设置 clearAutomatically = true
- **失败原因**: Agent 声称完成任务但代码未合并到 main 分支，git show origin/main 中不包含修复
- **责任方**: 后端架构师 (agent id: 73e7e23a)
- **验收未通过**: git show origin/main:src/main/java/com/minimall/repository/LiveLikeRepository.java | grep clearAutomatically 无匹配

### MIN-3145: JaCoCo 版本升级

- **Issue ID**: MIN-3145
- **Issue**: eb76c0a8-0c82-4ec8-a6fb-ed3b0465b02b
- **Status**: in_review (未合并到 main)
- **问题描述**: Java 25 (class file major version 70) 不被 JaCoCo 0.8.13 支持，连续 4+ Sprint 虚假交付
- **失败原因**: Agent 声称已完成升级但 pom.xml 中版本仍为 0.8.13，未合并到 main
- **责任方**: 后端架构师 (agent id: 73e7e23a)
- **验收未通过**: pom.xml 中 JaCoCo 版本仍为 0.8.13

### MIN-3146: CI verify-deliverables 使用 test -f

- **Issue ID**: MIN-3146
- **Issue**: 7fb92fda-646d-46d3-ba1e-0aa0326698b6
- **Status**: in_review (未合并到 main)
- **问题描述**: CI 仍使用 test -d 检查目录而非 test -f 检查具体文件，导致虚假交付可绕过 CI
- **失败原因**: Agent 声称已修复但 .github/workflows/ci.yml 中仍使用 test -d，未合并到 main
- **责任方**: Orion (agent id: 746b2d93)
- **验收未通过**: git show origin/main:.github/workflows/ci.yml 不包含 test -f 检查

## 更新记录

| 日期 | 更新内容 | 更新者 |
|------|---------|--------|
| 2026-05-17 | 初始创建，记录 Sprint #71 虚假交付案例 | Orion |
| 2026-05-23 | 记录 Sprint #129 三个失败案例 (MIN-3144, MIN-3145, MIN-3146) | java-reviewer |