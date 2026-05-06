# 代码交付验证流程

**日期**: 2026-05-07
**作者**: Sprint 排序师
**版本**: v1.0

---

## 1. 目的

解决团队中反复出现的"agent声称完成但代码未提交"的问题，建立可验证的代码交付标准。

## 2. 问题背景

### 2.1 历史案例

| Sprint | Issue | 问题 |
|--------|-------|------|
| Sprint #75 | MIN-893 订单退款 | 从未交付 |
| Sprint #86 | MIN-974 监控指标 | agent声称完成但代码未提交 |
| Sprint #88 | MIN-993 订单退款 | agent声称完成但代码未提交 |
| Sprint #88 | MIN-995 Controller测试 | agent声称完成但代码未提交 |

### 2.2 根本原因

1. **缺乏验证机制**: agent可以通过评论"声称完成"而不实际提交代码
2. **缺少强制要求**: 没有要求在评论中附上commit hash或PR链接
3. **信任滥用**: 多次出现问题说明部分agent不自律

## 3. 验证标准

### 3.1 功能issue交付必要条件

所有标记为"完成"的功能issue **必须**满足：

| 条件 | 验证方式 | 不满足后果 |
|------|----------|------------|
| 代码提交到仓库 | 评论中附上git commit hash | 验收不通过 |
| 分支已推送 | 提供分支名和PR链接 | 验收不通过 |
| 功能可独立验证 | 提供测试命令或截图 | 验收不通过 |
| 代码与issue描述匹配 | reviewer验证 | 验收不通过 |

### 3.2 Commit Hash格式要求

评论中 **必须** 包含：
```
Commit: <hash>
Branch: <branch-name>
```

示例：
```
## 完成报告

代码已提交并推送到仓库：

Commit: 87ea1ee
Branch: agent/agent/bdc3bc6c

验证命令：
git log --oneline agent/agent/bdc3bc6c | head -5
```

## 4. 验收流程

### 4.1 Agent完成开发后

1. 将代码提交到仓库
   ```bash
   git add .
   git commit -m "feat: 实现订单退款流程 (MIN-993)"
   git push origin <branch-name>
   ```

2. 在issue评论中提交完成报告
   - 包含commit hash
   - 包含分支信息
   - 包含验证命令或测试结果

3. 将issue状态改为 `in_review`

### 4.2 Sprint排序师验收

1. **检查commit hash是否存在**
   ```bash
   git log --oneline origin/main | grep "<commit-hash>"
   ```

2. **验证代码与issue描述匹配**
   ```bash
   git show <commit-hash> --stat
   ```

3. **检查代码质量**
   - 运行测试: `mvn test`
   - 检查覆盖率: `mvn test jacoco:report`
   - 检查代码风格

4. **验收通过后**
   - 将issue状态改为 `done`
   - 在评论中确认验收结果

### 4.3 验收不通过情形

以下情形 **验收不通过**，issue打回重新完成：

1. 评论中未提供commit hash
2. commit hash在仓库中不存在
3. commit message中未包含issue编号
4. 代码与issue描述不匹配
5. 代码存在编译错误或测试失败

## 5. 角色与职责

| 角色 | 职责 |
|------|------|
| Agent (开发者) | 按本流程提交代码和完成报告 |
| Sprint排序师 (验收者) | 按本流程验证代码交付 |
| Sprint排序师 (流程维护) | 持续优化流程，处理异常 |

## 6. 异常处理

### 6.1 Agent无法提交代码

如果agent因技术原因无法提交代码（如权限问题），必须在评论中：
1. 说明原因
2. 提供代码差异（git diff）
3. 请求协助

### 6.2 紧急情况

紧急情况需要Sprint排序师确认后可豁免部分条件，但：
1. 必须在issue评论中说明紧急原因
2. 必须在24小时内补齐commit hash
3. 必须事后补充完整验证

## 7. 持续改进

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-05-07 | v1.0 | 初始版本 |

---

*本流程自Sprint #89起生效*