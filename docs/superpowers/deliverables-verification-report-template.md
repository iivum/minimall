# 交付物验证报告模板 / Deliverables Verification Report Template

## 概述

本文档为测试任务提供标准化的验证报告格式，确保所有测试交付物可追溯、可验证。

---

## 验证报告格式

```markdown
# 交付物验证报告

## 基本信息

| 字段 | 值 |
|------|-----|
| Issue ID | MIN-XXXX |
| 执行者 | [Agent名称] |
| 开始时间 | YYYY-MM-DD HH:MM:SS |
| 完成时间 | YYYY-MM-DD HH:MM:SS |
| 验证状态 | PASS/FAIL |

---

## 交付物清单

### 测试文件

| 文件路径 | 大小 | 行数 | 状态 |
|---------|------|------|------|
| `src/test/java/...` | XXX KB | XXX | ✅ |

### 覆盖率报告

| 指标 | 目标 | 实际 | 状态 |
|------|------|------|------|
| 整体覆盖率 | ≥ 70% | XX% | ✅/❌ |
| Controller覆盖率 | ≥ XX% | XX% | ✅/❌ |
| Service覆盖率 | ≥ XX% | XX% | ✅/❌ |

---

## 验证命令执行结果

```bash
./scripts/verify-deliverables.sh [files...]
```

**输出:**
```
=== 交付物验证 ===

✅ src/test/java/com/minimall/controller/...
✅ src/test/java/com/minimall/service/...
...

=== 汇总 ===
总计: X
通过: X
失败: 0

✅ 全部验证通过
```

---

## 验证清单

- [ ] 所有测试文件存在且通过 `test -f` 验证
- [ ] 覆盖率报告生成 (target/site/jacoco/index.html)
- [ ] JaCoCo 检查通过 (mvn jacoco:check)
- [ ] 所有测试通过 (mvn test)
- [ ] 验证命令退出码为 0

---

## 问题与备注

[记录任何异常或需要注意的事项]

---

## 版本历史

| 版本 | 日期 | 修改内容 |
|------|------|----------|
| 1.0 | YYYY-MM-DD | 初始版本 |
```

---

## 使用说明

1. **测试任务开始时**: 复制此模板到任务目录
2. **每阶段完成时**: 填写验证报告并作为交付物提交
3. **CI集成**: 验证报告自动生成并上传到构建 artifact

---

## 示例

参见 `team-driven-verification.md` 中的步骤模板。

---

## 相关文档

- `scripts/verify-deliverables.sh` - 验证脚本
- `team-driven-verification.md` - 团队驱动验证机制