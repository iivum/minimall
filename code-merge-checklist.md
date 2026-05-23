# 代码合并检查清单 / Code Merge Checklist

## 执行要求

**所有 Agent 在提交 PR 到 `in_review` 状态前必须执行以下检查项。**

---

## 检查项 / Checklist

### 1. 代码格式检查 / Code Format

- [ ] `mvn checkstyle:check` 通过
- [ ] `mvn spotless:check` 通过（如适用）
- [ ] 代码无 merge conflict markers（`<<<<<<<`, `=======`, `>>>>>>>`）

### 2. 测试覆盖 / Test Coverage

- [ ] 新功能包含单元测试
- [ ] 关键业务逻辑有集成测试
- [ ] `mvn test` 全部通过

### 3. 安全检查 / Security

- [ ] 无硬编码凭证（API keys, passwords, tokens）
- [ ] 用户输入已验证
- [ ] SQL 注入防护（使用参数化查询）
- [ ] 无 XSS 漏洞

### 4. 业务逻辑 / Business Logic

- [ ] 日志级别正确使用
  - `log.error`: 仅用于真正的错误情况
  - `log.warn`: 用于可恢复的问题
  - `log.info`: 用于重要业务事件
- [ ] 错误处理完善
- [ ] 无静默失败

### 5. 文档更新 / Documentation

- [ ] README.md 已更新（如需要）
- [ ] API 文档已更新（如需要）
- [ ] 必要注释已添加

### 6. Git 规范 / Git Standards

- [ ] 提交信息符合规范
- [ ] 无不必要的文件提交
- [ ] 分支名称符合规范

### 7. 技术债检查 / Tech Debt Check

**目标：技术债增长率 < 5% per Sprint**

- [ ] 新代码复杂度合理（方法不超过 50 行，类不超过 500 行）
- [ ] 无重复代码（使用工具检测：PMD/CPD 或等效脚本）
- [ ] 无硬编码值（魔法数字、字符串应提取为常量）
- [ ] 无循环依赖或不合理耦合
- [ ] 数据库查询有适当索引（如有新增查询）
- [ ] 异步方法使用有界线程池（非 SimpleAsyncTaskExecutor）
- [ ] 无 Field Injection（应使用构造器注入）
- [ ] 变更已记录到 tech-debt-backlog.md（如引入新 tech debt）

---

## 技术债增长率目标

**目标：技术债增长率 < 5% per Sprint**

### 计算方法

```
技术债增长率 = (新增 tech debt items / 总 items) × 100%
```

### 自动化检测脚本

参考 `scripts/detect-tech-debt.sh`

### 验收标准

- [ ] 所有 PR 必须包含本检查单执行结果
- [ ] checkstyle.xml 无 merge conflict markers
- [ ] 自动化脚本已集成到 CI/CD

---

## 版本历史

| 版本 | 日期 | 修改内容 |
|------|------|----------|
| 1.2 | 2026-05-24 | 添加技术债检查项（第7节），目标增长率 < 5% per Sprint（MIN-3285） |
| 1.1 | 2026-05-23 | 添加 worktree→main 核心原则和 Post-Merge 验证清单（MIN-3202） |
| 1.0 | 2026-05-09 | 初始版本 |