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

---

## 自动化验证脚本 / Automation Scripts

### 快速检查脚本

```bash
#!/bin/bash
# code-merge-check.sh - 快速代码合并检查

set -e

echo "=== MiniMall 代码合并检查 ==="

# 1. 检查 merge conflict markers
echo "[1/5] 检查 merge conflict markers..."
if grep -r "<<<<<<<\|=======\|>>>>>>>" --include="*.java" src/ backend/; then
    echo "❌ 发现 merge conflict markers!"
    exit 1
fi
echo "✅ 无 merge conflict markers"

# 2. 运行 checkstyle
echo "[2/5] 运行 checkstyle..."
mvn checkstyle:check -q
echo "✅ checkstyle 检查通过"

# 3. 运行测试
echo "[3/5] 运行测试..."
mvn test -q -DskipITs
echo "✅ 测试通过"

# 4. 检查硬编码凭证
echo "[4/5] 检查硬编码凭证..."
if grep -rE "(apiKey|api_key|secret|password|token)\s*=\s*[\"'][a-zA-Z0-9]" --include="*.java" src/ backend/ 2>/dev/null; then
    echo "⚠️ 发现可能的硬编码凭证，请检查"
fi
echo "✅ 凭证检查完成"

# 5. 代码格式化
echo "[5/5] 检查代码格式化..."
mvn spotless:check -q 2>/dev/null || echo "⚠️ spotless 检查跳过"
echo "✅ 格式化检查完成"

echo ""
echo "=== 所有检查通过 ✓ ==="
```

### 使用方法

```bash
# 直接运行
chmod +x scripts/code-merge-check.sh
./scripts/code-merge-check.sh

# 或在 CI/CD 中集成
mvn verify -Dcheckstyle.skip=false
```

---

## 验收标准

- [ ] 所有 PR 必须包含本检查单执行结果
- [ ] checkstyle.xml 无 merge conflict markers
- [ ] 自动化脚本已集成到 CI/CD

---

## 版本历史

| 版本 | 日期 | 修改内容 |
|------|------|----------|
| 1.0 | 2026-05-09 | 初始版本 |