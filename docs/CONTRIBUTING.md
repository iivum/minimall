# 代码提交规范

## 概述

本文档定义了 minimall 项目的代码提交标准，确保所有代码修改可追溯并与 issue 正确关联。

## 提交规范

### 1. Commit Message 格式

```
<type>: <description>

[optional body]
```

**Type 类型：**
- `feat`: 新功能
- `fix`: 缺陷修复
- `refactor`: 重构
- `docs`: 文档更新
- `test`: 测试相关
- `chore`: 构建/工具调整
- `perf`: 性能优化
- `ci`: CI/CD 相关

### 2. Issue 关联要求

**所有代码修改必须在 issue 评论中附上 git commit hash**

提交流程：
1. 完成代码修改后，创建 commit
2. 将 commit push 到远程分支
3. 在对应的 issue 下发表评论，格式：
   ```
   Commit: <完整commit hash>
   ```
4. 提交 PR 并请求审查

示例：
```bash
# 完成代码后
git add .
git commit -m "fix: 修复支付回调重复处理问题"
git push origin feature/payment-fix

# 在 issue 评论中附上 commit hash
cat <<'COMMENT' | multica issue comment add <issue-id> --content-stdin
Commit: a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0
COMMENT
```

### 3. Commit Hash 获取方式

```bash
# 获取最新 commit hash
git log -1 --format="%H"

# 获取特定文件的最新 commit hash
git log -1 --format="%H" -- <file-path>

# 获取某个 commit 的完整 hash
git rev-parse HEAD
```

## 代码审查清单

提交 PR 前需确认：

- [ ] Commit message 格式正确
- [ ] 代码通过编译 (`./mvnw compile`)
- [ ] 已有测试覆盖新功能
- [ ] 测试通过 (`./mvnw test`)
- [ ] 无硬编码秘密或凭证
- [ ] Issue 评论中已附上 commit hash
- [ ] 审查者已分配

## 分支策略

- `main`: 主分支，仅通过 PR 合并
- `feature/<feature-name>`: 功能分支
- `fix/<issue-id>-<brief-desc>`: 修复分支

## 标签规范

Issue 标签用于分类任务：
- `P0`/`P1`/`P2`: 优先级
- `backend`/`frontend`: 模块
- `bug`/`feature`/`docs`: 类型

---

*本文档由 Orion 于 2026-05-06 建立*
