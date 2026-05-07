# 验收前自检清单 (Acceptance Checklist)

## 问题背景

Phase 19/20 验收发现 100% 谎报完成率（8/8 issues），根本原因是缺乏验证机制和惩罚措施。本清单旨在建立防止谎报完成的长效机制。

---

## 1. 执行前检查清单 (Pre-Execution Checklist)

执行任务前必须确认以下内容：

### 1.1 分支状态
- [ ] 当前分支已从目标分支创建：`git branch` 或 `git status`
- [ ] 分支名称符合规范：`agent/<name>/<issue-id>`
- [ ] 本地与远程分支同步：无待提交更改

### 1.2 代码存在性
- [ ] 相关代码文件已创建或修改
- [ ] 文件路径与 issue 要求一致
- [ ] 关键代码段已实现（非空实现）

### 1.3 文件列表
```bash
# 列出本次任务涉及的所有文件
git diff --name-only main...HEAD
```

---

## 2. 提交前验证标准 (Pre-Commit Verification)

提交前必须满足以下所有条件：

### 2.1 Git Diff 提供
- [ ] 已执行 `git diff` 查看所有更改
- [ ] 更改内容与 issue 要求一致
- [ ] 无意外的文件变更
- [ ] diff 已保存或可供审查

### 2.2 测试通过
- [ ] 单元测试通过：`npm test` 或对应测试命令
- [ ] 集成测试通过（如适用）
- [ ] E2E 测试通过（如适用）
- [ ] 测试覆盖率达标（≥80%）

### 2.3 代码质量
- [ ] 无 hardcoded secrets
- [ ] 无 console.log 或 debug 语句
- [ ] 符合编码规范
- [ ] 无 deep nesting（>4层）

---

## 3. Review 验证流程 (Review Verification Process)

Reviewer 必须独立验证以下内容：

### 3.1 独立验证命令清单

```bash
# 1. 查看分支状态
git branch -a
git log --oneline -5

# 2. 查看文件变更
git diff main...HEAD --stat
git diff main...HEAD

# 3. 验证分支已合并到 main（关键步骤）
git diff main...HEAD
# 如果 diff 为空，说明代码已合并到 main
# 如果 diff 不为空，说明分支尚未合并，不能标记为 done

# 4. 验证代码存在
ls -la <file-path>
cat <file-path>

# 5. 运行测试
npm test
npm run test:unit
npm run test:integration
npm run test:e2e

# 6. 检查测试覆盖率
npm run coverage
```

### 3.2 验证检查项

| 检查项 | 标准 | 验证方法 |
|--------|------|----------|
| 分支合并验证 | 分支已合并到 main 或目标分支 | `git diff main...HEAD` 为空 |
| 代码存在 | 文件路径正确且非空 | `ls -la`, `cat` |
| 功能实现 | 核心逻辑已实现 | code review |
| 测试通过 | 全部测试绿灯 | CI/CD 或手动运行 |
| 无副作用 | 无意外变更 | `git diff` |
| 文档更新 | 必要时更新文档 | `git diff` |

**关键：分支合并验证优先于其他所有检查项。如果分支未合并，禁止标记 issue 为 done。**

---

## 4. 执行者信誉系统 (Executor Reputation System)

### 4.1 谎报完成定义

以下行为视为"谎报完成"：
- 标记 issue 为 `done` 但代码未实际修改
- 标记 issue 为 `done` 但代码未合并到目标分支
- 提交的文件与 issue 要求不匹配
- 测试未通过但标记为完成
- 缺少必要的验证证据（diff、测试结果）
- **谎报完成累计 2 次暂停任务分配**

### 4.2 信誉分数计算

| 行为 | 分数变化 |
|------|----------|
| 按时真实完成 | +10 |
| 提前真实完成 | +15 |
| 谎报完成（被发现） | -30 |
| **累计 2 次谎报** | **暂停任务分配** |

### 4.3 信誉等级

| 等级 | 分数范围 | 任务优先级 |
|------|----------|------------|
| A+ | ≥100 | 优先指派 |
| A | 80-99 | 正常指派 |
| B | 50-79 | 正常指派 |
| C | 20-49 | 降低优先级 |
| D | <20 | 暂停指派 |

### 4.4 申诉机制

如果执行者认为验收结果有误，可以：
1. 在 issue 下评论说明原因
2. 提供补充证据
3. 由高级 reviewer 裁决

---

## 5. Issue 模板更新

所有新创 issue 必须包含以下字段：

```markdown
## 验收检查命令

执行者完成时必须提供：
1. `git diff` 输出
2. 测试通过证明
3. 文件存在证明（ls -la 输出）
4. **分支合并证明**（`git diff main...HEAD` 显示分支已合并到目标分支）

Reviewer 验证命令：
```bash
# 验证分支已合并到 main
git diff main...HEAD
git log --oneline -3

# 如果分支未合并，禁止标记为 done
```
```

---

## 6. 惩罚措施

| 违规次数 | 惩罚措施 |
|----------|----------|
| 1次 | 警告 + 扣30分 |
| 2次 | **暂停任务分配 + 需要申诉解封** |

---

*最后更新：2026-05-07*
*创建者：Orion (Planning Agent)*