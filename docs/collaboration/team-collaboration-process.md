# 团队协作流程

## 目的

建立 issue 状态变更规范，明确 "in_review" 状态的标准，强化验收流程：只有代码真正提交才能标记完成。

---

## 1. Issue 状态定义

### 1.1 状态流转

```
todo → in_progress → in_review → done
                 ↘ blocked
                 ↘ cancelled
```

### 1.2 状态说明

| 状态 | 定义 | 进入条件 | 退出条件 |
|------|------|----------|----------|
| `todo` | 待处理 | 创建 Issue 时 | 开始工作时 |
| `in_progress` | 进行中 | 开始执行 Issue | 完成或阻塞 |
| `blocked` | 阻塞 | 遇到无法解决的问题 | 问题解决 |
| `in_review` | 审核中 | 代码已提交，等待验收 | 验收通过或打回 |
| `done` | 完成 | 验收通过 | - |
| `cancelled` | 取消 | 不再需要执行 | - |

---

## 2. Issue 状态变更规范

### 2.1 状态变更时机

| 动作 | 触发状态变更 | 变更后状态 |
|------|--------------|------------|
| 开始工作 | `todo` → `in_progress` | 进行中 |
| 遇到阻塞 | `in_progress` → `blocked` | 阻塞 |
| 阻塞解决 | `blocked` → `in_progress` | 进行中 |
| 完成自检 | `in_progress` → `in_review` | 审核中 |
| 验收通过 | `in_review` → `done` | 完成 |
| 验收打回 | `in_review` → `in_progress` | 进行中 |
| 取消任务 | `in_progress` → `cancelled` | 已取消 |

### 2.2 状态变更命令

```bash
# 开始工作
multica issue status <issue-id> in_progress

# 标记阻塞
multica issue status <issue-id> blocked

# 提交审查
multica issue status <issue-id> in_review

# 标记完成
multica issue status <issue-id> done

# 标记取消
multica issue status <issue-id> cancelled
```

---

## 3. "in_review" 状态标准

### 3.1 进入 in_review 前必须满足

- [ ] **代码已提交**: 提交记录存在于仓库中
- [ ] **通过检查清单**: 代码提交检查清单所有项目已通过
- [ ] **构建通过**: 本地构建成功
- [ ] **测试通过**: 所有测试通过，覆盖率满足要求
- [ ] **文档已更新**: 相关文档已更新
- [ ] **Issue 评论已发布**: 在 Issue 下发布完成评论，说明已完成的工作

### 3.2 in_review 阶段说明

进入 in_review 状态后:
1. Reviewer 会检查代码质量和规范遵守情况
2. 如有问题，会打回给执行者修复
3. 只有在所有问题修复后才能标记为 done

---

## 4. 验收流程

### 4.1 验收检查项

Reviewer 在验收时应检查:

1. **提交记录验证**
   - [ ] 提交记录存在于仓库中
   - [ ] 提交信息符合规范
   - [ ] 提交内容与 Issue 要求一致

2. **代码质量检查**
   - [ ] 构建通过
   - [ ] 测试通过
   - [ ] 覆盖率满足要求
   - [ ] 无新增的编译警告

3. **规范遵守检查**
   - [ ] 代码风格符合项目规范
   - [ ] 无硬编码敏感信息
   - [ ] 无调试代码遗留

4. **文档检查**
   - [ ] 相关文档已更新
   - [ ] API 变更已记录

### 4.2 验收结果

#### 验收通过

如果所有检查项通过:
1. 将 Issue 状态更新为 `done`
2. 在 Issue 下发布验收通过评论
3. 通知相关人员

#### 验收打回

如果发现问题:
1. 将 Issue 状态更新为 `in_progress`
2. 在 Issue 下详细说明问题
3. 指派给执行者修复

---

## 5. 团队协作规范

### 5.1 信息同步

- 定期更新 Issue 状态
- 在 Issue 下发布进度评论
- 遇到问题时及时标记 blocked

### 5.2 沟通规范

- 使用 `@mention` 提及其他成员或 agent
- 描述问题时提供足够的上下文
- 验收结果清晰明确

### 5.3 反馈机制

- 每完成一个子任务，发布进度更新
- 发现问题时及时反馈
- 验收通过/打回时给出明确结论

---

## 6. 常见问题处理

### 6.1 多次打回

如果 Issue 被多次打回:
1. 分析打回原因
2. 与 Reviewer 沟通确认要求
3. 修复后重新提交审查
4. 如有争议，升级给团队负责人

### 6.2 阻塞升级

如果 Issue 长时间处于 blocked 状态:
1. 在 Issue 下说明阻塞原因
2. 提及其他团队成员寻求帮助
3. 如无法解决，标记为 cancelled

---

## 文件位置

本规范保存在: `docs/collaboration/team-collaboration-process.md`

---

*最后更新: 2026-05-07*
*更新者: Orion*