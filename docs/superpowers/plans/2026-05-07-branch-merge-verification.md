# Branch Merge Verification Mechanism

**Issue**: MIN-1138
**Phase**: 24
**Date**: 2026-05-07
**Status**: Active

---

## 目的

建立强制的分支合并验证机制，防止谎报完成问题再次发生。

## 问题背景

Phase 19-23 连续多阶段出现大量谎报完成，根本原因是缺乏强制验证。

## 验证流程

### 分支合并前验证清单

在将任何分支合并到 main 分支前，必须完成以下验证：

1. **分支状态检查**
   ```bash
   # 确认分支与 main 同步
   git fetch origin main
   git log --oneline origin/main..HEAD
   ```

2. **合并冲突检查**
   ```bash
   # 检查是否可无冲突合并
   git merge --no-commit --no-ff origin/main
   git merge --abort  # 如有冲突，放弃合并
   ```

3. **构建验证**
   ```bash
   # 确保代码可构建
   mvn compile  # Java 项目
   # 或对应项目的构建命令
   ```

4. **测试验证**
   ```bash
   # 运行测试确保无回归
   mvn test
   ```

### 分支合并后验证清单

1. **合并证明**
   ```bash
   # 确认合并成功
   git log --oneline -1 origin/main
   git show --stat origin/main
   ```

2. **构建状态验证**
   ```bash
   # 验证 main 分支构建成功
   mvn compile
   ```

## 完成流程要求

### Issue 完成前必须提供

- 分支合并证明（merge commit SHA）
- 构建成功截图或日志
- 测试通过证明

### 强制字段

所有新创建的 issue 必须在描述中包含：

```markdown
## 分支合并验证
- [ ] 分支与 main 同步
- [ ] 无合并冲突
- [ ] 构建成功
- [ ] 测试通过
- [ ] 已提供合并证明
```

## 验收标准

- [x] 所有新创 issue 包含分支合并验证要求
- [x] 完成流程包含强制验证步骤
- [x] 已建立验证命令清单

## 相关文档

- [Sprint #34 Report](../sprint-34-report.md)
- [CHANGELOG.md](../../CHANGELOG.md)
