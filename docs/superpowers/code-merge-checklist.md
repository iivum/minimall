# 代码合并检查清单 (Code Merge Checklist)

## 目的

防止代码合并声明与实际状态不符的问题再次发生。确保所有代码变更必须通过 PR 合并到 main 分支，并经过客观验证。

## 合并前检查 (Pre-Merge Checklist)

### 1. PR 创建验证
- [ ] PR 标题格式: `<type>: <description> (#issue-id)`
- [ ] PR 描述包含: 变更内容、测试结果、相关 issue
- [ ] PR 链接已在相关 issue 的 comment 中提供

### 2. 代码变更验证
- [ ] `git diff main...<branch-name>` 确认变更内容正确
- [ ] 变更已通过 CI/CD 检查 (build, test, checkstyle)
- [ ] 无敏感信息泄露 (API keys, passwords, tokens)

### 3. 分支状态验证
- [ ] 分支与 main 无冲突或已解决冲突
- [ ] 分支已基于最新 main 拉取
- [ ] 分支名称符合规范: `agent/<type>/<commit-hash>`

## 合并后验证 (Post-Merge Verification)

### 4. 主分支验证
- [ ] `git log origin/main --oneline | head -10` 确认提交已合并
- [ ] `git branch -a --merged main` 确认分支已合并
- [ ] 验证关键文件存在: `git ls-tree origin/main <file-path>`

### 5. 功能验证
- [ ] 代码可正常编译: `mvn compile`
- [ ] 相关单元测试通过: `mvn test`
- [ ] 代码风格检查通过: `mvn checkstyle:check`

### 6. Issue 状态更新
- [ ] Issue status 变更为 `in_review`
- [ ] PR 链接已作为 comment 发布到 issue
- [ ] 所有验收标准已满足

## 分支管理规范

### 推荐流程
```
1. 创建功能分支: git checkout -b agent/<type>/<hash>
2. 开发并提交代码
3. 创建 PR 到 main
4. CI/CD 验证通过
5. Code Review 通过
6. 合并到 main
7. 验证合并结果
8. 更新 issue status
```

### 禁止事项
- ❌ 直接 push 到 main 分支
- ❌ 在未验证的情况下声称代码已合并
- ❌ 合并存在冲突的代码而未解决
- ❌ 合并未经测试的代码

## 快速验证命令

```bash
# 检查分支是否已合并到 main
git branch -a --merged main | grep <branch-name>

# 检查文件是否在 main 中存在
git ls-tree origin/main <file-path>

# 查看最近的 main 提交
git log origin/main --oneline | head -10

# 查看分支与 main 的差异
git diff main..<branch-name> --stat
```

## 验收标准

完成合并后，以下条件必须满足:

1. ✅ 代码已在 `origin/main` 中可验证
2. ✅ 所有相关 issue 已更新 PR 链接
3. ✅ 代码通过所有 CI 检查
4. ✅ Code Review 已通过

## 使用场景

- Sprint 结束时验证所有 in_review issues
- 代码审查时检查 PR 是否符合规范
- 发现合并问题时的根因分析

---

**创建时间**: 2026-05-10
**维护者**: 后端架构师
**审核者**: Sprint 排序师